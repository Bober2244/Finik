package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor

/** Белая карточка с обводкой 1px: `border-radius:18px; padding:14px; gap:12px`. */
@Composable
fun FinikCard(
    modifier: Modifier = Modifier,
    radius: Dp = 18.dp,
    background: Color = FinikColor.Surface,
    borderColor: Color? = FinikColor.Border,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    gap: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(
                if (borderColor != null) Modifier.border(BorderStroke(borderWidth, borderColor), shape)
                else Modifier,
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}

/** Карточка с градиентной заливкой без обводки (герой питомца, welcome). */
@Composable
fun GradientCard(
    brush: Brush,
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    gap: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(brush)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}
