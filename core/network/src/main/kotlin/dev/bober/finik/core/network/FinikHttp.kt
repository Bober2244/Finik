package dev.bober.finik.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object FinikHttp {
    const val BASE_URL = "https://finik.app/api/"

    fun jsonConfig(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun client(baseUrl: String = BASE_URL): HttpClient = HttpClient(OkHttp) {
        val mapper = jsonConfig()
        install(ContentNegotiation) { json(mapper) }
        install(HttpTimeout) {
            requestTimeoutMillis = 4_000
            connectTimeoutMillis = 4_000
        }
        install(Logging) { level = LogLevel.NONE }
        defaultRequest { url(baseUrl) }
        expectSuccess = false
    }
}
