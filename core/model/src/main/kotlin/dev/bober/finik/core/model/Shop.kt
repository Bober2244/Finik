package dev.bober.finik.core.model

/** Форма пиктограммы товара — макет рисует товары простыми фигурами. */
enum class ItemGlyph { CIRCLE, ROUNDED, POT, TALL_POT, BUCKET }

/** Обязательная покупка нужна питомцу; необязательную можно перенести. */
enum class SpendKind(val label: String) {
    REQUIRED(label = "нужное"),
    OPTIONAL(label = "желаемое"),
}

data class ShopItem(
    val id: String,
    val name: String,
    val cost: Int,
    val oldCost: Int? = null,
    val category: SpendCategory,
    val glyph: ItemGlyph,
    val kind: SpendKind = if (category == SpendCategory.PLAY) SpendKind.OPTIONAL else SpendKind.REQUIRED,
    val effect: String = "",
    val imageUrl: String? = null,
    val restore: Int = 0,
    val xpBonus: Int = 0,
    val slot: String = "",
    val owned: Boolean = false,
    val equipped: Boolean = false,
    val leftInCategory: Int? = null,
    val affordable: Boolean = true,
) {
    val isSale: Boolean get() = oldCost != null
    val salePercent: Int get() = oldCost?.let { ((1 - cost.toFloat() / it) * 100).let(Math::round) } ?: 0
}
