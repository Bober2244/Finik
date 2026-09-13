package dev.bober.finik.core.model

/** Вид ростка, который выбирают в онбординге. Внешний вид описывает core:pet. */
enum class PetSpecies(
    val title: String,
    val description: String,
    val trait: String,
    val bonus: String,
) {
    FINIK(
        title = "Финик",
        description = "Пальма из финиковой косточки",
        trait = "пьёт много воды",
        bonus = "+опыт за воду",
    ),
    CACTUS(
        title = "Кактус Пух",
        description = "Экономный, терпит без полива",
        trait = "редко ест",
        bonus = "дешёвый уход",
    ),
    SPARK(
        title = "Огонёк",
        description = "Цветок, который любит внимание",
        trait = "скучает быстрее",
        bonus = "+опыт за игры",
    ),
}

enum class PetMood(val label: String) {
    HAPPY(label = "доволен"),
    OKAY(label = "в порядке"),
    BORED(label = "скучает"),
    SAD(label = "плохо"),
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
    GrowthStage(name = "Семечко", note = "Всё впереди", requiredXp = 0),
    GrowthStage(name = "Росток", note = "План выполняется", requiredXp = 100),
    GrowthStage(name = "Кустик", note = "Копилка не пустеет", requiredXp = 200),
    GrowthStage(name = "Молодое дерево", note = "Цель близко", requiredXp = 300),
    GrowthStage(name = "Дерево", note = "Мечта собрана", requiredXp = 400),
)

/** Текущее состояние питомца. */
data class PetProfile(
    val name: String,
    val species: PetSpecies,
    val stageIndex: Int,
    val xp: Int,
    val mood: PetMood,
    val moodNote: String,
    val dayOfWeek: Int,
) {
    val stage: GrowthStage get() = growthStages[stageIndex]
}

data class NeedLevel(val category: SpendCategory, val percent: Int)
