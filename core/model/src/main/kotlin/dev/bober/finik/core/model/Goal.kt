package dev.bober.finik.core.model

data class SavingsGoal(
    val id: String = "g1",
    val title: String,
    val target: Int,
    val saved: Int,
) {
    val percent: Int get() = if (target <= 0) 0 else (saved * 100f / target).let(Math::round).coerceAtLeast(0)
    val remaining: Int get() = (target - saved).coerceAtLeast(0)
}

/**
 * Столбик истории накоплений за неделю. `tone` подсказывает цвет: удачная или провальная неделя,
 * `barHeight` — высота столбика в dp (считается из суммы).
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
    val progress: Float get() = if (planned <= 0) 0f else (actual.toFloat() / planned).coerceIn(0f, 1f)
}

data class WeekReport(
    val week: Int,
    val rows: List<ReportRow>,
    val summary: String,
    val note: String,
)
