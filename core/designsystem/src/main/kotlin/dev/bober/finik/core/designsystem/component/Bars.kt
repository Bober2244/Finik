package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
) {
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
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(radius))
                .background(color),
        )
    }
}

/** Составная полоса плана: сегменты по статьям, `height:14px; radius:7px`. */
@Composable
fun SegmentedBar(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2)),
    ) {
        segments.forEach { (weight, color) ->
            if (weight > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weight)
                        .background(color),
                )
            }
        }
    }
}
