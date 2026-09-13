package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded

/** Заголовок вкладки/экрана: `600 21px Unbounded`. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = unbounded(21), color = FinikColor.Ink, modifier = modifier)
}

/** «ШАГ 1 ИЗ 2»: `700 11px; letter-spacing .12em; uppercase`. */
@Composable
fun StepLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = nunito(11).copy(letterSpacing = 0.12.em),
        color = FinikColor.Text56,
        modifier = modifier,
    )
}

/** Маленький цветной чип с текстом: `700 11px; padding 4px 8px; radius 7`. */
@Composable
fun TagChip(
    text: String,
    background: Color,
    ink: Color,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Bold,
) {
    Text(
        text = text,
        style = nunito(11, weight),
        color = ink,
        modifier = modifier
            .background(background, RoundedCornerShape(7.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Шапка оверлея: кнопка «←» 42dp и заголовок Unbounded 21. */
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SquareIconButton(onClick = onBack, size = 42.dp) {
            Text(text = "←", style = nunito(18), color = FinikColor.Ink)
        }
        ScreenTitle(text = title)
    }
}

/** Строка «заголовок слева · подпись справа» с выравниванием по базовой линии. */
@Composable
fun TitleRow(
    title: String,
    trailing: String,
    modifier: Modifier = Modifier,
    titleStyle: androidx.compose.ui.text.TextStyle = nunito(14.5),
    trailingColor: Color = FinikColor.Text50,
) {
    Row(
        modifier = modifier.padding(0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(text = title, style = titleStyle, color = FinikColor.Ink)
        Text(text = trailing, style = nunito(12.5), color = trailingColor)
    }
}
