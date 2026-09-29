package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import dev.bober.finik.core.designsystem.R
import dev.bober.finik.core.designsystem.theme.FinikColor

/** A single static scene behind navigation, with no touch or accessibility targets. */
@Composable
fun FinikWorldBackground(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val paper = FinikColor.Background
    Box(modifier = modifier.background(paper)) {
        Image(
            painter = painterResource(R.drawable.finik_forest_day),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            alpha = if (dark) .18f else 1f,
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to paper.copy(alpha = if (dark) .10f else .16f),
                    .24f to paper.copy(alpha = if (dark) .12f else .28f),
                    .64f to paper.copy(alpha = if (dark) .18f else .32f),
                    1f to paper.copy(alpha = if (dark) .24f else .38f),
                ),
            ),
        )
    }
}
