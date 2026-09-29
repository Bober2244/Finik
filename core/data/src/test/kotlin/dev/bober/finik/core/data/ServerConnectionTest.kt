package dev.bober.finik.core.data

import dev.bober.finik.core.network.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnectionTest {
    @Test
    fun switchPersistsAddressClearsBothCachesAndNeverSendsOldTokenToNewOrigin() = runTest {
        val f = fixture()
        f.ready()
        f.repo.startDemo(true)
        f.repo.leaveDemo()
        assertEquals(setOf(1, 2), f.cache.values.keys)
        val oldId = f.session.id
        val result = f.repo.connectToServer("new.example:8000")
        assertTrue(result.second, result.first)
        assertEquals("http://new.example:8000/", f.repo.serverAddress.value)
        assertEquals("http://new.example:8000/", f.session.saved)
        assertEquals(oldId, f.session.id)
        assertEquals(setOf(1), f.cache.values.keys)
        assertFalse(f.session.tokens.containsKey("demo"))
        assertEquals(20, f.repo.state.value.plan.freeCoins)
        val newRequests = f.hub.calls.filter { it.host == "new.example" }
        assertTrue(newRequests.isNotEmpty())
        assertTrue(newRequests.none { it.bearer.orEmpty().contains("old.example") })
        assertNull(newRequests.first { it.path == "/health" }.bearer)
        assertNull(newRequests.first { it.path.endsWith("/auth/device") }.bearer)
        assertTrue(newRequests.filter { it.path.endsWith("/state") }.all { it.bearer == "Bearer new.example-normal" })
        assertTrue(f.hub.logins.all { it["device_id"]?.jsonPrimitive?.content == oldId })
    }

    @Test
    fun failedHealthAndFailedAuthPreserveOldEndpointSessionAndCache() = runTest {
        val f = fixture()
        f.ready()
        val previous = f.repo.state.value
        val tokens = f.session.tokens.toMap()
        val cache = f.cache.values.toMap()
        for (address in listOf("bad-health.example", "bad-auth.example")) {
            val result = f.repo.connectToServer(address)
            assertFalse(result.first)
            assertEquals("https://old.example/", f.repo.serverAddress.value)
            assertEquals(previous, f.repo.state.value)
            assertEquals(tokens, f.session.tokens)
            assertEquals(cache, f.cache.values)
            assertNull(f.session.saved)
        }
    }

    @Test
    fun restartUsesSavedDebugEndpointBeforeAnyNetworkRequestAndReleaseIgnoresIt() = runTest {
        val f = fixture()
        f.ready()
        assertTrue(f.repo.connectToServer("new.example:8000").first)
        f.hub.calls.clear()
        val restarted = f.newRepo(debug = true)
        advanceUntilIdle()
        restarted.busy.first { !it }
        assertEquals("http://new.example:8000/", restarted.serverAddress.value)
        assertTrue(f.hub.calls.all { it.host == "new.example" })
        f.hub.calls.clear()
        val release = f.newRepo(debug = false)
        advanceUntilIdle()
        release.busy.first { !it }
        assertEquals("https://old.example/", release.serverAddress.value)
        assertFalse(release.canChangeServer)
        assertTrue(f.hub.calls.all { it.host == "old.example" })
        assertFalse(release.connectToServer("new.example:8000").first)
    }

    @Test
    fun sameEndpointReconnectKeepsDemoCacheAndFailedPresetIsNeverReplayed() = runTest {
        val f = fixture()
        f.ready()
        f.repo.startDemo(true)
        f.repo.leaveDemo()
        val demoCache = f.cache.values[2]
        val demoToken = f.session.tokens["demo"]
        assertTrue(f.repo.connectToServer("https://old.example/").first)
        assertEquals(demoCache, f.cache.values[2])
        assertEquals(demoToken, f.session.tokens["demo"])
        assertNull(f.hub.logins.last()["demo_preset"])
    }

    @Test
    fun switchingContinuesAfterSettingsViewModelLeavesItsScreen() = runTest {
        val f = fixture()
        f.ready()
        f.hub.pauseNewLogin = true
        val caller = launch { f.repo.connectToServer("new.example") }
        f.hub.newLoginStarted.await()
        caller.cancelAndJoin()
        f.hub.resumeNewLogin.complete(Unit)
        f.repo.busy.first { !it }
        assertEquals("http://new.example:8000/", f.repo.serverAddress.value)
        assertTrue(f.repo.state.value.online)
        assertEquals(20, f.repo.state.value.plan.freeCoins)
    }

    private fun TestScope.fixture(): Fixture = Fixture(StandardTestDispatcher(testScheduler), this)
}

