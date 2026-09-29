package dev.bober.finik.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.net.URI
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.ConnectionPool

data class NetworkConfig(
    val baseUrl: String,
    val allowInsecure: Boolean = false,
    val canChangeServer: Boolean = false,
) {
    // An unavailable address still allows reading the last server snapshot.
    val normalizedBaseUrl: String = runCatching { normalize(baseUrl) }.getOrDefault("")
    val isConfigured: Boolean get() = normalizedBaseUrl.isNotEmpty()
    val isSecure: Boolean get() = normalizedBaseUrl.startsWith("https://", ignoreCase = true)

    val canConnect: Boolean get() = isConfigured && (isSecure || allowInsecure)

    companion object {
        fun normalize(url: String): String {
            val trimmed = url.trim()
            if (trimmed.isEmpty()) return ""
            val uri = runCatching { URI(trimmed) }.getOrNull()
            require(
                uri != null &&
                    (uri.scheme.equals("http", ignoreCase = true) ||
                        uri.scheme.equals("https", ignoreCase = true)) &&
                    !uri.host.isNullOrBlank() &&
                    uri.rawUserInfo == null &&
                    uri.rawQuery == null &&
                    uri.rawFragment == null &&
                    (uri.port == -1 || uri.port in 1..65535),
            ) { "API base URL must be an absolute HTTP(S) URL without credentials, query or fragment" }
            return "${trimmed.trimEnd('/')}/"
        }

        fun normalizeUserAddress(value: String): String {
            val text = value.trim()
            require(text.isNotEmpty()) { "Введи IP-адрес или адрес сервера." }
            val full = if (text.contains("://")) text else {
                val parsed = runCatching { URI("http://$text") }.getOrNull()
                require(parsed != null && !parsed.host.isNullOrBlank()) { "Проверь IP-адрес или имя сервера." }
                if (parsed.port == -1) {
                    "http://${parsed.rawAuthority}:8000${parsed.rawPath.orEmpty()}" +
                        (parsed.rawQuery?.let { "?$it" } ?: "") +
                        (parsed.rawFragment?.let { "#$it" } ?: "")
                } else "http://$text"
            }
            return try { normalize(full) }
            catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Нужен HTTP(S)-адрес без логина, пароля, параметров и фрагмента; порт — от 1 до 65535.")
            }
        }
    }
}

object HttpClientFactory {
    fun create(
        baseUrl: String,
        tokenProvider: suspend () -> String?,
        engine: HttpClientEngine = finikOkHttpEngine(),
    ): HttpClient {
        val serverUrl = NetworkConfig(baseUrl).normalizedBaseUrl
        val serverOrigin = serverUrl.takeIf { it.isNotEmpty() }?.let(::Url)
        return HttpClient(engine) {
            expectSuccess = false
            followRedirects = false
            install(ContentNegotiation) { json(FinikJson.json) }
            install(HttpRequestRetry) {
                maxRetries = 1
                retryIf { _, _ -> false }
                retryOnExceptionIf { request, cause ->
                    request.method == HttpMethod.Get && cause is IOException
                }
                delayMillis { 0 }
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 8_000
                requestTimeoutMillis = 20_000
                socketTimeoutMillis = 20_000
            }
            install(Logging) {
                logger = FinikNetLogger
                level = LogLevel.HEADERS
                sanitizeHeader { name ->
                    name.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                        name.equals(HttpHeaders.Cookie, ignoreCase = true) ||
                        name.equals(HttpHeaders.SetCookie, ignoreCase = true)
                }
            }
            install(finikBearerPlugin(tokenProvider, serverOrigin))
            defaultRequest {
                if (serverUrl.isNotEmpty()) url(serverUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }
}

fun createFinikApi(
    baseUrl: String,
    tokenProvider: suspend () -> String?,
    engine: HttpClientEngine = finikOkHttpEngine(),
): FinikApi = FinikApi(HttpClientFactory.create(baseUrl, tokenProvider, engine))

private fun finikOkHttpEngine() = OkHttp.create {
    config {
        // A POST with an uncertain response must never be replayed by OkHttp.
        retryOnConnectionFailure(false)
        // Do not reuse sockets that the API may have closed while the player was idle.
        // This also protects writes without adding any automatic write retry.
        connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
    }
}

private fun finikBearerPlugin(tokenProvider: suspend () -> String?, serverOrigin: Url?) =
    createClientPlugin("FinikBearer") {
        on(Send) { request ->
            val url = request.url.build()
            val origin = serverOrigin ?: error("API base URL is not configured")
            check(
                url.protocol == origin.protocol &&
                    url.host.equals(origin.host, ignoreCase = true) &&
                    url.port == origin.port,
            ) { "Request origin does not match configured API base URL" }
            if (!url.encodedPath.endsWith("/api/v1/auth/device")) {
                val token = tokenProvider()
                if (!token.isNullOrBlank() && request.headers[HttpHeaders.Authorization].isNullOrBlank()) {
                    request.headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            proceed(request)
        }
    }

internal object FinikNetLogger : Logger {
    override fun log(message: String) {
        try {
            android.util.Log.d("FinikNet", message)
        } catch (_: RuntimeException) {
            println("FinikNet $message")
        }
    }
}
