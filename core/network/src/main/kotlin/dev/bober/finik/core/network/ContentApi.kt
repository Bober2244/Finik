package dev.bober.finik.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class RemoteTaskDto(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val reward: Int = 6,
    val prompt: String = "",
    val choices: List<RemoteChoiceDto> = emptyList(),
)

@Serializable
data class RemoteChoiceDto(
    val label: String,
    val correct: Boolean = false,
    val explanation: String = "",
)

@Serializable
data class RemoteContentDto(
    val tasks: List<RemoteTaskDto> = emptyList(),
)

/**
 * Учебный контент с сервера. Для конкурсного прототипа сервер не обязателен:
 * при ошибке сети или пустом ответе репозиторий остаётся на локальном каталоге.
 */
class ContentApi(private val client: HttpClient) {
    suspend fun fetch(): RemoteContentDto? = try {
        client.get("content/tasks.json").body()
    } catch (_: Exception) {
        null
    }
}
