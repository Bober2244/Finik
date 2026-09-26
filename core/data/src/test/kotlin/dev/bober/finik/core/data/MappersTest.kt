package dev.bober.finik.core.data

import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetEyeColor
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.network.FinikJson
import dev.bober.finik.core.network.dto.StateOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {
    @Test
    fun stateOutMapsIntoSnapshot() {
        val out = FinikJson.json.decodeFromString(StateOut.serializer(), STATE_JSON)
        val snapshot: GameSnapshot = out.toSnapshot(SampleData.snapshot.copy(onboarded = false))
        assertTrue(snapshot.onboarded)
        assertTrue(snapshot.online)
        assertEquals("Кустик", snapshot.pet.name)
        assertEquals(16, snapshot.plan.entry(SpendCategory.FOOD).planned)
        assertEquals("sunny_window", snapshot.selectedGoalId)
        assertEquals(40, snapshot.lastIncome)
        assertEquals(PetSpecies.OWL, snapshot.pet.species)
        assertEquals("Напоить", snapshot.care.first().label)
        assertEquals("Уютный домик для питомца", snapshot.selectedGoal.title)
    }

    @Test
    fun remoteRefreshPreservesLocallySavedAnimalAppearance() {
        val appearance = PetAppearance(PetFurColor.DESERT_SAND, PetEyeColor.BLUE, PetAccessory.NONE)
        val previous = SampleData.snapshot.copy(pet = SampleData.pet.copy(appearance = appearance))
        val remote = FinikJson.json.decodeFromString(StateOut.serializer(), STATE_JSON)
        val refreshed = remote.toSnapshot(previous)
        assertEquals(appearance, refreshed.pet.appearance)
        assertEquals(remote.pet.xp, refreshed.pet.totalXp)
        assertEquals(remote.week.day, refreshed.pet.dayOfWeek)
    }

    @Test
    fun legacyTextMigrationLeavesAnimalStagesAndUserNamesIntact() {
        assertEquals("Питомец устал. Подросток хочет пить.", "Росток подвял. Подросток хочет пить.".animalText())
        assertEquals("Питомец, питомец; ПОДРОСТОК", "Росток, росток; ПОДРОСТОК".animalText())
        val remote = FinikJson.json.decodeFromString(StateOut.serializer(), STATE_JSON)
        val snapshot = remote.copy(pet = remote.pet.copy(name = "Росток", moodNote = "Ростку нужен свет.")).toSnapshot(SampleData.snapshot)
        assertEquals("Росток", snapshot.pet.name)
        assertEquals("Питомцу нужна забота.", snapshot.pet.moodNote)
    }

    @Test
    fun deviceIdMeetsServerLength() {
        val id = DeviceIdProvider.build("1a2b3c4d5e6f7890")
        assertTrue(id.startsWith("android-"))
        assertTrue(id.length in 8..128)
    }
}

private const val STATE_JSON = """
{
  "free_coins": 0,
  "weekly_income": 40,
  "pet": {
    "id": "11111111-1111-1111-1111-111111111111",
    "name": "Кустик",
    "species": "FINIK",
    "xp": 0,
    "stage_index": 0,
    "stage_name": "Семечко",
    "next_stage_xp": 100,
    "mood": "OKAY",
    "mood_note": "Мне нормально.",
    "needs": [{"category": "FOOD", "percent": 70}],
    "look_variant": 0,
    "equipped_pot": "",
    "equipped_accessory": ""
  },
  "week": {
    "id": "22222222-2222-2222-2222-222222222222",
    "number": 1,
    "day": 3,
    "income": 40,
    "overrun": 0,
    "plan_confirmed": false,
    "entries": [
      {"category": "FOOD", "planned": 16, "spent": 0, "left": 16},
      {"category": "WATER", "planned": 12, "spent": 0, "left": 12},
      {"category": "PLAY", "planned": 4, "spent": 0, "left": 4},
      {"category": "SAVE", "planned": 8, "spent": 0, "left": 8}
    ]
  },
  "goal": {
    "id": "33333333-3333-3333-3333-333333333333",
    "title": "Солнечное окно и большой горшок",
    "target": 150,
    "saved": 0,
    "percent": 0,
    "remain": 150,
    "catalog_slug": "sunny_window",
    "why": "Ростку нужен свет."
  },
  "care_actions": [
    {"category": "WATER", "label": "Полить", "cost": 2}
  ],
  "streak": {
    "count": 1,
    "goal": 7,
    "bonus": 5,
    "days": [{"label": "Пн", "done": true}]
  },
  "last_income": 40,
  "last_income_note": "Пришёл доход 40",
  "last_purchase_note": "",
  "last_purchase_amount": 0,
  "owned_cosmetics": []
}
"""
