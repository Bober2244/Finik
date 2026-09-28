package dev.bober.finik.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
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
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.net.URI

data class NetworkConfig(val baseUrl: String) {
    // Network content is optional, so a bad build-time address must not prevent offline play.
    val normalizedBaseUrl: String = runCatching { normalize(baseUrl) }.getOrDefault("")
    val isConfigured: Boolean get() = normalizedBaseUrl.isNotEmpty()
    val isSecure: Boolean get() = normalizedBaseUrl.startsWith("https://", ignoreCase = true)

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
                    uri.rawFragment == null,
            ) { "API base URL must be an absolute HTTP(S) URL without credentials, query or fragment" }
            return "${trimmed.trimEnd('/')}/"
        }
    }
}

object HttpClientFactory {
    fun create(
        baseUrl: String,
        tokenProvider: suspend () -> String?,
        engine: HttpClientEngine = OkHttp.create(),
    ): HttpClient {
        val serverUrl = NetworkConfig(baseUrl).normalizedBaseUrl
        val serverOrigin = serverUrl.takeIf { it.isNotEmpty() }?.let(::Url)
        return HttpClient(engine) {
            expectSuccess = false
            install(ContentNegotiation) { json(FinikJson.json) }
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
    engine: HttpClientEngine = OkHttp.create(),
): FinikApi = FinikApi(HttpClientFactory.create(baseUrl, tokenProvider, engine))

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