private class Fixture(val dispatcher: CoroutineDispatcher, val scope: TestScope) {
    val session = ConnectionSession()
    val cache = ConnectionCache()
    val hub = EndpointHub()
    fun newRepo(debug: Boolean) = FinikRepository(cache, hub::create, session, DeviceIdProvider(session),
        NetworkConfig("https://old.example/", allowInsecure = debug, canChangeServer = debug), dispatcher)
    val repo = newRepo(true)
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun ready() { scope.advanceUntilIdle(); repo.busy.first { !it } }
}
private class ConnectionSession : PlayerSession {
    var active = "normal"
    val tokens = mutableMapOf<String, String>()
    var id = "finik-connection-test-device"
    var saved: String? = null
    var bound: String? = null
    override suspend fun mode() = active
    override suspend fun token() = tokens[active]
    override suspend fun activate(mode: String, token: String) { active = mode; tokens[mode] = token }
    override suspend fun clearToken() { tokens.remove(active) }
    override suspend fun saveToken(value: String) { tokens[active] = value }
    override suspend fun deviceId() = id
    override suspend fun saveDeviceId(value: String) { id = value }
    override suspend fun serverAddress() = saved
    override suspend fun boundServer() = bound
    override suspend fun saveServerAddress(address: String) { saved = address }
    override suspend fun selectServer(address: String, saveOverride: Boolean) {
        bound = address
        if (saveOverride) saved = address
        tokens.clear()
    }
}
private class ConnectionCache : SnapshotStore {
    val values = mutableMapOf<Int, String>()
    override suspend fun read(profileId: Int) = values[profileId]
    override suspend fun write(profileId: Int, json: String) { values[profileId] = json }
    override suspend fun clear(profileId: Int) { values.remove(profileId) }
}
private data class EndpointCall(val host: String, val path: String, val bearer: String?)
private class EndpointHub {
    val calls = java.util.Collections.synchronizedList(mutableListOf<EndpointCall>())
    val logins = java.util.Collections.synchronizedList(mutableListOf<JsonObject>())
    var pauseNewLogin = false
    val newLoginStarted = CompletableDeferred<Unit>()
    val resumeNewLogin = CompletableDeferred<Unit>()
    fun create(address: String, provider: suspend () -> String?): FinikApi = createFinikApi(address, provider, MockEngine { req ->
        val host = req.url.host
        val path = req.url.encodedPath
        val bearer = req.headers[HttpHeaders.Authorization]
        calls += EndpointCall(host, path, bearer)
        val mode = if (bearer?.endsWith("-demo") == true) "demo" else "normal"
        var code = HttpStatusCode.OK
        val body = when {
            path == "/health" -> if (host == "bad-health.example") { code = HttpStatusCode.ServiceUnavailable; "{}" } else """{"status":"ok"}"""
            path.endsWith("/auth/device") -> {
                val json = FinikJson.json.parseToJsonElement((req.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject
                logins += json
                if (host == "new.example" && pauseNewLogin) { newLoginStarted.complete(Unit); resumeNewLogin.await() }
                if (host == "bad-auth.example") { code = HttpStatusCode.Unauthorized; "{}" }
                else {
                    val requestedMode = json.getValue("mode").jsonPrimitive.content
                    """{"token":"$host-$requestedMode","has_pet":true,"mode":"$requestedMode","timezone":"UTC"}"""
                }
            }
            path.endsWith("/state") -> state(host, mode)
            path.endsWith("/profile") -> """{"weekly_income":40,"state":${state(host, mode)}}"""
            path.endsWith("/weeks/history") -> "{}"
            path.endsWith("/events/today") -> "null"
            path.endsWith("/ai/word-of-day") -> """{"word":"Budget","meaning":"Plan"}"""
            path.endsWith("/ai/remark") -> """{"text":"Hello"}"""
            else -> "[]"
        }
        respond(body, code, headersOf(HttpHeaders.ContentType, "application/json"))
    })
    private fun state(host: String, mode: String) = """{
        "free_coins":${if (host == "new.example") 20 else 10},"weekly_income":40,"mode":"$mode",
        "pet":{"name":"Owl","species":"OWL","xp":0},
        "week":{"number":1,"day":1,"income":40,"entries":[]},
        "goal":{"catalog_slug":"sunny_window","title":"Home","target":150,"saved":0},
        "timezone":"UTC","can_advance_time":${mode == "demo"}
    }"""
}
