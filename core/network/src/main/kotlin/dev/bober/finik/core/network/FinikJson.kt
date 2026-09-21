package dev.bober.finik.core.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/** JSON на проводе — snake_case, как отдаёт FastAPI. */
object FinikJson {
    @OptIn(ExperimentalSerializationApi::class)
    val json: Json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
    }
}
