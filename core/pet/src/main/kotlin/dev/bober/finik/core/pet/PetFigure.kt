package dev.bober.finik.core.pet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetSpecies

/** Размер области питомца. Небольшие карточки используют экспортированный Blender-портрет. */
data class PetFigureSpec(
    val width: Dp,
    val height: Dp,
    val stageIndex: Int = 0,
    val live3d: Boolean = false,
) {
    companion object {
        val Welcome = PetFigureSpec(150.dp, 160.dp, live3d = true)
        val Card = PetFigureSpec(74.dp, 74.dp, live3d = true)
        val Naming = PetFigureSpec(140.dp, 154.dp, live3d = true)
        fun hero(stageIndex: Int) = PetFigureSpec(190.dp, 214.dp, stageIndex, live3d = true)
    }
}

/** Available motions in the owl GLB. Idle_3 is the natural standing pose. */
enum class PetAnimation(val clipName: String) {
    IDLE("Idle_3"),
    CALM("Idle_4"),
    WALK("Walking"),
    RUN("Running"),
    DRINK("Stand_and_Drink"),
    GREET("Big_Wave_Hello"),
    HELP("Wave_for_Help_1"),
    THINK("01a0cfcc-7a03-75ac-bde5-e71cb8b79d9e"),
}

/**
 * Скелетная 3D-сова с шестью текстурами и аксессуарами.
 *
 * Увеличивайте [actionEventId] после каждого действия, включая повтор одного и того же действия.
 * Одноразовый клип завершается возвращением к спокойной стойке. [animate] фиксирует
 * стоячую позу, сохраняя возможность поворачивать камеру. В Compose Preview Filament не создаётся.
 */
@Composable
fun PetFigure(
    species: PetSpecies,
    spec: PetFigureSpec,
    modifier: Modifier = Modifier,
    mood: PetMood? = null,
    appearance: PetAppearance = PetAppearance(),
    action: PetAnimation = PetAnimation.IDLE,
    actionEventId: Long = 0L,
    animate: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(spec.width, spec.height)
            .semantics { contentDescription = "Сова. Проведите пальцем, чтобы повернуть; сведите пальцы, чтобы изменить масштаб" },
    ) {
        if (LocalInspectionMode.current || !spec.live3d) {
            PetPortrait(species, Modifier.matchParentSize())
        } else {
            PetScene(
                species = species,
                stageIndex = spec.stageIndex,
                appearance = appearance,
                mood = mood,
                action = action,
                actionEventId = actionEventId,
                animate = animate,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
internal fun PetPortrait(species: PetSpecies, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pet_owl),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Лапка вместо растительного бутона на шкале взросления. */
@Composable
fun StageAnimal(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        drawOval(color, Offset(this.size.width * .22f, this.size.height * .44f), Size(this.size.width * .56f, this.size.height * .48f))
        listOf(.16f to .35f, .36f to .18f, .64f to .18f, .84f to .35f).forEach { (x, y) ->
            drawOval(color, Offset(this.size.width * (x - .115f), this.size.height * (y - .14f)), Size(this.size.width * .23f, this.size.height * .28f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PetFigurePreview() {
    FinikTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(12.dp)) {
            PetSpecies.entries.forEach { PetFigure(species = it, spec = PetFigureSpec.Card) }
        }
    }
}
