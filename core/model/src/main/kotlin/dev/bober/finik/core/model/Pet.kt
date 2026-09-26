package dev.bober.finik.core.model

/** The owl is the only pet; the backend still identifies it by its legacy API name. */
enum class PetSpecies(
    val title: String,
    val description: String,
    val trait: String,
    val bonus: String,
    val legacyApiName: String,
) {
    OWL("Сова", "Спокойный пернатый исследователь", "любит заботу", "+опыт за уход", "CACTUS"),
    ;

    companion object {
        /** Old CAT/DOG/FINIK/SPARK profiles keep their data while switching to the owl. */
        fun fromStored(@Suppress("UNUSED_PARAMETER") value: String): PetSpecies = OWL
    }
}

/** Retained only to read old snapshots and communicate with the existing backend. */
enum class PetPotStyle(val title: String) {
    CLAY("натуральный"), SKY("серебристый"), SUN("кремовый"),
}

enum class PetFurColor(val title: String, val assetId: String, val swatchArgb: Long) {
    BLUE("Синяя", "blue", 0xFF6298CA),
    DESERT_SAND("Песочная", "desert_sand", 0xFFD6AB7A),
    FIERY_RED("Рыжая", "fiery_red", 0xFFCA644E),
    FOREST_GREEN("Лесная", "forest_green", 0xFF6B9873),
    NIGHT_PURPLE("Фиолетовая", "night_purple", 0xFF82649F),
    SNOWY_WHITE("Белая", "snowy_white", 0xFFE5E5DF),
    ;

    val textureAssetPath: String get() = "models/owl/textures/$assetId.jpg"

    companion object {
        fun fromStored(value: String): PetFurColor = when (value.trim().uppercase()) {
            "BLUE", "NATURAL" -> BLUE
            "DESERT_SAND", "CREAM", "CHOCOLATE" -> DESERT_SAND
            "FIERY_RED" -> FIERY_RED
            "FOREST_GREEN" -> FOREST_GREEN
            "NIGHT_PURPLE" -> NIGHT_PURPLE
            "SNOWY_WHITE", "SILVER" -> SNOWY_WHITE
            else -> BLUE
        }
    }
}

/** Kept for old snapshots; the new owl's eyes are part of its baked color texture. */
enum class PetEyeColor(val title: String, val argb: Long) {
    GREEN("Зелёные", 0xFF72B887),
    BLUE("Голубые", 0xFF6CB6E7),
    AMBER("Янтарные", 0xFFE5AF4D),
    BROWN("Карие", 0xFF85563B),
}

enum class PetAccessory(val title: String, val id: String) {
    NONE("Без аксессуара", "none"),
    HAT("Шляпа", "hat"),
    BANDANA("Бандана", "bandana"),
    MEDAL("Медаль", "medal"),
    BACKPACK("Рюкзак", "backpack"),
    ;

    companion object {
        fun fromStored(value: String): PetAccessory = when (value.trim().lowercase()) {
            "hat", "cap" -> HAT
            "bandana", "scarf" -> BANDANA
            "medal", "bow" -> MEDAL
            "backpack" -> BACKPACK
            else -> NONE
        }
    }
}

data class PetAppearance(
    val furColor: PetFurColor = PetFurColor.BLUE,
    val eyeColor: PetEyeColor = PetEyeColor.GREEN,
    val accessories: Set<PetAccessory> = emptySet(),
) {
    init { require(PetAccessory.NONE !in accessories) { "NONE cannot be equipped" } }

    /** Compatibility for old snapshots and callers that stored one accessory. */
    val accessory: PetAccessory get() = PetAccessory.entries.firstOrNull { it in accessories } ?: PetAccessory.NONE

    constructor(furColor: PetFurColor, eyeColor: PetEyeColor, accessory: PetAccessory) : this(
        furColor,
        eyeColor,
        if (accessory == PetAccessory.NONE) emptySet() else setOf(accessory),
    )

    companion object {
        fun fromLegacy(lookVariant: Int, accessory: String = "") = PetAppearance(
            furColor = when (lookVariant) {
                1 -> PetFurColor.SNOWY_WHITE
                2 -> PetFurColor.DESERT_SAND
                else -> PetFurColor.BLUE
            },
            accessories = PetAccessory.fromStored(accessory).let { if (it == PetAccessory.NONE) emptySet() else setOf(it) },
        )
    }
}

enum class PetMood(val label: String) {
    HAPPY(label = "доволен"),
    OKAY(label = "в порядке"),
    BORED(label = "скучает"),
    SAD(label = "грустит"),
    ;

    /** В макете улыбка показывается при среднем уровне потребностей > 55. */
    val isSmiling: Boolean get() = this == HAPPY || this == OKAY
}

/** Стадия роста. `requiredXp` — порог, при котором стадия открывается. */
data class GrowthStage(
    val name: String,
    val note: String,
    val requiredXp: Int,
)

val growthStages: List<GrowthStage> = listOf(
    GrowthStage(name = "Малыш", note = "Первое знакомство и забота", requiredXp = 0),
    GrowthStage(name = "Непоседа", note = "Открывает мир вместе с тобой", requiredXp = 100),
    GrowthStage(name = "Подросток", note = "Становится увереннее", requiredXp = 200),
    GrowthStage(name = "Взрослый друг", note = "Крепкая дружба и хорошие привычки", requiredXp = 300),
    GrowthStage(name = "Мудрый друг", note = "Вы многому научились вместе", requiredXp = 400),
)

fun stageIndexFor(totalXp: Int): Int =
    growthStages.indexOfLast { totalXp >= it.requiredXp }.coerceAtLeast(0)

fun xpPercentInStage(totalXp: Int): Int {
    val index = stageIndexFor(totalXp)
    val current = growthStages[index].requiredXp
    val next = growthStages.getOrNull(index + 1)?.requiredXp ?: (current + 100)
    val span = (next - current).coerceAtLeast(1)
    return ((totalXp - current) * 100 / span).coerceIn(0, 100)
}

/** Текущее состояние питомца. */
data class PetProfile(
    val name: String,
    val species: PetSpecies,
    val potStyle: PetPotStyle = PetPotStyle.CLAY,
    val stageIndex: Int,
    val xp: Int,
    val totalXp: Int = xp,
    val mood: PetMood,
    val moodNote: String,
    val dayOfWeek: Int,
    val lookVariant: Int = 0,
    val equippedPot: String = "",
    val equippedAccessory: String = "",
    val appearance: PetAppearance = PetAppearance.fromLegacy(lookVariant, equippedAccessory),
) {
    val stage: GrowthStage get() = growthStages[stageIndex.coerceIn(growthStages.indices)]
}

data class NeedLevel(val category: SpendCategory, val percent: Int)
