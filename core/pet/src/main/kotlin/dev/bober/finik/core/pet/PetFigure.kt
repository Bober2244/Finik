package dev.bober.finik.core.pet

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetSpecies
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/** Размер области питомца. Небольшие карточки используют экспортированный Blender-портрет. */
data class PetFigureSpec(
    val width: Dp,
    val height: Dp,
    val stageIndex: Int = 0,
    val live3d: Boolean = false,
    val portraitScale: Float = 1f,
    val portraitOffsetY: Dp = 0.dp,
    val modelScaleMultiplier: Float = 1f,
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

val LocalPetAnimationEnabled = compositionLocalOf { true }

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
    onInteractionChange: (Boolean) -> Unit = {},
) {
    val effectiveAnimation = animate && LocalPetAnimationEnabled.current
    var showLiveScene by remember(spec.live3d) { mutableStateOf(false) }
    var sceneReady by remember(spec.live3d, spec.stageIndex) { mutableStateOf(false) }
    var sceneFailed by remember(spec.live3d, spec.stageIndex) { mutableStateOf(false) }
    LaunchedEffect(spec.live3d) {
        if (spec.live3d) {
            // Paint the lightweight portrait first; native 3D loading starts after initial UI.
            delay(3.seconds)
            showLiveScene = true
        }
    }
    Box(
        modifier = modifier
            .size(spec.width, spec.height)
            .semantics {
                contentDescription = if (spec.live3d) {
                    "Сова. Проведите пальцем, чтобы повернуть; сведите пальцы, чтобы изменить масштаб"
                } else {
                    "Портрет совы"
                }
            },
    ) {
        if (spec.live3d && showLiveScene && !LocalInspectionMode.current) {
            PetScene(
                species = species,
                stageIndex = spec.stageIndex,
                appearance = appearance,
                mood = mood,
                action = action,
                actionEventId = actionEventId,
                animate = effectiveAnimation,
                modelScaleMultiplier = spec.modelScaleMultiplier,
                onSceneReady = {
                    sceneReady = true
                    sceneFailed = false
                },
                onSceneFailure = { sceneFailed = true },
                onSceneRetry = { sceneFailed = false },
                onInteractionChange = onInteractionChange,
                modifier = Modifier.matchParentSize(),
            )
        }
        // A presented Filament frame can still be empty while resources warm up.
        // Keep the portrait above the TextureView until the scene passes its startup gate.
        val portraitAlpha by animateFloatAsState(
            targetValue = if (spec.live3d && (sceneReady || sceneFailed)) 0f else 1f,
            label = "Pet portrait",
        )
        if (portraitAlpha > 0f) {
            PetPortrait(
                appearance,
                Modifier.matchParentSize()
                    .offset(y = spec.portraitOffsetY)
                    .scale(spec.portraitScale)
                    .alpha(portraitAlpha),
            )
        }
    }
}

@Composable
internal fun PetPortrait(appearance: PetAppearance, modifier: Modifier = Modifier) {
    val portrait = when (appearance.furColor) {
        PetFurColor.BLUE -> R.drawable.pet_owl_cutout
        PetFurColor.DESERT_SAND -> R.drawable.pet_owl_desert_sand
        PetFurColor.FIERY_RED -> R.drawable.pet_owl_fiery_red
        PetFurColor.FOREST_GREEN -> R.drawable.pet_owl_forest_green
        PetFurColor.NIGHT_PURPLE -> R.drawable.pet_owl_night_purple
        PetFurColor.SNOWY_WHITE -> R.drawable.pet_owl_snowy_white
    }
    Image(
        painter = painterResource(portrait),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
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
