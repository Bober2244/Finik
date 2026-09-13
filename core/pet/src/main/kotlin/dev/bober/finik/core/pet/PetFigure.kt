package dev.bober.finik.core.pet

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.model.PetMood
import dev.bober.finik.core.model.PetSpecies

/**
 * Геометрия фигуры в dp, один к одному с CSS макета (бутон, глаза, стебель, горшок).
 * Пресеты — в [PetFigureSpec.Companion].
 */
data class PetFigureSpec(
    val width: Dp,
    val height: Dp,
    val bud: Dp,
    /** Отступ бутона от верха контейнера; если null — используется [budBottom]. */
    val budTop: Dp?,
    val budBottom: Dp = 0.dp,
    val eye: Dp,
    val eyeGap: Dp,
    val eyePadTop: Dp,
    val stemWidth: Dp,
    val stemHeight: Dp,
    val stemBottom: Dp,
    val potWidth: Dp,
    val potHeight: Dp,
    val potTopRadius: Dp,
    val potBottomRadius: Dp,
    val leaves: Int = 0,
    val bobDurationMs: Int = 0,
) {
    companion object {
        /** Экран приветствия: контейнер 150×150. */
        val Welcome = PetFigureSpec(
            width = 150.dp, height = 150.dp, bud = 82.dp, budTop = 4.dp,
            eye = 12.dp, eyeGap = 14.dp, eyePadTop = 8.dp,
            stemWidth = 10.dp, stemHeight = 52.dp, stemBottom = 46.dp,
            potWidth = 96.dp, potHeight = 54.dp, potTopRadius = 10.dp, potBottomRadius = 30.dp,
            bobDurationMs = 4000,
        )

        /** Карточка выбора ростка: 74×74. */
        val Card = PetFigureSpec(
            width = 74.dp, height = 74.dp, bud = 46.dp, budTop = 0.dp,
            eye = 7.dp, eyeGap = 8.dp, eyePadTop = 5.dp,
            stemWidth = 7.dp, stemHeight = 28.dp, stemBottom = 22.dp,
            potWidth = 54.dp, potHeight = 30.dp, potTopRadius = 7.dp, potBottomRadius = 18.dp,
        )

        /** Экран имени: 118×118. */
        val Naming = PetFigureSpec(
            width = 118.dp, height = 118.dp, bud = 70.dp, budTop = 0.dp,
            eye = 11.dp, eyeGap = 12.dp, eyePadTop = 7.dp,
            stemWidth = 9.dp, stemHeight = 40.dp, stemBottom = 38.dp,
            potWidth = 84.dp, potHeight = 48.dp, potTopRadius = 9.dp, potBottomRadius = 26.dp,
            bobDurationMs = 3800,
        )

        /** Герой на главном экране: 150×186, размер бутона и стебля растут со стадией. */
        fun hero(stageIndex: Int) = PetFigureSpec(
            width = 150.dp, height = 186.dp,
            bud = (64 + stageIndex * 9).dp, budTop = null, budBottom = 60.dp,
            eye = 14.dp, eyeGap = 16.dp, eyePadTop = 9.dp,
            stemWidth = 11.dp, stemHeight = (44 + stageIndex * 8).dp, stemBottom = 48.dp,
            potWidth = 110.dp, potHeight = 62.dp, potTopRadius = 11.dp, potBottomRadius = 32.dp,
            leaves = maxOf(1, stageIndex),
            bobDurationMs = 4200,
        )
    }
}

/**
 * Фигура питомца — слот для будущей 3D-модели из Blender.
 *
 * Сейчас это плоская вёрстка из макета: бутон с глазами (и ртом в герое), стебель,
 * листья и горшок, с idle-анимациями «bob» и «sway». Когда появится glTF/GLB-модель,
 * тело функции заменяется на рендерер, сигнатура остаётся прежней — экраны не меняются.
 */
