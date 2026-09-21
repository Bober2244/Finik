package dev.bober.finik.core.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

class FinikApiException(
    val code: String,
    override val message: String,
    val httpStatus: Int = 0,
) : Exception(message)

@Serializable
private data class ErrorEnvelope(
    val error: ErrorBody? = null,
    val detail: JsonElement? = null,
)

@Serializable
private data class ErrorBody(
    val code: String = "",
    val message: String = "",
)

suspend fun HttpResponse.throwIfError() {
    if (status.isSuccess()) return
    val raw = runCatching { bodyAsText() }.getOrDefault("")
    val envelope = raw.takeIf { it.isNotBlank() }?.let {
        runCatching { FinikJson.json.decodeFromString<ErrorEnvelope>(it) }.getOrNull()
    }
    val parsed = envelope?.error
    val message = parsed?.message?.takeIf { it.isNotBlank() } ?: fallbackMessage(status.value)
    val code = parsed?.code?.takeIf { it.isNotBlank() } ?: fallbackCode(status.value)
    throw FinikApiException(code = code, message = message, httpStatus = status.value)
}

fun Throwable.userMessage(): String = when (this) {
    is FinikApiException -> message
    else -> "Нет сети. Попробуй ещё раз."
}

private fun fallbackCode(status: Int): String = when (status) {
    401 -> "unauthorized"
    404 -> "not_found"
    409 -> "conflict"
    422 -> "rule_violation"
    in 500..599 -> "server_error"
    else -> "error"
}

private fun fallbackMessage(status: Int): String = when (status) {
    401 -> "Токен недействителен, войди заново"
    404 -> "Не найдено"
    409 -> "Так уже нельзя"
    422 -> "Так нельзя по правилам игры."
    in 500..599 -> "Сервер занят. Попробуй чуть позже."
    else -> "Не получилось. Попробуй ещё раз."
}
