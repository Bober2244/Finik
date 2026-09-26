package dev.bober.finik.core.data.snapshot

import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.data.game.GameEngine
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetEyeColor
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class AnimalSnapshotTest {
    private val catalog = ContentCatalog()
    private fun fresh() = GameEngine.createProfile(
        name = "Друг",
        species = PetSpecies.OWL,
        potStyle = PetPotStyle.CLAY,
        income = 40,
        shop = catalog.shop,
        goals = catalog.goals,
        tasks = catalog.tasks,
        epochDay = 10,
    )

    @Test
    fun allLegacySpeciesMigrateToOwlWithoutLosingProgress() {
        listOf("CAT", "FINIK", "OWL", "CACTUS", "DOG", "SPARK").forEach { legacy ->
            val original = fresh().let { it.copy(pet = it.pet.copy(totalXp = 255)) }
            val fields = Json.parseToJsonElement(original.encode()).jsonObject.toMutableMap()
            fields["species"] = JsonPrimitive(legacy)
            fields["potStyle"] = JsonPrimitive("SKY")
            fields["accessory"] = JsonPrimitive("scarf")
            fields.remove("furColor")
            fields.remove("eyeColor")
            fields.remove("accessories")
            val decoded = decodeSnapshot(JsonObject(fields).toString(), catalog)
            assertEquals(PetSpecies.OWL, decoded.pet.species)
            assertEquals("Друг", decoded.pet.name)
            assertEquals(255, decoded.pet.totalXp)
            assertEquals("Подросток", decoded.pet.stage.name)
            assertEquals(PetFurColor.SNOWY_WHITE, decoded.pet.appearance.furColor)
            assertEquals(setOf(PetAccessory.BANDANA), decoded.pet.appearance.accessories)
            assertEquals(original.plan, decoded.plan)
            assertEquals(original.selectedGoalId, decoded.selectedGoalId)
        }
    }

    @Test
    fun owlKeepsColorsAndAccessoryCombinationsAcrossRestart() {
        listOf(emptySet(), setOf(PetAccessory.HAT, PetAccessory.MEDAL, PetAccessory.BACKPACK)).forEach { equipped ->
            val appearance = PetAppearance(PetFurColor.NIGHT_PURPLE, PetEyeColor.AMBER, equipped)
            val original = fresh().let { it.copy(pet = it.pet.copy(appearance = appearance)) }
            val restored = decodeSnapshot(original.encode(), catalog)
            assertEquals(PetSpecies.OWL, restored.pet.species)
            assertEquals(appearance, restored.pet.appearance)
        }
    }

    @Test
    fun owlUsesExistingBackendIdentifier() {
        assertEquals(listOf("CACTUS"), PetSpecies.entries.map { it.legacyApiName })
        listOf("CAT", "FINIK", "OWL", "CACTUS", "DOG", "SPARK").forEach {
            assertEquals(PetSpecies.OWL, PetSpecies.fromStored(it))
        }
        assertEquals(PetFurColor.BLUE, PetFurColor.fromStored("NATURAL"))
        assertEquals(PetFurColor.DESERT_SAND, PetFurColor.fromStored("CREAM"))
        assertEquals(PetFurColor.SNOWY_WHITE, PetFurColor.fromStored("SILVER"))
        assertEquals(PetAccessory.HAT, PetAccessory.fromStored("cap"))
        assertEquals(PetAccessory.MEDAL, PetAccessory.fromStored("bow"))
    }
}
