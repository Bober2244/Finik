package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito

@Immutable
data class FinikNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Нижняя навигация: пилюля 44×26 с узнаваемой пиктограммой и подписью 11px.
 * Активная вкладка — зелёная пилюля и тёмно-зелёный текст.
 */
@Composable
fun FinikBottomNav(
    items: List<FinikNavItem>,
    selectedId: String?,
    onItemClick: (FinikNavItem) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val density = LocalDensity.current
    val largeText = density.fontScale >= 1.2f
    val labelStyle = nunito(12)
    val textMeasurer = rememberTextMeasurer()
    val minimumItemWidth = with(density) { 48.dp.toPx() }
    val labelPadding = with(density) { 4.dp.toPx() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FinikColor.Background)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = FinikColor.Track, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (largeText) 4.dp else 8.dp,
                    end = if (largeText) 4.dp else 8.dp,
                    top = 6.dp,
                    bottom = 10.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                val itemWeight = if (largeText) {
                    maxOf(
                        minimumItemWidth,
                        textMeasurer.measure(
                            text = item.label,
                            style = labelStyle,
                            maxLines = 1,
                            softWrap = false,
                        ).size.width.toFloat() + labelPadding,
                    )
                } else {
                    1f
                }
                Column(
                    modifier = Modifier
                        .weight(itemWeight)
                        .heightIn(min = 58.dp)
                        .clickable(
                            enabled = enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClickLabel = "Открыть ${item.label}",
                            onClick = { onItemClick(item) },
                        )
                        .semantics { this.selected = selected }
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 26.dp)
                            .background(
                                if (selected) FinikColor.GreenPill else Color.Transparent,
                                RoundedCornerShape(9.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) FinikColor.GreenPressed else FinikColor.Text50,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Text(
                        text = item.label,
                        style = labelStyle,
                        color = if (selected) FinikColor.GreenInk36 else FinikColor.Text50,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
