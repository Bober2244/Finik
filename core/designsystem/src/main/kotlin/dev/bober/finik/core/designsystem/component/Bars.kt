package dev.bober.finik.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor

/** Горизонтальная полоска прогресса со скруглёнными краями. */
@Composable
fun FinikProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 9.dp,
    track: Color = FinikColor.Track,
    radius: Dp = height / 2,
    animate: Boolean = true,
) {
    val targetProgress = progress.coerceIn(0f, 1f)
    val displayedProgress = if (animate) {
        animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            label = "Прогресс",
        ).value
    } else {
        targetProgress
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(displayedProgress)
                .clip(RoundedCornerShape(radius))
                .background(color),
        )
    }
}

/** Составная полоса плана. [totalWeight] оставляет часть трека пустой для нераспределённого бюджета. */
@Composable
fun SegmentedBar(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    totalWeight: Float? = null,
    track: Color = FinikColor.Track,
    animate: Boolean = true,
) {
    val sum = segments.sumOf { it.first.coerceAtLeast(0f).toDouble() }.toFloat()
    val denominator = maxOf(sum, totalWeight?.coerceAtLeast(0f) ?: sum, 1f)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(track),
    ) {
        val barWidth = maxWidth
        Row(modifier = Modifier.fillMaxSize()) {
            segments.forEach { (weight, color) ->
                val targetFraction = weight.coerceAtLeast(0f) / denominator
                val fraction = if (animate) {
                    animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        label = "Сегмент плана",
                    ).value
                } else {
                    targetFraction
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(barWidth * fraction)
                        .background(color),
                )
            }
        }
    }
}
