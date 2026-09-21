package dev.bober.finik.core.model

enum class TaskKind(val label: String) {
    LESSON(label = "урок"),
    WEEK(label = "неделя"),
    HABIT(label = "привычка"),
    DAY(label = "день"),
}

enum class TaskTheme(val label: String) {
    PLAN(label = "план"),
    SAVE(label = "копилка"),
    BUY(label = "покупки"),
}

/** Куда ведёт задание, если его нельзя выполнить на месте. */
enum class TaskTarget { QUIZ, PLAN, GOAL, SHOP, SCENARIO }

data class TaskItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: TaskKind,
    val reward: Int,
    val done: Boolean,
    val target: TaskTarget? = null,
    val theme: TaskTheme = TaskTheme.PLAN,
    val progress: Int = 0,
    val goalCount: Int = 0,
    val rewarded: Boolean = false,
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val rightIndex: Int,
    val explanation: String,
)

data class ScenarioChoice(
    val label: String,
    val correct: Boolean,
    val explanation: String,
)

data class ScenarioTask(
    val id: String,
    val prompt: String,
    val choices: List<ScenarioChoice>,
    val quiz: List<QuizQuestion> = emptyList(),
)

data class GlossaryTerm(
    val term: String,
    val meaning: String,
    val example: String = "",
)
