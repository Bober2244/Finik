package dev.bober.finik.core.model

/** Форма пиктограммы товара — макет рисует товары простыми фигурами. */
enum class ItemGlyph { CIRCLE, ROUNDED, POT, TALL_POT, BUCKET }

data class ShopItem(
    val id: String,
    val name: String,
    val cost: Int,
    val oldCost: Int? = null,
    val category: SpendCategory,
    val glyph: ItemGlyph,
) {
    val isSale: Boolean get() = oldCost != null
    val salePercent: Int get() = oldCost?.let { ((1 - cost.toFloat() / it) * 100).let(Math::round) } ?: 0
}
