package dev.bober.finik.core.data.game

import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SpendCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
