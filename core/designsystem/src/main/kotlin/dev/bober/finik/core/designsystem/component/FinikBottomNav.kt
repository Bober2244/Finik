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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito

@Immutable
data class FinikNavItem(
    val id: String,
    val label: String,
    val glyph: Shape,
)

/**
 * Нижняя навигация макета: пилюля 44×26 с фигурной точкой 13dp и подпись 11px.
 * Активная вкладка — зелёная пилюля и тёмно-зелёный текст.
 */
@Composable
fun FinikBottomNav(
    items: List<FinikNavItem>,
    selectedId: String?,
    onItemClick: (FinikNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items.forEach { item ->
                val selected = item.id == selectedId
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 58.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemClick(item) },
                        )
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
                        ShapeDot(
                            color = if (selected) FinikColor.GreenPressed else FinikColor.DotInactive,
                            shape = item.glyph,
                            size = 13.dp,
                        )
                    }
                    Text(
                        text = item.label,
                        style = nunito(11),
                        color = if (selected) FinikColor.GreenInk36 else FinikColor.Text50,
                    )
                }
            }
        }
    }
}
