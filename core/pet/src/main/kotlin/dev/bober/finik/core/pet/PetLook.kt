package dev.bober.finik.core.pet

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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

val PetSpecies.look: PetLook
    get() = when (this) {
        PetSpecies.FINIK -> PetLook(
            bud = Color(0xFF5CB572),   // oklch(0.7 0.13 150)
            stem = Color(0xFF3D8E53),  // oklch(0.58 0.12 150)
            budShape = budShape(52, 44),
        )
        PetSpecies.CACTUS -> PetLook(
            bud = Color(0xFF8CB460),   // oklch(0.72 0.12 130)
            stem = Color(0xFF6A8D43),  // oklch(0.6 0.11 130)
            budShape = budShape(44, 38),
        )
        PetSpecies.SPARK -> PetLook(
            bud = Color(0xFFEB9070),   // oklch(0.74 0.12 40)
            stem = Color(0xFFB6753B),  // oklch(0.62 0.11 60)
            budShape = budShape(58, 42),
        )
    }
