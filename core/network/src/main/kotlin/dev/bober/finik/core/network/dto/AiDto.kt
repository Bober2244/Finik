package dev.bober.finik.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RemarkOut(
    val text: String,
    val mood: String = "OKAY",
    val source: String = "fallback",
)

@Serializable
data class WeekSummaryOut(
    val text: String,
    val source: String = "fallback",
    val weekNumber: Int? = null,
)

@Serializable
data class WordOfDayOut(
    val word: String,
    val meaning: String = "",
    val example: String = "",
    val source: String = "fallback",
)

@Serializable
data class DiaryOut(
    val text: String,
    val source: String = "fallback",
    val weekNumber: Int? = null,
)

@Serializable
data class DreamPlanOut(
    val title: String = "",
    val remain: Int = 0,
    val weeklySave: Int = 0,
    val weeksLeft: Int = 0,
    val steps: List<DreamStepOut> = emptyList(),
    val advice: String = "",
    val source: String = "fallback",
    val cutPlay: Int = 0,
    val fasterSave: Int = 0,
    val weeksSaved: Int = 0,
)

@Serializable
data class DreamStepOut(
    val title: String,
    val coins: Int = 0,
)

@Serializable
data class OriginOut(
    val text: String,
    val source: String = "fallback",
)

@Serializable
data class QuizOut(
    val kind: String = "quiz",
    val source: String = "fallback",
    val rewarded: Boolean = false,
    val questions: List<QuizQuestionOut> = emptyList(),
)

@Serializable
data class QuizQuestionOut(
    val index: Int = 0,
    val question: String,
    val options: List<String> = emptyList(),
)

@Serializable
data class QuizAnswerIn(
    val index: Int,
    val answerIndex: Int,
    val kind: String = "quiz",
)

@Serializable
data class QuizAnswerOut(
    val correct: Boolean = false,
    val explanation: String = "",
    val coins: Int = 0,
    val rewarded: Boolean = false,
)

@Serializable
data class ChatIn(
    val text: String,
)

@Serializable
data class ChatOut(
    val text: String,
    val source: String = "fallback",
    val blocked: Boolean = false,
    val chatLeft: Int? = null,
)
