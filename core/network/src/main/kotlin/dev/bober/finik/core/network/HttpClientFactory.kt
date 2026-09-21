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
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

data class NetworkConfig(val baseUrl: String) {
    companion object {
        const val EMULATOR_BASE_URL = "http://100.127.197.56:8000/"

        fun normalize(url: String): String {
            val trimmed = url.trim()
            return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        }
    }
}

object HttpClientFactory {
    fun create(
        baseUrl: String,
        tokenProvider: suspend () -> String?,
        engine: HttpClientEngine = OkHttp.create(),
    ): HttpClient = HttpClient(engine) {
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
        }
        install(finikBearerPlugin(tokenProvider))
        defaultRequest {
            url(NetworkConfig.normalize(baseUrl))
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
}

fun createFinikApi(
    baseUrl: String,
    tokenProvider: suspend () -> String?,
    engine: HttpClientEngine = OkHttp.create(),
): FinikApi = FinikApi(HttpClientFactory.create(baseUrl, tokenProvider, engine))

private fun finikBearerPlugin(tokenProvider: suspend () -> String?) =
    createClientPlugin("FinikBearer") {
        on(Send) { request ->
            val url = request.url.buildString()
            if (!url.contains("/auth/device")) {
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
