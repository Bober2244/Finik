package dev.bober.finik.core.model

data class WordOfDay(
    val word: String,
    val meaning: String,
    val example: String = "",
    val source: String = "fallback",
)

data class DreamStep(
    val title: String,
    val coins: Int,
)

data class DreamPlan(
    val title: String,
    val remain: Int,
    val weeklySave: Int,
    val weeksLeft: Int,
    val steps: List<DreamStep> = emptyList(),
    val advice: String = "",
    val source: String = "fallback",
    val cutPlay: Int = 0,
    val fasterSave: Int = 0,
    val weeksSaved: Int = 0,
)

data class OriginStory(
    val text: String,
    val source: String = "fallback",
)

data class AiQuizQuestion(
    val index: Int,
    val question: String,
    val options: List<String>,
)

data class AiQuiz(
    val kind: String,
    val source: String = "fallback",
    val rewarded: Boolean = false,
    val questions: List<AiQuizQuestion> = emptyList(),
)

data class AiQuizAnswer(
    val correct: Boolean,
    val explanation: String,
    val coins: Int = 0,
    val rewarded: Boolean = false,
)

data class ChatReply(
    val text: String,
    val source: String = "fallback",
    val blocked: Boolean = false,
    val chatLeft: Int? = null,
)

data class TodayEvent(
    val id: String,
    val slug: String,
    val title: String,
    val text: String,
    val day: Int = 1,
    val options: List<EventOption> = emptyList(),
    val chosen: String? = null,
)

data class EventOption(
    val key: String,
    val label: String,
)

data class EventChoice(
    val note: String,
    val coinsDelta: Int = 0,
    val xpDelta: Int = 0,
    val needChanges: Map<SpendCategory, Int> = emptyMap(),
    val spent: Map<SpendCategory, Int> = emptyMap(),
)

data class LessonAnswer(
    val correct: Boolean,
    val explanation: String,
    val lessonDone: Boolean = false,
    val answered: Int = 0,
    val total: Int = 0,
)

data class DiaryEntry(
    val text: String,
    val source: String = "fallback",
    val weekNumber: Int? = null,
)
