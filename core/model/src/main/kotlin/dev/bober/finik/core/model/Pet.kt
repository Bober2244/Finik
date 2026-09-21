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

/** Цвет горшка: 3 вида × 3 горшка = 9 различимых комбинаций (минимум ТЗ). */
enum class PetPotStyle(val title: String) {
    CLAY(title = "глиняный"),
    SKY(title = "небесный"),
    SUN(title = "солнечный"),
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
) {
    val stage: GrowthStage get() = growthStages[stageIndex.coerceIn(growthStages.indices)]
}

data class NeedLevel(val category: SpendCategory, val percent: Int)
