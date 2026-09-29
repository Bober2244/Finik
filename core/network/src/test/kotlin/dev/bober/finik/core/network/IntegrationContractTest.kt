package dev.bober.finik.core.network

import dev.bober.finik.core.network.dto.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class IntegrationContractTest {
    @Test
    fun resumeDemoDoesNotRepeatResetPreset() {
        val resume = FinikJson.json.encodeToJsonElement(LoginIn("finik-id-123", "Europe/Moscow", "demo")).jsonObject
        assertFalse(resume.containsKey("demo_preset"))
        val prepared = FinikJson.json.encodeToJsonElement(LoginIn("finik-id-123", "Europe/Moscow", "demo", "prepared")).jsonObject
        assertEquals("prepared", prepared.getValue("demo_preset").jsonPrimitive.content)
    }

    @Test
    fun numericAnswerAndMultiAccessoryRequestsMatchServer() {
        val answer = FinikJson.json.encodeToJsonElement(AnswerIn("q1", answerValue = 8)).jsonObject
        assertEquals(8, answer.getValue("answer_value").jsonPrimitive.int)
        assertFalse(answer.containsKey("answer_index"))
        val owl = FinikJson.json.encodeToJsonElement(PetIn("Сова", "OWL", 40, lookVariant = 5, accessories = listOf("hat", "medal"))).jsonObject
        assertEquals(5, owl.getValue("look_variant").jsonPrimitive.int)
        assertEquals(2, owl.getValue("accessories").jsonArray.size)
    }

    @Test
    fun httpAllowedOnlyByExplicitDebugConfiguration() {
        assertFalse(NetworkConfig("http://10.0.2.2:8000").canConnect)
        assertTrue(NetworkConfig("http://10.0.2.2:8000", allowInsecure = true).canConnect)
        assertTrue(NetworkConfig("https://api.example.org").canConnect)
    }

    @Test
    fun serverAddressInputAcceptsIpHostPortAndFullUrls() {
        assertEquals("http://192.168.1.43:8000/", NetworkConfig.normalizeUserAddress(" 192.168.1.43 "))
        assertEquals("http://localhost:8001/", NetworkConfig.normalizeUserAddress("localhost:8001"))
        assertEquals("http://[::1]:8000/", NetworkConfig.normalizeUserAddress("[::1]"))
        assertEquals("https://api.example.org/finik/", NetworkConfig.normalizeUserAddress("https://api.example.org/finik"))
        assertEquals("http://192.168.1.43/", NetworkConfig.normalizeUserAddress("http://192.168.1.43"))
    }

    @Test
    fun serverAddressInputRejectsCredentialsParametersFragmentsAndBadPorts() {
        listOf("", "ftp://example.org", "user:secret@example.org", "http://user:secret@example.org",
            "host:0", "host:65536", "host:-1", "host:abc", "host?key=1", "http://host/#part", "not a host")
            .forEach { input ->
                assertTrue("Should reject $input", runCatching { NetworkConfig.normalizeUserAddress(input) }.isFailure)
            }
    }

    @Test
    fun failedFinancialCommandIsNotAutomaticallyRepeated() = runTest {
        var attempts = 0
        val api = createFinikApi("https://api.example.org", { "jwt" }, MockEngine {
            attempts++
            throw IOException("Disconnected after sending request")
        })
        assertTrue(runCatching { api.deposit(DepositIn(5)) }.isFailure)
        assertEquals(1, attempts)
    }

    @Test
    fun readReconnectsOnceAfterAnUnexpectedlyClosedSocket() = runTest {
        var attempts = 0
        val api = createFinikApi("https://api.example.org", { "jwt" }, MockEngine {
            attempts++
            if (attempts == 1) throw IOException("unexpected end of stream")
            respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        assertTrue(api.questions("needs").isEmpty())
        assertEquals(2, attempts)
    }

    @Test
    fun readReconnectIsBoundedWhenTheNetworkRemainsUnavailable() = runTest {
        var attempts = 0
        val api = createFinikApi("https://api.example.org", { "jwt" }, MockEngine {
            attempts++
            throw IOException("offline")
        })
        assertTrue(runCatching { api.review() }.isFailure)
        assertEquals(2, attempts)
    }

    @Test
    fun parsesAdventureSceneAndEmptySpacedReview() = runTest {
        val api = createFinikApi("https://api.example.org", { "jwt" }, MockEngine { req ->
            val body = if (req.url.encodedPath.endsWith("/learning/review")) {
                """{"topic":null,"title":"Повторять пока нечего","question":"","options":[],"next_due":null}"""
            } else {
                """{"slug":"adventure_market","title":"Ярмарка","summary":"Покупки","stage":1,"total":5,"wallet":14,"reserve":6,"care":2,"joy":0,"score":1,"dream":0,"feedback":"Готово","completed":false,"rewarded":false,"medal":null,"outcome":"","scene":{"kind":"MARKET","title":"Выбор","story":"Купим воду?","choices":[{"id":"water","title":"Вода","detail":"4 монеты","cost":4}]}}"""
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        assertNull(api.review().topic)
        assertEquals("water", api.adventure("adventure_market").scene.choices.single().id)
    }
}
