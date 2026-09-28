package dev.bober.finik.core.network

import dev.bober.finik.core.network.dto.LoginIn
import dev.bober.finik.core.network.dto.PlanIn
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinikJsonTest {
    @Test
    fun loginBodyUsesSnakeCase() {
        val raw = FinikJson.json.encodeToString(LoginIn.serializer(), LoginIn("android-abc-12345678"))
        assertTrue(raw.contains("\"device_id\""))
        assertFalse(raw.contains("deviceId"))
    }

    @Test
    fun planBodyUsesSnakeCaseKeys() {
        val raw = FinikJson.json.encodeToString(PlanIn.serializer(), PlanIn(14, 4, 6, 16))
        assertTrue(raw.contains("\"food\""))
        assertTrue(raw.contains("14"))
    }
}

class NetworkConfigTest {
    @Test
    fun blankAddressLeavesNetworkUnconfigured() {
        val config = NetworkConfig("  ")
        assertEquals("", config.normalizedBaseUrl)
        assertFalse(config.isConfigured)
        assertFalse(config.isSecure)

        val client = HttpClientFactory.create(config.baseUrl, { null }, MockEngine { error("Unexpected request") })
        client.close()
    }

    @Test
    fun blankAddressCannotIssueApiRequest() = runTest {
        var requests = 0
        val engine = MockEngine {
            requests++
            json("{}")
        }
        val api = createFinikApi("", { null }, engine)
        runCatching { api.getState() }
        assertEquals(0, requests)
    }

    @Test
    fun normalizesAbsoluteAddressAndIdentifiesHttps() {
        val config = NetworkConfig(" https://example.org/finik ")
        assertEquals("https://example.org/finik/", config.normalizedBaseUrl)
        assertTrue(config.isConfigured)
        assertTrue(config.isSecure)
        assertEquals("http://10.0.2.2:8000/", NetworkConfig.normalize("http://10.0.2.2:8000"))
    }

    @Test
    fun unsafeOrRelativeAddressesLeaveOptionalNetworkDisabled() {
        listOf("example.org", "ftp://example.org", "https://user:secret@example.org", "https://example.org/?key=1")
            .forEach { address ->
                assertFalse(NetworkConfig(address).isConfigured)
                try {
                    NetworkConfig.normalize(address)
                    throw AssertionError("Expected invalid URL: $address")
                } catch (_: IllegalArgumentException) {
                    // Invalid configuration must fail before creating HTTP requests.
                }
            }
    }
}

class FinikApiTest {
    @Test
    fun requestToDifferentOriginIsBlocked() = runTest {
        var requests = 0
        val engine = MockEngine {
            requests++
            json("{}")
        }
        val client = HttpClientFactory.create("https://finik.example/", { "private-token" }, engine)
        try {
            client.get("https://other.example/content")
            throw AssertionError("Expected a blocked cross-origin request")
        } catch (_: IllegalStateException) {
            assertEquals(0, requests)
        }
        client.close()
    }

    @Test
    fun loginParsesTokenAndSkipsBearer() = runTest {
        var sawAuth = false
        val engine = MockEngine { request ->
            if (request.url.encodedPath.contains("/auth/device")) {
                sawAuth = request.headers.contains(HttpHeaders.Authorization)
                json("""{"token":"jwt-1","has_pet":false}""")
            } else {
                error("unexpected ${request.url}")
            }
        }
        val api = createFinikApi("http://10.0.2.2:8000/", { "stale-token" }, engine)
        val out = api.login(LoginIn("android-device-abcdef12"))
        assertEquals("jwt-1", out.token)
        assertFalse(out.hasPet)
        assertFalse(sawAuth)
    }

    @Test
    fun stateSendsBearerAndParsesPet() = runTest {
        var auth: String? = null
        val engine = MockEngine { request ->
            auth = request.headers[HttpHeaders.Authorization]
            json(STATE_JSON)
        }
        val api = createFinikApi("http://10.0.2.2:8000/", { "jwt-live" }, engine)
        val state = api.getState()
        assertEquals("Bearer jwt-live", auth)
        assertEquals("Кустик", state.pet.name)
        assertEquals(0, state.freeCoins)
        assertEquals("WATER", state.careActions.first().category)
    }

    @Test
    fun todayEventNullBody() = runTest {
        val engine = MockEngine { json("null") }
        val api = createFinikApi("http://10.0.2.2:8000/", { "jwt" }, engine)
        assertNull(api.todayEvent())
    }

