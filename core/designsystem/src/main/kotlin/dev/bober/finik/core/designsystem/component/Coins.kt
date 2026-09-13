package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito

/** Монета: жёлтый круг с тёмно-жёлтой обводкой. */
@Composable
fun CoinIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    borderWidth: Dp = 3.dp,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(size)
            .background(FinikColor.Coin, CircleShape)
            .border(borderWidth, FinikColor.CoinBorder, CircleShape),
    )
}

/** Чип «монета + число (+ подпись)» на жёлтом фоне. */
@Composable
fun CoinChip(
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    height: Dp = 40.dp,
    radius: Dp = 12.dp,
    coinSize: Dp = 20.dp,
    coinBorder: Dp = 3.dp,
    valueSize: Number = 17,
    background: Color = FinikColor.CoinChip,
) {
    Row(
        modifier = modifier
            .height(height)
            .background(background, RoundedCornerShape(radius))
            .padding(start = 7.dp, end = if (caption != null) 13.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (caption != null) 7.dp else 5.dp),
    ) {
        CoinIcon(size = coinSize, borderWidth = coinBorder)
        Text(text = value, style = nunito(valueSize, androidx.compose.ui.text.font.FontWeight.ExtraBold), color = FinikColor.CoinInk)
        if (caption != null) {
            Text(text = caption, style = nunito(11), color = FinikColor.CoinInk48)
        }
    }
}
