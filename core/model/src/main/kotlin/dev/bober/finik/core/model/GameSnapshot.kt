package dev.bober.finik.core.model

/**
 * Полный снимок игрового состояния. Экраны читают его, игровой движок
 * возвращает новый снимок после каждого действия.
 */
data class GameSnapshot(
    val onboarded: Boolean,
    val pet: PetProfile,
    val plan: WeekPlan,
    val planConfirmed: Boolean,
    val needs: List<NeedLevel>,
    val care: List<CareAction>,
    val streak: List<StreakDay>,
    val tasks: List<TaskItem>,
    val shop: List<ShopItem>,
    val goals: List<SavingsGoal>,
    val selectedGoalId: String,
    val history: List<HistoryWeek>,
    val weekLog: List<WeekLogEntry>,
    val badges: List<Badge>,
    val report: WeekReport?,
    val demoMode: Boolean,
    val soundOn: Boolean,
    val earnedTotal: Int,
    val weeksDone: Int,
    val caredWater: Boolean = false,
    val caredFood: Boolean = false,
    val caredPlay: Boolean = false,
    val saleBuys: Int = 0,
    val weeksOnTrack: Int = 0,
    val lastOpenEpochDay: Long = 0,
    val ready: Boolean = true,
) {
    val selectedGoal: SavingsGoal
        get() = goals.firstOrNull { it.id == selectedGoalId } ?: goals.first()

    val spendableFree: Int
        get() = if (planConfirmed) {
            plan.entries.filter { it.category != SpendCategory.SAVE }.sumOf { it.left.coerceAtLeast(0) }
        } else {
            plan.freeCoins
        }

    val activeTask: TaskItem? get() = tasks.firstOrNull { !it.done }
}

data class EngineResult(
    val state: GameSnapshot,
    val message: String,
    val warning: Boolean = false,
    val reportReady: Boolean = false,
)

data class BuyCheck(
    val allowed: Boolean,
    val message: String,
)
