package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.model.ItemGlyph
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.SpendCategory

/**
 * Пиктограммы макета — простые фигуры, заданные через border-radius.
 * Именованные формы соответствуют CSS-значениям из прототипа.
 */
object DotShape {
    /** `50%` */
    val Circle: Shape = CircleShape

    /** `8px` / `9px` / `4px` … */
    fun rounded(radius: Dp): Shape = RoundedCornerShape(radius)

    /** `50% 50% 50% 5px` — капля/лист. */
    fun drop(corner: Dp = 5.dp): Shape = RoundedCornerShape(
        topStart = CornerSize(50),
        topEnd = CornerSize(50),
        bottomEnd = CornerSize(50),
        bottomStart = CornerSize(corner),
    )

    /** `50% 4px 50% 4px` — вкладка «Задания». */
    fun leaf(corner: Dp = 4.dp): Shape = RoundedCornerShape(
        topStart = CornerSize(50),
        topEnd = CornerSize(corner),
        bottomEnd = CornerSize(50),
        bottomStart = CornerSize(corner),
    )

    /** `9px 9px 18px 18px` — горшок. */
    fun pot(top: Dp, bottom: Dp): Shape = RoundedCornerShape(
        topStart = top, topEnd = top, bottomEnd = bottom, bottomStart = bottom,
    )

    /** `9px 9px 50% 50%` — лейка/ведро. */
    fun bucket(top: Dp = 9.dp): Shape = RoundedCornerShape(
        topStart = CornerSize(top),
        topEnd = CornerSize(top),
        bottomEnd = CornerSize(50),
        bottomStart = CornerSize(50),
    )
}

@Composable
fun ShapeDot(
    color: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    Box(modifier = modifier.size(size).background(color, shape))
}

/** Форма пиктограммы статьи плана: еда — квадрат, игры — капля, вода — круг. */
val SpendCategory.glyphShape: Shape
    get() = when (this) {
        SpendCategory.FOOD -> DotShape.rounded(8.dp)
        SpendCategory.PLAY -> DotShape.drop()
        else -> DotShape.Circle
    }

val ShopItem.glyphShape: Shape
    get() = when (glyph) {
        ItemGlyph.CIRCLE -> DotShape.Circle
        ItemGlyph.ROUNDED -> DotShape.rounded(9.dp)
        ItemGlyph.POT -> DotShape.pot(9.dp, 18.dp)
        ItemGlyph.TALL_POT -> DotShape.pot(10.dp, 20.dp)
        ItemGlyph.BUCKET -> DotShape.bucket(9.dp)
    }

/** Цвет пиктограммы товара — в макете у каждого предмета свой оттенок. */
val ShopItem.glyphColor: Color
    get() = when (id) {
        "s3" -> FinikColor.FoodPot
        "s5" -> FinikColor.PlayPot
        "s6" -> FinikColor.WaterCan
        else -> when (category) {
            SpendCategory.FOOD -> FinikColor.FoodBright
            SpendCategory.WATER -> FinikColor.Water
            SpendCategory.PLAY -> FinikColor.Play
            SpendCategory.SAVE -> FinikColor.Green
        }
    }
