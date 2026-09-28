package dev.bober.finik.core.data.game

import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TransactionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val catalog = ContentCatalog()

    private fun fresh() = GameEngine.createProfile(
        name = "Финик",
        species = PetSpecies.OWL,
        potStyle = PetPotStyle.CLAY,
        income = 40,
        shop = catalog.shop,
        goals = catalog.goals,
        tasks = catalog.tasks,
        epochDay = 10,
    )

    @Test
    fun planCannotExceedIncome() {
        var state = fresh()
        state = GameEngine.changePlan(state, SpendCategory.FOOD, 30).state
        val over = GameEngine.changePlan(state, SpendCategory.PLAY, 20)
        assertTrue(over.warning)
        assertEquals(30, over.state.plan.entry(SpendCategory.FOOD).planned)
        assertEquals(10, over.state.plan.freeCoins)
    }

    @Test
    fun percentUsesIncomeEvenBeforeAllCoinsAreAllocatedAndAdviceMatchesLabels() {
        val partial = GameEngine.changePlan(fresh(), SpendCategory.SAVE, 8).state
        assertEquals(20, partial.plan.percentOf(SpendCategory.SAVE))

        val advised = GameEngine.applyAdvice(partial).state
        assertEquals(16, advised.plan.entry(SpendCategory.FOOD).planned)
        assertEquals(12, advised.plan.entry(SpendCategory.WATER).planned)
        assertEquals(4, advised.plan.entry(SpendCategory.PLAY).planned)
        assertEquals(8, advised.plan.entry(SpendCategory.SAVE).planned)
        assertEquals(0, advised.plan.freeCoins)
    }

    @Test
    fun extraIncomeRemainsAvailableThroughAdviceAndPlanChanges() {
        var state = GameEngine.parentBonus(fresh(), 10).state
        state = GameEngine.applyAdvice(state).state
        assertEquals(50, state.plan.total)
        assertEquals(8, state.plan.entry(SpendCategory.SAVE).planned)
        assertEquals(20, state.plan.percentOf(SpendCategory.SAVE))
        state = GameEngine.changePlan(state, SpendCategory.PLAY, -2).state
        assertEquals(2, state.plan.freeCoins)
        assertEquals(50, state.plan.total + state.plan.freeCoins)
    }

    @Test
    fun buyWithoutFundsIsBlocked() {
        var state = GameEngine.applyAdvice(fresh()).state
        state = GameEngine.confirmPlan(state).state
        val feed = catalog.shop.first { it.id == "s3" }
        repeat(3) { state = GameEngine.buy(state, "s2").state }
        val check = GameEngine.checkBuy(state, feed)
        assertFalse(check.allowed)
        assertTrue(check.message.contains("Не хватает") || check.message.contains("статьи"))
    }

    @Test
    fun buyDecreasesCategoryAndNeverGoesNegative() {
        var state = GameEngine.applyAdvice(fresh()).state
        state = GameEngine.confirmPlan(state).state
        val before = state.plan.entry(SpendCategory.FOOD).left
        state = GameEngine.buy(state, "s2").state
        val after = state.plan.entry(SpendCategory.FOOD).left
        assertEquals(before - 6, after)
        assertTrue(after >= 0)
    }

    @Test
    fun depositIncreasesSavings() {
        var state = GameEngine.applyAdvice(fresh()).state
        state = GameEngine.confirmPlan(state).state
        state = GameEngine.deposit(state, 8).state
        assertEquals(8, state.selectedGoal.saved)
        assertEquals(0, state.plan.entry(SpendCategory.SAVE).left)
    }

    @Test
    fun idleWeekCannotGrowPetOrCompletePlanBadge() {
        var state = GameEngine.confirmPlan(GameEngine.applyAdvice(fresh()).state).state
        state = GameEngine.closeWeek(state).state
        assertEquals(0, state.pet.totalXp)
        assertEquals(0, state.weeksOnTrack)
        assertEquals(80, state.earnedTotal)
        assertEquals(2, state.transactions.count { it.kind == TransactionKind.INCOME })
        assertEquals(0, state.badges.first { it.name == "Без долгов" }.percent)
    }

    @Test
    fun purchasesAndSavingsAreItemizedAndForecastUsesCompletedNetContributions() {
        var state = GameEngine.confirmPlan(GameEngine.applyAdvice(fresh()).state).state
        state = GameEngine.buy(state, "s2").state
        state = GameEngine.care(state, SpendCategory.WATER).state
        state = GameEngine.deposit(state, 8).state
        state = GameEngine.withdraw(state, 2).state
        assertNull(GameEngine.weeksToGoal(state, state.selectedGoal))
        val spendingAndSavings = state.transactions.filter { it.kind != TransactionKind.INCOME }
        assertEquals(listOf(6, 2, 8, 2), spendingAndSavings.map { it.amount })
        assertEquals(
            listOf(TransactionKind.PURCHASE, TransactionKind.CARE, TransactionKind.DEPOSIT, TransactionKind.WITHDRAWAL),
            spendingAndSavings.map { it.kind },
        )

        state = GameEngine.closeWeek(state).state
        assertEquals(6, state.history.single().value)
        assertEquals(6, state.report!!.rows.first { it.category == SpendCategory.SAVE }.actual)
        assertEquals(6.0, GameEngine.averageActualWeeklyContribution(state, state.selectedGoalId)!!, 0.001)
        assertEquals(24, GameEngine.weeksToGoal(state, state.selectedGoal))
        state = GameEngine.closeWeek(GameEngine.confirmPlan(GameEngine.applyAdvice(state).state).state).state
        assertEquals(3.0, GameEngine.averageActualWeeklyContribution(state, state.selectedGoalId)!!, 0.001)
        assertEquals(48, GameEngine.weeksToGoal(state, state.selectedGoal))
    }

    @Test
    fun realActionTasksCompleteOnceAndPayIntoLedger() {
        var state = GameEngine.confirmPlan(GameEngine.applyAdvice(fresh()).state).state
        assertFalse(state.tasks.first { it.id == "action-plan" }.done)
        state = GameEngine.buy(state, "s2").state
        assertTrue(state.tasks.first { it.id == "action-buy" }.done)
        val afterFirstBuy = state.earnedTotal
        state = GameEngine.buy(state, "s2").state
        assertEquals(afterFirstBuy, state.earnedTotal)
        state = GameEngine.deposit(state, 5).state
        assertTrue(state.tasks.first { it.id == "action-save" }.done)
        state = GameEngine.closeWeek(state).state
        assertTrue(state.tasks.first { it.id == "action-plan" }.done)
        assertEquals(3, state.transactions.count { it.source.startsWith("Задание:") })
    }

    @Test
    fun incorrectTaskAnswerCanBeRetriedWithoutReward() {
        val state = fresh()
        val task = state.tasks.first()
        val wrong = GameEngine.completeTask(state, task.id, correct = false, reward = task.reward)
        assertTrue(wrong.warning)
        assertEquals(state, wrong.state)
        val correct = GameEngine.completeTask(wrong.state, task.id, correct = true, reward = task.reward)
        assertTrue(correct.state.tasks.first { it.id == task.id }.done)
        assertEquals(40 + task.reward, correct.state.earnedTotal)
    }

    @Test
    fun sevenDayRewardIsPaidOncePerSevenConsecutiveOpens() {
        var state = fresh()
        (11L..16L).forEach { day -> state = GameEngine.dailyOpen(state, day).state }
        assertEquals(7, state.consecutiveOpenDays)
        assertEquals(45, state.earnedTotal)
        state = GameEngine.dailyOpen(state, 17).state
        assertEquals(8, state.consecutiveOpenDays)
        assertEquals(45, state.earnedTotal)
        assertEquals(1, state.streak.count { it.done })
        (18L..23L).forEach { day -> state = GameEngine.dailyOpen(state, day).state }
        assertEquals(50, state.earnedTotal)
    }

    @Test
    fun savingsBadgeRequiresActualTwentyPercentAndStaysEarned() {
        var state = GameEngine.confirmPlan(GameEngine.applyAdvice(fresh()).state).state
        state = GameEngine.deposit(state, 7).state
        state = GameEngine.closeWeek(state).state
        assertFalse(state.badges.first { it.name == "Двадцать процентов" }.isDone)
        assertEquals(0, state.qualifiedSavingsWeeks)

        state = GameEngine.confirmPlan(GameEngine.applyAdvice(state).state).state
        state = GameEngine.deposit(state, 8).state
        state = GameEngine.closeWeek(state).state
        assertEquals(1, state.qualifiedSavingsWeeks)
        assertTrue(state.badges.first { it.name == "Двадцать процентов" }.isDone)

        state = GameEngine.confirmPlan(GameEngine.applyAdvice(state).state).state
        state = GameEngine.closeWeek(state).state
        assertTrue(state.badges.first { it.name == "Двадцать процентов" }.isDone)
    }

    @Test
    fun fiveDemoPeriodsAdvanceStageProgress() {
        var state = GameEngine.applyAdvice(fresh()).state
        state = GameEngine.confirmPlan(state).state
        repeat(5) {
            state = GameEngine.care(state, SpendCategory.FOOD).state
            state = GameEngine.care(state, SpendCategory.WATER).state
            state = GameEngine.deposit(state, state.plan.entry(SpendCategory.SAVE).left).state
            state = GameEngine.closeWeek(state).state
            state = GameEngine.applyAdvice(state).state
            state = GameEngine.confirmPlan(state).state
        }
        assertEquals(5, state.weeksDone)
        assertTrue(state.pet.totalXp > 0)
        assertTrue(state.history.size == 5)
    }
}
