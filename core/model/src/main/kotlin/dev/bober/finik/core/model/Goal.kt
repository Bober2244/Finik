package dev.bober.finik.core.model

data class SavingsGoal(
    val title: String,
    val target: Int,
    val saved: Int,
) {
    val percent: Int get() = (saved * 100f / target).let(Math::round)
}

/**
 * Столбик истории накоплений за неделю. `tone` подсказывает цвет: удачная или провальная неделя,
 * `barHeight` — высота столбика в dp из макета (пока данные статичные).
 */
data class HistoryWeek(val label: String, val value: Int, val tone: HistoryTone, val barHeight: Int)

enum class HistoryTone { LOW, MID, HIGH, BAD }

enum class LogTone { GOOD, NEUTRAL, BAD }

data class WeekLogEntry(
    val text: String,
    val delta: String,
    val tone: LogTone,
    val rounded: Boolean,
)

data class Badge(
    val name: String,
    val note: String,
    val percent: Int,
) {
    val isDone: Boolean get() = percent >= 100
}

/** Отчёт по неделе (вариант 2c макета). */
data class ReportRow(
    val category: SpendCategory,
    val planned: Int,
    val actual: Int,
) {
    val isOver: Boolean get() = actual > planned
    val progress: Float get() = (actual.toFloat() / planned).coerceIn(0f, 1f)
}