@Composable
fun PetFigure(
    species: PetSpecies,
    spec: PetFigureSpec,
    modifier: Modifier = Modifier,
    mood: PetMood? = null,
    animate: Boolean = true,
) {
    val look = species.look
    val bob = rememberIdle(active = animate && spec.bobDurationMs > 0, durationMs = spec.bobDurationMs)
    val sway = rememberIdle(active = animate && spec.leaves > 0, durationMs = 5500)

    Box(modifier = modifier.size(spec.width, spec.height)) {
        // Горшок
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(spec.potWidth, spec.potHeight)
                .background(
                    FinikColor.Pot,
                    RoundedCornerShape(
                        topStart = spec.potTopRadius, topEnd = spec.potTopRadius,
                        bottomEnd = spec.potBottomRadius, bottomStart = spec.potBottomRadius,
                    ),
                ),
        )

        // Бутон с глазами и ртом (bob: translateY 0→-7, rotate -1°→1°)
        val budAlignment = if (spec.budTop != null) Alignment.TopCenter else Alignment.BottomCenter
        val budOffsetY = spec.budTop ?: -spec.budBottom
        Box(
            modifier = Modifier
                .align(budAlignment)
                .offset(y = budOffsetY)
                .graphicsLayer {
                    translationY = -7.dp.toPx() * bob
                    rotationZ = -1f + 2f * bob
                }
                .size(spec.bud)
                .background(look.bud, look.budShape),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(top = spec.eyePadTop),
                horizontalArrangement = Arrangement.spacedBy(spec.eyeGap),
            ) {
                repeat(2) {
                    Box(modifier = Modifier.size(spec.eye).background(FinikColor.InkDeep, CircleShape))
                }
            }
            if (mood != null) {
                if (mood.isSmiling) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-13).dp)
                            .size(width = 24.dp, height = 12.dp)
                            .background(
                                FinikColor.Mouth,
                                RoundedCornerShape(
                                    topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp),
                                    bottomEnd = CornerSize(14.dp), bottomStart = CornerSize(14.dp),
                                ),
                            ),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-16).dp)
                            .size(width = 20.dp, height = 4.dp)
                            .background(FinikColor.Mouth, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }

        // Листья (sway: rotate -5°→5°, origin bottom center)
        repeat(spec.leaves) { index ->
            val odd = index % 2 == 1
            val leafWidth = (34 + index * 4).dp
            val leftFraction = if (odd) 0.62f else 0.18f
            val baseRotation = if (odd) 26f else -26f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = spec.width * leftFraction, y = -(48 + index * 13).dp)
                    .graphicsLayer {
                        rotationZ = baseRotation + (-5f + 10f * sway)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .size(width = leafWidth, height = 19.dp)
                    .background(
                        if (odd) Color(0xFF60A563) else Color(0xFF59B47D),
                        RoundedCornerShape(
                            topStart = CornerSize(50), topEnd = CornerSize(5.dp),
                            bottomEnd = CornerSize(50), bottomStart = CornerSize(5.dp),
                        ),
                    ),
            )
        }

        // Стебель (в макете рисуется поверх бутона)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -spec.stemBottom)
                .size(spec.stemWidth, spec.stemHeight)
                .background(look.stem, RoundedCornerShape(spec.stemWidth / 2)),
        )
    }
}

/** Бутон-иконка для списка стадий роста: `border-radius:52% 52% 44% 44%`. */
@Composable
fun StageBud(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color,
                RoundedCornerShape(
                    topStart = CornerSize(50), topEnd = CornerSize(50),
                    bottomEnd = CornerSize(44), bottomStart = CornerSize(44),
                ),
            ),
    )
}

@Composable
private fun rememberIdle(active: Boolean, durationMs: Int): Float {
    if (!active) return 0.5f
    val transition = rememberInfiniteTransition(label = "pet-idle")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs / 2, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pet-idle-value",
    )
    return value
}

@Preview(showBackground = true)
@Composable
private fun PetFigurePreview() {
    FinikTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(12.dp)) {
            PetFigure(species = PetSpecies.FINIK, spec = PetFigureSpec.Welcome, animate = false)
            PetFigure(species = PetSpecies.FINIK, spec = PetFigureSpec.hero(2), mood = PetMood.OKAY, animate = false)
            PetFigure(species = PetSpecies.SPARK, spec = PetFigureSpec.Card, animate = false)
        }
    }
}