    @Test
    fun domainErrorEnvelopeBecomesApiException() = runTest {
        val engine = MockEngine {
            json(
                """{"error":{"code":"rule_violation","message":"План уже утверждён"}}""",
                HttpStatusCode.UnprocessableEntity,
            )
        }
        val api = createFinikApi("http://10.0.2.2:8000/", { "jwt" }, engine)
        try {
            api.confirmPlan()
            throw AssertionError("expected FinikApiException")
        } catch (e: FinikApiException) {
            assertEquals("rule_violation", e.code)
            assertEquals("План уже утверждён", e.message)
            assertEquals("План уже утверждён", e.userMessage())
        }
    }

    @Test
    fun fastapiDetailFallsBackToRulePhrase() = runTest {
        val engine = MockEngine {
            json(
                """{"detail":[{"type":"greater_than","loc":["body","amount"],"msg":"Input should be greater than 0"}]}""",
                HttpStatusCode.UnprocessableEntity,
            )
        }
        val api = createFinikApi("http://10.0.2.2:8000/", { "jwt" }, engine)
        try {
            api.deposit(dev.bober.finik.core.network.dto.DepositIn(0))
            throw AssertionError("expected FinikApiException")
        } catch (e: FinikApiException) {
            assertEquals("rule_violation", e.code)
            assertEquals("Так нельзя по правилам игры.", e.message)
        }
    }

    @Test
    fun originQuizAndChatAreWired() = runTest {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("/ai/origin") ->
                    json("""{"text":"Я семечко.","source":"fallback"}""")
                request.url.encodedPath.endsWith("/ai/quiz") ->
                    json("""{"kind":"quiz","source":"fallback","rewarded":false,"questions":[{"index":0,"question":"Скидка?","options":["20%","40%"]}]}""")
                request.url.encodedPath.endsWith("/ai/quiz/answer") ->
                    json("""{"correct":true,"explanation":"40%","coins":2,"rewarded":true}""")
                request.url.encodedPath.endsWith("/ai/chat") ->
                    json("""{"text":"Копилка только на мечту.","source":"fallback","blocked":false,"chat_left":11}""")
                else -> error(request.url.encodedPath)
            }
        }
        val api = createFinikApi("http://10.0.2.2:8000/", { "jwt" }, engine)
        assertEquals("Я семечко.", api.origin().text)
        assertEquals(1, api.quiz("quiz").questions.size)
        assertEquals(2, api.answerQuiz(dev.bober.finik.core.network.dto.QuizAnswerIn(0, 1)).coins)
        assertEquals(11, api.chat(dev.bober.finik.core.network.dto.ChatIn("Что такое копилка?")).chatLeft)
    }
}

private fun MockRequestHandleScope.json(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
) = respond(
    content = body,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
)

private const val STATE_JSON = """
{
  "free_coins": 0,
  "weekly_income": 40,
  "pet": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Кустик",
    "species": "FINIK",
    "xp": 0,
    "stage_index": 0,
    "stage_name": "Семечко",
    "next_stage_xp": 100,
    "mood": "OKAY",
    "mood_note": "Мне нормально.",
    "needs": [{"category": "FOOD", "percent": 70}],
    "look_variant": 0,
    "equipped_pot": "",
    "equipped_accessory": ""
  },
  "week": {
    "id": "22222222-2222-2222-2222-222222222222",
    "number": 1,
    "day": 1,
    "income": 40,
    "overrun": 0,
    "plan_confirmed": false,
    "entries": [
      {"category": "FOOD", "planned": 16, "spent": 0, "left": 16},
      {"category": "WATER", "planned": 12, "spent": 0, "left": 12},
      {"category": "PLAY", "planned": 4, "spent": 0, "left": 4},
      {"category": "SAVE", "planned": 8, "spent": 0, "left": 8}
    ]
  },
  "goal": {
    "id": "33333333-3333-3333-3333-333333333333",
    "title": "Солнечное окно и большой горшок",
    "target": 150,
    "saved": 0,
    "percent": 0,
    "remain": 150,
    "catalog_slug": "sunny_window",
    "why": "Ростку нужен свет."
  },
  "care_actions": [
    {"category": "WATER", "label": "Полить", "cost": 2}
  ],
  "streak": {
    "count": 1,
    "goal": 7,
    "bonus": 5,
    "days": [{"label": "Пн", "done": true}]
  },
  "last_income": 40,
  "last_income_note": "Пришёл доход 40",
  "last_purchase_note": "",
  "last_purchase_amount": 0,
  "owned_cosmetics": []
}
"""
