package dev.bober.finik.core.model

/** Статьи недельного плана. Цвета статей задаёт дизайн-система. */
enum class SpendCategory(val label: String, val note: String) {
    FOOD(label = "Еда", note = "корм и витамины"),
    WATER(label = "Вода", note = "полив и лейка"),
    PLAY(label = "Игры", note = "игрушки и веселье"),
    SAVE(label = "Копилка", note = "только на мечту"),
}
