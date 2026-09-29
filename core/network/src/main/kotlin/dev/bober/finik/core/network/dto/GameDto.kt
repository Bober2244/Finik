package dev.bober.finik.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StateOut(
    val freeCoins: Int = 0,
    val weeklyIncome: Int = 40,
    val pet: PetOut,
    val week: WeekOut,
    val goal: GoalOut,
    val careActions: List<CareActionOut> = emptyList(),
    val streak: StreakOut = StreakOut(),
    val lastIncome: Int = 0,
    val lastIncomeNote: String = "",
    val lastPurchaseNote: String = "",
    val lastPurchaseAmount: Int = 0,
    val ownedCosmetics: List<String> = emptyList(),
    val goals: List<GoalOut> = emptyList(),
    val timezone: String = "UTC",
    val mode: String = "normal",
    val serverNow: String = "",
    val gameNow: String = "",
    val canAdvanceTime: Boolean = false,
)

@Serializable
data class PetOut(
    val id: String = "",
    val name: String,
    val species: String,
    val xp: Int = 0,
    val stageIndex: Int = 0,
    val stageName: String = "",
    val nextStageXp: Int? = null,
    val mood: String = "OKAY",
    val moodNote: String = "",
    val needs: List<NeedOut> = emptyList(),
    val lookVariant: Int = 0,
    val equippedPot: String = "",
    val equippedAccessory: String = "",
    val accessories: List<String> = emptyList(),
)

@Serializable
data class NeedOut(
    val category: String,
    val percent: Int = 0,
)

@Serializable
data class WeekOut(
    val id: String = "",
    val number: Int = 1,
    val day: Int = 1,
    val income: Int = 0,
    val overrun: Int = 0,
    val planConfirmed: Boolean = false,
    val entries: List<WeekEntryOut> = emptyList(),
    val startsAt: String? = null,
    val endsAt: String? = null,
)

@Serializable
data class WeekEntryOut(
    val category: String,
    val planned: Int = 0,
    val spent: Int = 0,
    val left: Int = 0,
)

@Serializable
data class GoalOut(
    val id: String = "",
    val title: String,
    val target: Int = 0,
    val saved: Int = 0,
    val percent: Int = 0,
    val remain: Int = 0,
    val catalogSlug: String = "",
    val why: String = "",
)

@Serializable
data class CareActionOut(
    val category: String,
    val label: String,
    val cost: Int = 0,
)

@Serializable
data class StreakOut(
    val count: Int = 0,
    val goal: Int = 7,
    val bonus: Int = 5,
    val days: List<StreakDayOut> = emptyList(),
)

@Serializable
data class StreakDayOut(
    val label: String,
    val done: Boolean = false,
)

@Serializable
data class PetIn(
    val name: String,
    val species: String,
    val weeklyIncome: Int,
    val goalSlug: String? = null,
    val lookVariant: Int = 0,
    val accessories: List<String> = emptyList(),
)

@Serializable
data class CustomizeIn(
    val pot: String? = null,
    val accessory: String? = null,
    val lookVariant: Int? = null,
    val accessories: List<String>? = null,
)

@Serializable
data class PlanIn(
    val food: Int,
    val water: Int,
    val play: Int,
    val save: Int,
)

@Serializable
data class CareIn(
    val category: String,
)

@Serializable
data class CareOut(
    val category: String,
    val cost: Int = 0,
    val restored: Int = 0,
    val xpGained: Int = 0,
    val fromSavings: Boolean = false,
    val state: StateOut,
)

@Serializable
data class GoalIn(
    val slug: String,
)

@Serializable
data class DepositIn(
    val amount: Int,
)

@Serializable
data class DayOut(
    val day: Int = 0,
    val xpGained: Int = 0,
    val weekFinished: Boolean = false,
    val weekReport: WeekReportOut? = null,
    val wilted: Boolean = false,
    val wiltXpLost: Int = 0,
    val state: StateOut,
)

@Serializable
data class WeekReportOut(
    val number: Int = 0,
    val saved: Int = 0,
    val overrun: Int = 0,
    val returnedCoins: Int = 0,
    val xpGained: Int = 0,
    val goalAchieved: Boolean = false,
    val interest: Int = 0,
    val cashback: Int = 0,
)

@Serializable
data class ShopItemOut(
    val slug: String,
    val name: String,
    val category: String,
    val cost: Int = 0,
    val oldCost: Int? = null,
    val salePercent: Int = 0,
    val glyph: String = "CIRCLE",
    val leftInCategory: Int = 0,
    val affordable: Boolean = true,
    val restore: Int = 0,
    val xpBonus: Int = 0,
    val kind: String = "NEED",
    val slot: String = "",
    val owned: Boolean = false,
    val equipped: Boolean = false,
)

@Serializable
data class BuyIn(
    val slug: String,
)

@Serializable
data class BuyOut(
    val slug: String = "",
    val name: String = "",
    val cost: Int = 0,
    val restored: Int = 0,
    val xpGained: Int = 0,
    val state: StateOut,
)

@Serializable
data class TaskOut(
    val slug: String,
    val title: String,
    val subtitle: String = "",
    val kind: String = "LESSON",
    val target: String? = null,
    val reward: Int = 0,
    val progress: Int = 0,
    val goal: Int = 0,
    val done: Boolean = false,
    val rewarded: Boolean = false,
    val activity: String = "QUIZ",
)

@Serializable
data class QuestionOut(
    val slug: String,
    val order: Int = 0,
    val question: String,
    val options: List<String> = emptyList(),
    val activity: String = "CHOICE",
    val scene: String = "",
)

@Serializable
data class AnswerIn(
    val questionSlug: String,
    val answerIndex: Int? = null,
    val answerValue: Int? = null,
)

@Serializable
data class AnswerOut(
    val correct: Boolean = false,
    val explanation: String = "",
    val lessonDone: Boolean = false,
    val answered: Int = 0,
    val total: Int = 0,
)

@Serializable
data class HistoryOut(
    val weeks: List<HistoryWeekOut> = emptyList(),
    val lastReport: List<ReportRowOut> = emptyList(),
    val lastSummary: String = "",
    val lastStory: String? = null,
    val lastIncome: Int = 0,
    val lastPurchaseNote: String = "",
)

@Serializable
data class HistoryWeekOut(
    val number: Int,
    val saved: Int = 0,
    val overrun: Int = 0,
    val income: Int = 0,
    val tone: String = "MID",
)

@Serializable
data class ReportRowOut(
    val category: String,
    val planned: Int = 0,
    val actual: Int = 0,
    val isOver: Boolean = false,
)

@Serializable
data class EventOut(
    val id: String,
    val slug: String,
    val title: String,
    val text: String,
    val day: Int = 1,
    val options: List<EventOptionOut> = emptyList(),
    val chosen: String? = null,
)

@Serializable
data class EventOptionOut(
    val key: String,
    val label: String,
)

@Serializable
data class ChooseIn(
    val option: String,
)

@Serializable
data class ChoiceOut(
    val note: String = "",
    val coinsDelta: Int = 0,
    val xpDelta: Int = 0,
    val needChanges: Map<String, Int> = emptyMap(),
    val spent: Map<String, Int> = emptyMap(),
    val state: StateOut,
)

@Serializable
data class DemoAdvanceIn(val days: Int? = null, val toWeekEnd: Boolean = false)
