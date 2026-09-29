package dev.bober.finik.core.data

import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.network.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FinikRepositoryTest {
    @Test
    fun demoSwitchAndOfflineRestartKeepNormalCacheAndNeverIssueOfflineMutations() = runTest {
        val session = FakeSession()
        val storage = FakeCache()
        val server = FakeServer(session)
        val dispatcher = StandardTestDispatcher(testScheduler)
        fun repository() = FinikRepository(storage, { _, _ -> server.api }, session, DeviceIdProvider(session), NetworkConfig("https://test.example"), dispatcher)
        val repo = repository()
        advanceUntilIdle()
        repo.busy.first { !it }
        assertEquals(10, repo.state.value.plan.freeCoins)
        assertFalse(repo.state.value.demoMode)
        assertEquals("normal", session.mode())
        val normalCache = storage.values[1]
        repo.startDemo(prepared = true)
        assertEquals(99, repo.state.value.plan.freeCoins)
        assertTrue(repo.state.value.demoMode)
        assertEquals(normalCache, storage.values[1])
        assertTrue(storage.values.containsKey(2))
        repo.leaveDemo()
        assertFalse(repo.state.value.demoMode)
        assertEquals(10, repo.state.value.plan.freeCoins)
        assertEquals(listOf(null, "prepared", null), server.presets)
        server.offline = true
        val restarted = repository()
        advanceUntilIdle()
        restarted.busy.first { !it }
        assertTrue(restarted.state.value.onboarded)
        assertFalse(restarted.state.value.online)
        assertFalse(restarted.state.value.demoMode)
        assertEquals(10, restarted.state.value.plan.freeCoins)
        val calls = server.requests
        restarted.care(SpendCategory.FOOD)
        restarted.deposit(3)
        assertEquals(calls, server.requests)
        assertEquals(10, restarted.state.value.plan.freeCoins)
        assertNotNull(restarted.error.value)
    }

    @Test
    fun domainErrorKeepsStateOnlineAndDoesNotAwardAnything() = runTest {
        val session = FakeSession()
        val server = FakeServer(session)
        val repo = FinikRepository(FakeCache(), { _, _ -> server.api }, session, DeviceIdProvider(session), NetworkConfig("https://test.example"), StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        repo.busy.first { !it }
        repo.deposit(500)
        assertTrue(repo.state.value.online)
        assertEquals(10, repo.state.value.plan.freeCoins)
        assertEquals(0, repo.state.value.selectedGoal.saved)
        assertEquals(1, server.deposits)
    }

    @Test
    fun failedDemoLoginLeavesNormalSessionAndCacheUntouched() = runTest {
        val session = FakeSession()
        val storage = FakeCache()
        val server = FakeServer(session)
        val repo = FinikRepository(storage, { _, _ -> server.api }, session, DeviceIdProvider(session), NetworkConfig("https://test.example"), StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        repo.busy.first { !it }
        val saved = storage.values.toMap()
        server.offline = true
        repo.startDemo(prepared = false)
        assertEquals("normal", session.mode())
        assertFalse(repo.state.value.demoMode)
        assertEquals(10, repo.state.value.plan.freeCoins)
        assertEquals(saved, storage.values)
    }

    @Test
    fun readOnlySheetRetryReauthenticatesAfterAnOfflineFailureWithoutResettingDemo() = runTest {
        val session = FakeSession()
        val server = FakeServer(session)
        val repo = FinikRepository(FakeCache(), { _, _ -> server.api }, session, DeviceIdProvider(session), NetworkConfig("https://test.example"), StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        repo.busy.first { !it }
        repo.startDemo(prepared = true)
        server.offline = true
        assertNull(repo.review())
        assertFalse(repo.state.value.online)
        server.offline = false
        val loaded = repo.review()
        assertNotNull(loaded)
        assertTrue(repo.state.value.online)
        assertTrue(repo.state.value.demoMode)
        assertEquals(99, repo.state.value.plan.freeCoins)
        assertEquals(listOf(null, "prepared", null), server.presets)
        assertEquals(0, server.deposits)
    }
}

private class FakeCache : SnapshotStore {
    val values = mutableMapOf<Int, String>()
    override suspend fun read(profileId: Int) = values[profileId]
    override suspend fun write(profileId: Int, json: String) { values[profileId] = json }
    override suspend fun clear(profileId: Int) { values.remove(profileId) }
}
private class FakeSession : PlayerSession {
    private var active = "normal"
    private val tokens = mutableMapOf<String, String>()
    private var id: String? = "finik-test-device"
    override suspend fun mode() = active
    override suspend fun token() = tokens[active]
    override suspend fun activate(mode: String, token: String) { active = mode; tokens[mode] = token }
    override suspend fun clearToken() { tokens.remove(active) }
    override suspend fun saveToken(value: String) { tokens[active] = value }
    override suspend fun deviceId() = id
    override suspend fun saveDeviceId(value: String) { id = value }
    private var address: String? = null
    private var bound: String? = null
    override suspend fun serverAddress() = address
    override suspend fun boundServer() = bound
    override suspend fun saveServerAddress(address: String) { this.address = address }
    override suspend fun selectServer(address: String, saveOverride: Boolean) {
        bound = address
        if (saveOverride) this.address = address
        tokens.clear()
    }

}
private class FakeServer(session: PlayerSession) {
    var offline = false
    var requests = 0
    var deposits = 0
    val presets = mutableListOf<String?>()
    private var mode = "normal"
    val api = createFinikApi("https://test.example", { session.token() }, MockEngine { req ->
        requests++
        if (offline) throw IOException("offline")
        val path = req.url.encodedPath
        var status = HttpStatusCode.OK
        val body = when {
            path.endsWith("/auth/device") -> {
                val payload = FinikJson.json.parseToJsonElement((req.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject
                mode = payload.getValue("mode").jsonPrimitive.content
                presets += payload["demo_preset"]?.jsonPrimitive?.contentOrNull
                """{"token":"token-$mode","has_pet":true,"mode":"$mode","timezone":"Europe/Moscow"}"""
            }
            path.endsWith("/profile") -> """{"weekly_income":60,"state":${state()}}"""
            path.endsWith("/state") -> state()
            path.endsWith("/weeks/history") -> "{}"
            path.endsWith("/events/today") -> "null"
            path.endsWith("/ai/word-of-day") -> """{"word":"Budget","meaning":"Plan"}"""
            path.endsWith("/ai/remark") -> """{"text":"Hello"}"""
            path.endsWith("/learning/review") -> """{"topic":null,"title":"Review","question":"","options":[],"next_due":null}"""
            path.endsWith("/goal/deposit") -> {
                deposits++
                status = HttpStatusCode.UnprocessableEntity
                """{"error":{"code":"insufficient_funds","message":"Insufficient funds"}}"""
            }
            else -> "[]"
        }
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    })
    private fun state() = """{
        "free_coins":${if (mode == "demo") 99 else 10},"weekly_income":60,"mode":"$mode",
        "pet":{"name":"Owl","species":"OWL","xp":0},
        "week":{"number":1,"day":1,"income":40,"entries":[]},
        "goal":{"catalog_slug":"sunny_window","title":"Home","target":150,"saved":0},
        "timezone":"Europe/Moscow","can_advance_time":${mode == "demo"}
    }"""
}
