package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito

/**
 * Верхняя панель приложения: чип монет «12 свободно», кнопка стадии с полоской опыта
 * и круглая кнопка профиля. `padding: 10px 14px 8px`.
 */
@Composable
fun FinikShellTopBar(
    coins: Int,
    stageName: String,
    xpPercent: Int,
    onStageClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHelpClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FinikColor.Background)
            .statusBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CoinChip(value = coins.toString(), caption = "свободно")

        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FinikColor.GreenChipStage)
                .clickable(onClick = onStageClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = stageName,
                style = nunito(13, androidx.compose.ui.text.font.FontWeight.ExtraBold),
                color = FinikColor.GreenInk38,
            )
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FinikColor.GreenTrack),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(xpPercent / 100f)
                        .background(FinikColor.Green),
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (onHelpClick != null) {
            SquareIconButton(onClick = onHelpClick, size = 40.dp) {
                Text(text = "?", style = nunito(16), color = FinikColor.Ink)
            }
        }

        SquareIconButton(onClick = onProfileClick, size = 40.dp) {
            Box(modifier = Modifier.size(16.dp).background(FinikColor.Avatar, CircleShape))
        }
    }
}
