package dev.bober.finik.core.data.snapshot

import dev.bober.finik.core.data.animalText
import dev.bober.finik.core.data.content.ContentCatalog
import dev.bober.finik.core.data.game.GameEngine
import dev.bober.finik.core.model.GameSnapshot
import dev.bober.finik.core.model.HistoryTone
import dev.bober.finik.core.model.HistoryWeek
import dev.bober.finik.core.model.LogTone
import dev.bober.finik.core.model.NeedLevel
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetEyeColor
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.PlanEntry
import dev.bober.finik.core.model.ReportRow
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.StreakDay
import dev.bober.finik.core.model.WeekLogEntry
import dev.bober.finik.core.model.WeekPlan
import dev.bober.finik.core.model.WeekReport
import dev.bober.finik.core.model.stageIndexFor
import dev.bober.finik.core.model.xpPercentInStage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun GameSnapshot.encode(): String = json.encodeToString(SnapshotDto.serializer(), toDto())

fun decodeSnapshot(raw: String, catalog: ContentCatalog): GameSnapshot =
    GameEngine.decorate(json.decodeFromString(SnapshotDto.serializer(), raw).toDomain(catalog))

@Serializable
internal data class SnapshotDto(
    val onboarded: Boolean,
    val petName: String,
    val species: String,
    val potStyle: String = "CLAY",
    val totalXp: Int,
    val mood: String,
    val moodNote: String,
    val dayOfWeek: Int,
    val weeklyIncome: Int,
    val freeCoins: Int,
    val planConfirmed: Boolean,
    val plan: List<PlanDto>,
    val needs: List<NeedDto>,
    val streak: List<Boolean>,
    val doneTaskIds: List<String>,
    val selectedGoalId: String,
    val goalSaved: Map<String, Int> = emptyMap(),
    val history: List<HistoryDto> = emptyList(),
    val weekLog: List<LogDto> = emptyList(),
    val report: ReportDto? = null,
    val demoMode: Boolean = true,
    val soundOn: Boolean = true,
    val earnedTotal: Int = 0,
    val weeksDone: Int = 0,
    val caredWater: Boolean = false,
    val caredFood: Boolean = false,
    val caredPlay: Boolean = false,
    val saleBuys: Int = 0,
    val weeksOnTrack: Int = 0,
    val lastOpenEpochDay: Long = 0,
    val lookVariant: Int = 0,
    val equippedPot: String = "",
    val equippedAccessory: String = "",
    val furColor: String? = null,
    val eyeColor: String? = null,
    val accessory: String? = null,
    /** Null means a legacy snapshot that stored only one accessory. */
    val accessories: List<String>? = null,
    val ownedCosmetics: List<String> = emptyList(),
    val lastIncomeNote: String = "",
    val lastPurchaseNote: String = "",
)

@Serializable
internal data class PlanDto(val category: String, val planned: Int, val spent: Int)

@Serializable
internal data class NeedDto(val category: String, val percent: Int)

@Serializable
internal data class HistoryDto(val label: String, val value: Int, val tone: String)

@Serializable
internal data class LogDto(val text: String, val delta: String, val tone: String, val rounded: Boolean)

@Serializable
internal data class ReportDto(
    val week: Int,
    val summary: String,
    val note: String,
    val rows: List<PlanDto>,
)

internal fun GameSnapshot.toDto() = SnapshotDto(
    onboarded = onboarded,
    petName = pet.name,
    species = pet.species.name,
    potStyle = pet.potStyle.name,
    totalXp = pet.totalXp,
    mood = pet.mood.name,
    moodNote = pet.moodNote,
    dayOfWeek = pet.dayOfWeek,
    weeklyIncome = plan.weeklyIncome,
    freeCoins = plan.freeCoins,
    planConfirmed = planConfirmed,
    plan = plan.entries.map { PlanDto(it.category.name, it.planned, it.spent) },
    needs = needs.map { NeedDto(it.category.name, it.percent) },
    streak = streak.map { it.done },
    doneTaskIds = tasks.filter { it.done }.map { it.id },
    selectedGoalId = selectedGoalId,
    goalSaved = goals.associate { it.id to it.saved },
    history = history.map { HistoryDto(it.label, it.value, it.tone.name) },
    weekLog = weekLog.map { LogDto(it.text, it.delta, it.tone.name, it.rounded) },
    report = report?.let { r ->
        ReportDto(r.week, r.summary, r.note, r.rows.map { PlanDto(it.category.name, it.planned, it.actual) })
    },
    demoMode = demoMode,
    soundOn = soundOn,
    earnedTotal = earnedTotal,
    weeksDone = weeksDone,
    caredWater = caredWater,
    caredFood = caredFood,
    caredPlay = caredPlay,
    saleBuys = saleBuys,
    weeksOnTrack = weeksOnTrack,
    lastOpenEpochDay = lastOpenEpochDay,
    lookVariant = pet.lookVariant,
    equippedPot = pet.equippedPot,
    equippedAccessory = pet.equippedAccessory,
    furColor = pet.appearance.furColor.name,
    eyeColor = pet.appearance.eyeColor.name,
    accessory = pet.appearance.accessory.id,
    accessories = PetAccessory.entries.filter { it in pet.appearance.accessories }.map { it.id },
    ownedCosmetics = ownedCosmetics,
    lastIncomeNote = lastIncomeNote,
    lastPurchaseNote = lastPurchaseNote,
)

