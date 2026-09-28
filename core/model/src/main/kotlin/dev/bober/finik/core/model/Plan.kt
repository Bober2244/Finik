package dev.bober.finik.core.model

/** Строка недельного плана: сколько заложено и сколько уже потрачено по статье. */
data class PlanEntry(
    val category: SpendCategory,
    val planned: Int,
    val spent: Int,
) {
    val left: Int get() = planned - spent
}

data class WeekPlan(
    val entries: List<PlanEntry>,
    val freeCoins: Int,
    val weeklyIncome: Int,
) {
    val total: Int get() = entries.sumOf { it.planned }

    fun percentOf(category: SpendCategory): Int {
        val entry = entries.first { it.category == category }
        return if (weeklyIncome <= 0) 0 else (entry.planned * 100f / weeklyIncome).toInt()
    }

    fun entry(category: SpendCategory): PlanEntry = entries.first { it.category == category }
}

data class CareAction(
    val label: String,
    val category: SpendCategory,
    val cost: Int,
)

data class StreakDay(val label: String, val done: Boolean)
