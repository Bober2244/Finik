package dev.bober.finik.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
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
    enabled: Boolean = true,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(FinikColor.Background)
            .statusBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
    ) {
        val compact = maxWidth < 460.dp || LocalDensity.current.fontScale > 1.15f
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CoinChip(value = coins.toString(), caption = "свободно")
                    Spacer(modifier = Modifier.weight(1f))
                    TopBarActions(onHelpClick = onHelpClick, onProfileClick = onProfileClick, enabled = enabled)
                }
                StageChip(
                    stageName = stageName,
                    xpPercent = xpPercent,
                    onClick = onStageClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoinChip(value = coins.toString(), caption = "свободно")
                StageChip(stageName = stageName, xpPercent = xpPercent, onClick = onStageClick, enabled = enabled)
                Spacer(modifier = Modifier.weight(1f))
                TopBarActions(onHelpClick = onHelpClick, onProfileClick = onProfileClick, enabled = enabled)
            }
        }
    }
}

@Composable
private fun StageChip(stageName: String, xpPercent: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FinikColor.GreenChipStage)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = "Посмотреть рост питомца", onClick = onClick)
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
                    .fillMaxWidth(xpPercent.coerceIn(0, 100) / 100f)
                    .background(FinikColor.GreenInk34),
            )
        }
    }
}

@Composable
private fun TopBarActions(onHelpClick: (() -> Unit)?, onProfileClick: () -> Unit, enabled: Boolean) {
    if (onHelpClick != null) {
        SquareIconButton(onClick = onHelpClick, size = 48.dp, contentDescription = "Помощь", enabled = enabled) {
            Text(text = "?", style = nunito(16), color = FinikColor.Ink)
        }
    }

    SquareIconButton(onClick = onProfileClick, size = 48.dp, contentDescription = "Профиль", enabled = enabled) {
        Icon(
            imageVector = FinikIcons.Profile,
            contentDescription = null,
            tint = FinikColor.Text44,
            modifier = Modifier.size(24.dp),
        )
    }
}
