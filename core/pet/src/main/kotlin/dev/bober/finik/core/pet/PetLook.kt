package dev.bober.finik.core.pet

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies

/** Внешность вида: цвет бутона, цвет стебля и форма бутона (border-radius из макета). */
data class PetLook(
    val bud: Color,
    val stem: Color,
    val budShape: Shape,
)

private fun budShape(top: Int, bottom: Int): Shape = RoundedCornerShape(
    topStart = CornerSize(top.coerceAtMost(50)),
    topEnd = CornerSize(top.coerceAtMost(50)),
    bottomEnd = CornerSize(bottom),
    bottomStart = CornerSize(bottom),
)

val PetPotStyle.color: Color
    get() = when (this) {
        PetPotStyle.CLAY -> FinikColor.Pot
        PetPotStyle.SKY -> Color(0xFF6FA8C9)
        PetPotStyle.SUN -> Color(0xFFE2B84A)
    }

val PetSpecies.look: PetLook
    get() = when (this) {
        PetSpecies.FINIK -> PetLook(
            bud = Color(0xFF5CB572),
            stem = Color(0xFF3D8E53),
            budShape = budShape(52, 44),
        )
        PetSpecies.CACTUS -> PetLook(
            bud = Color(0xFF8CB460),
            stem = Color(0xFF6A8D43),
            budShape = budShape(44, 38),
        )
        PetSpecies.SPARK -> PetLook(
            bud = Color(0xFFEB9070),
            stem = Color(0xFFB6753B),
            budShape = budShape(58, 42),
        )
    }