internal fun SnapshotDto.toDomain(catalog: ContentCatalog): GameSnapshot {
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val total = totalXp
    return GameSnapshot(
        onboarded = onboarded,
        pet = PetProfile(
            name = petName,
            species = PetSpecies.fromStored(species),
            potStyle = runCatching { PetPotStyle.valueOf(potStyle) }.getOrDefault(PetPotStyle.CLAY),
            stageIndex = stageIndexFor(total),
            xp = xpPercentInStage(total),
            totalXp = total,
            mood = runCatching { PetMood.valueOf(mood) }.getOrDefault(PetMood.OKAY),
            moodNote = moodNote.animalText(),
            dayOfWeek = dayOfWeek,
            lookVariant = lookVariant,
            equippedPot = equippedPot,
            equippedAccessory = equippedAccessory,
            appearance = PetAppearance(
                furColor = furColor?.let(PetFurColor::fromStored)
                    ?: PetAppearance.fromLegacy(if (lookVariant != 0) lookVariant else PetPotStyle.entries.indexOfFirst { it.name == potStyle }.coerceAtLeast(0)).furColor,
                eyeColor = eyeColor?.let { value -> PetEyeColor.entries.firstOrNull { it.name == value } } ?: PetEyeColor.GREEN,
                accessories = accessories?.map(PetAccessory::fromStored)?.filter { it != PetAccessory.NONE }?.toSet()
                    ?: PetAccessory.fromStored(accessory ?: equippedAccessory).let {
                        if (it == PetAccessory.NONE) emptySet() else setOf(it)
                    },
            ),
        ),
        plan = WeekPlan(
            entries = plan.map {
                PlanEntry(SpendCategory.valueOf(it.category), it.planned, it.spent)
            },
            freeCoins = freeCoins,
            weeklyIncome = weeklyIncome,
        ),
        planConfirmed = planConfirmed,
        needs = needs.map { NeedLevel(SpendCategory.valueOf(it.category), it.percent) },
        care = GameEngine.careActions,
        streak = labels.mapIndexed { index, label -> StreakDay(label, streak.getOrElse(index) { false }) },
        tasks = catalog.tasks.map { task ->
            if (task.id in doneTaskIds) task.copy(done = true, subtitle = "выполнено") else task
        },
        shop = catalog.shop,
        goals = catalog.goals.map { it.copy(saved = goalSaved[it.id] ?: 0) },
        selectedGoalId = selectedGoalId,
        history = history.map {
            HistoryWeek(
                label = it.label,
                value = it.value,
                tone = runCatching { HistoryTone.valueOf(it.tone) }.getOrDefault(HistoryTone.MID),
                barHeight = (it.value * 4).coerceIn(8, 70),
            )
        },
        weekLog = weekLog.map {
            WeekLogEntry(
                text = it.text.animalText(),
                delta = it.delta,
                tone = runCatching { LogTone.valueOf(it.tone) }.getOrDefault(LogTone.NEUTRAL),
                rounded = it.rounded,
            )
        },
        report = report?.let { r ->
            WeekReport(
                week = r.week,
                summary = r.summary,
                note = r.note.animalText(),
                rows = r.rows.map { ReportRow(SpendCategory.valueOf(it.category), it.planned, it.spent) },
            )
        },
        demoMode = demoMode,
        soundOn = soundOn,
        earnedTotal = earnedTotal,
        weeksDone = weeksDone,
        caredWater = caredWater,
        caredFood = caredFood,
        caredPlay = caredPlay,
        saleBuys = saleBuys,
        weeksOnTrack = weeksOnTrack,
        lastOpenEpochDay = lastOpenEpochDay,
        ready = true,
        badges = emptyList(),
        ownedCosmetics = ownedCosmetics,
        lastIncomeNote = lastIncomeNote,
        lastPurchaseNote = lastPurchaseNote.animalText(),
    )
}
