package dev.bober.finik.core.model

enum class TaskKind(val label: String) {
    LESSON(label = "урок"),
    WEEK(label = "неделя"),
    HABIT(label = "привычка"),
    DAY(label = "день"),
}

/** Куда ведёт задание, если его нельзя выполнить на месте. */
enum class TaskTarget { QUIZ, PLAN, GOAL, SHOP }

data class TaskItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: TaskKind,
    val reward: Int,
    val done: Boolean,
    val target: TaskTarget? = null,
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val rightIndex: Int,
    val explanation: String,
)
