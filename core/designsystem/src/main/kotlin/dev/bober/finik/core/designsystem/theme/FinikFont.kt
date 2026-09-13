package dev.bober.finik.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.bober.finik.core.designsystem.R

/** Шрифты макета: Nunito для текста, Unbounded для заголовков. Оба — variable TTF. */
object FinikFont {

    private fun variable(resId: Int, weights: List<FontWeight>): FontFamily = FontFamily(
        weights.map { weight ->
            Font(
                resId = resId,
                weight = weight,
                style = FontStyle.Normal,
                variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
            )
        },
    )

    val Nunito: FontFamily = variable(
        R.font.nunito_variable,
        listOf(FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold),
    )

    val Unbounded: FontFamily = variable(
        R.font.unbounded_variable,
        listOf(FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold),
    )
}

/**
 * `font: 700 14.5px Nunito` из макета → `nunito(14.5)`.
 * `lineHeight` — множитель как в CSS (`19px/1.1` → `lineHeight = 1.1`).
 */
fun nunito(
    size: Number,
    weight: FontWeight = FontWeight.Bold,
    lineHeight: Number? = null,
    color: Color = Color.Unspecified,
): TextStyle = TextStyle(
    fontFamily = FinikFont.Nunito,
    fontWeight = weight,
    fontSize = size.toFloat().sp,
    lineHeight = lineHeight?.let { (size.toFloat() * it.toFloat()).sp } ?: TextUnit.Unspecified,
    color = color,
)

/** `font: 600 21px Unbounded` → `unbounded(21)`. */
fun unbounded(
    size: Number,
    weight: FontWeight = FontWeight.SemiBold,
    lineHeight: Number? = null,
    color: Color = Color.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = FinikFont.Unbounded,
    fontWeight = weight,
    fontSize = size.toFloat().sp,
    lineHeight = lineHeight?.let { (size.toFloat() * it.toFloat()).sp } ?: TextUnit.Unspecified,
    color = color,
    letterSpacing = letterSpacing,
)
