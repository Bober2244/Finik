package dev.bober.finik.core.data

import dev.bober.finik.core.model.*
import dev.bober.finik.core.network.FinikJson
import dev.bober.finik.core.network.dto.*
import org.junit.Assert.*
import org.junit.Test

class ServerCacheTest {
    private val goals = listOf(
        GoalOut(title = "Домик", target = 150, saved = 30, catalogSlug = "sunny_window"),
        GoalOut(title = "Прогулки", target = 80, saved = 12, catalogSlug = "watering_kit"),
        GoalOut(title = "Площадка", target = 120, saved = 0, catalogSlug = "play_garden"),
    )
    private val state = StateOut(
        freeCoins = 7, weeklyIncome = 40,
        pet = PetOut(name = "Сова", species = "OWL", xp = 115, lookVariant = 5,
            accessories = listOf("bandana", "backpack"), needs = listOf(NeedOut("FOOD", 48))),
        week = WeekOut(number = 4, day = 3, income = 40, planConfirmed = true,
            startsAt = "2026-09-27T21:00:00Z", endsAt = "2026-10-04T21:00:00Z",
            entries = listOf(WeekEntryOut("SAVE", planned = 20, spent = 8, left = 12))),
        goal = goals[1], goals = goals, timezone = "Europe/Moscow", mode = "normal",
    )

    @Test
    fun cacheRoundTripPreservesServerCatalogsBalancesAndAppearanceWithoutLocalRewards() {
        val cache = ServerCache(
            state = state, cachedAt = "2026-09-29T10:00:00Z",
            tasks = listOf(TaskOut("adventure_market", "Экспедиция", activity = "ADVENTURE", progress = 2, goal = 5)),
            shop = listOf(ShopItemOut("water_3d", "Вода", "WATER", cost = 6)),
            history = HistoryOut(lastStory = null),
            profile = ProfileOut(weeklyIncome = 60, earnedTotal = 120),
        )
        val restored = FinikJson.json.decodeFromString<ServerCache>(FinikJson.json.encodeToString(ServerCache.serializer(), cache))
            .toSnapshot(SampleData.snapshot, online = false)
        assertFalse(restored.online)
        assertEquals(7, restored.plan.freeCoins)
        assertEquals(listOf(30, 12, 0), restored.goals.map { it.saved })
        assertEquals("watering_kit", restored.selectedGoalId)
        assertEquals(PetFurColor.SNOWY_WHITE, restored.pet.appearance.furColor)
        assertEquals(setOf(PetAccessory.BANDANA, PetAccessory.BACKPACK), restored.pet.appearance.accessories)
        assertEquals(listOf("water_3d"), restored.shop.map { it.id })
        assertEquals("ADVENTURE", restored.tasks.single().activity)
        assertEquals(115, restored.pet.totalXp)
        assertEquals(60, restored.nextWeekIncome)
        assertEquals(4, restored.weekNumber)
        assertFalse(restored.canAdvanceTime)
        assertNull(restored.report)
    }

    @Test
    fun newServerGoalsReplaceAllPriorBalancesAndSelectedGoal() {
        val previous = SampleData.snapshot.copy(goals = SampleData.goals.map { it.copy(saved = 999) })
        val mapped = state.toSnapshot(previous)
        assertEquals(listOf(30, 12, 0), mapped.goals.map { it.saved })
        assertEquals(3, mapped.goals.size)
        assertEquals("watering_kit", mapped.selectedGoalId)
    }

    @Test
    fun numericQuestionsKeepIdentifierAndDoNotRevealCorrectAnswer() {
        val q = QuestionOut("need_count", question = "Сколько?", activity = "COINS", scene = "market").toModel()
        assertEquals("need_count", q.slug)
        assertEquals("COINS", q.activity)
        assertEquals(-1, q.rightIndex)
    }
}
