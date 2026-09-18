package dev.bober.finik.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.BackHeader
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.SampleData

/** «Профиль»: три итоговые цифры, доход в неделю, достижения, звуки, сброс. */
@Composable
internal fun ProfileScreen(
    onBack: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenAdult: () -> Unit,
    modifier: Modifier = Modifier,
    earnedTotal: Int = SampleData.EARNED_TOTAL,
    savedTotal: Int = SampleData.SAVED_TOTAL,
    weeksDone: Int = SampleData.WEEKS_DONE,
    income: Int = SampleData.WEEKLY_INCOME,
    soundOn: Boolean = SampleData.SOUND_ON,
    badgesDone: Int = SampleData.badges.count { it.isDone },
    badgesTotal: Int = SampleData.badges.size,
    onIncome: (Int) -> Unit = {},
    onSound: (Boolean) -> Unit = {},
    onReset: () -> Unit = {},
) {
    var showReset by rememberSaveable { mutableStateOf(false) }
    var gateOpen by rememberSaveable { mutableStateOf(false) }
    val gateA = 7
    val gateB = 8

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Профиль", onBack = onBack)

        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            StatTile(
                value = earnedTotal.toString(), label = "монет заработано",
                background = FinikColor.CoinChip, valueColor = FinikColor.CoinInk36, labelColor = FinikColor.CoinInk46,
            )
            StatTile(
                value = savedTotal.toString(), label = "отложено всего",
                background = FinikColor.GreenCard, valueColor = FinikColor.GreenInk34, labelColor = FinikColor.GreenInk42s,
            )
            StatTile(
                value = weeksDone.toString(), label = "недель с планом",
                background = FinikColor.ChipStat, valueColor = FinikColor.TextWarm38, labelColor = FinikColor.Text48,
            )
        }

        FinikCard(radius = 16.dp, gap = 10.dp) {
            Text(text = "Доход в неделю", style = nunito(14.5), color = FinikColor.Ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleData.incomeOptions.forEach { (value, label) ->
                    IncomeOption(value = value, label = label, selected = income == value, onClick = { onIncome(value) })
                }
            }
        }

        SettingsRow(
            icon = { Box(modifier = Modifier.size(30.dp).background(FinikColor.Orange, RoundedCornerShape(9.dp))) },
            title = "Достижения",
            onClick = onOpenBadges,
        ) {
            Text(
                text = "$badgesDone/$badgesTotal",
                style = nunito(14, FontWeight.ExtraBold),
                color = FinikColor.Text46,
            )
        }

        SettingsRow(
            icon = { Box(modifier = Modifier.size(30.dp).background(FinikColor.Sound, CircleShape)) },
            title = "Звуки и подсказки",
            onClick = { onSound(!soundOn) },
        ) {
            Toggle(on = soundOn)
        }

        SettingsRow(
            icon = { Box(modifier = Modifier.size(30.dp).background(FinikColor.RedIcon, RoundedCornerShape(9.dp))) },
            title = "Начать заново",
            titleColor = FinikColor.RedReset,
            borderColor = FinikColor.RedBorderReset,
            onClick = { showReset = true },
        )

        SettingsRow(
            icon = { Box(modifier = Modifier.size(30.dp).background(FinikColor.Green, RoundedCornerShape(9.dp))) },
            title = "Раздел для взрослых",
            onClick = { gateOpen = true },
        ) {
            Text(text = "7+8", style = nunito(12), color = FinikColor.Text50)
        }
    }

    if (showReset) {
        FinikConfirmSheet(
            title = "Начать заново?",
            body = "Профиль, монеты и прогресс сотрутся.",
            confirmText = "Сбросить",
            warning = true,
            onConfirm = {
                showReset = false
                onReset()
            },
            onDismiss = { showReset = false },
        )
    }
    if (gateOpen) {
        FinikConfirmSheet(
            title = "Сколько будет $gateA + $gateB?",
            body = "Барьер для взрослого. Нажми правильную сумму.",
            confirmText = "${gateA + gateB}",
            cancelText = "${gateA + gateB + 4}",
            onConfirm = {
                gateOpen = false
                onOpenAdult()
            },
            onDismiss = { gateOpen = false },
        )
    }
    }
}

@Composable
private fun RowScope.StatTile(
    value: String,
    label: String,
    background: Color,
    valueColor: Color,
    labelColor: Color,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(background, RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = value, style = unbounded(24, lineHeight = 1), color = valueColor)
        Text(text = label, style = nunito(11.5), color = labelColor)
    }
}

@Composable
private fun RowScope.IncomeOption(value: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(if (selected) FinikColor.GreenSelected else FinikColor.Surface)
            .border(2.dp, if (selected) FinikColor.Green else FinikColor.Border, shape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(text = value.toString(), style = nunito(17, FontWeight.ExtraBold), color = FinikColor.Ink)
        Text(text = label, style = nunito(11), color = FinikColor.Text50)
    }
}

@Composable
internal fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = FinikColor.Ink,
    borderColor: Color = FinikColor.Border,
    shape: Shape = RoundedCornerShape(16.dp),
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(FinikColor.Surface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(text = title, style = nunito(15), color = titleColor, modifier = Modifier.weight(1f))
        trailing()
    }
}

/** Переключатель 54×32 с белым кружком 26dp. */
@Composable
private fun Toggle(on: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 54.dp, height = 32.dp)
            .background(if (on) FinikColor.Green else FinikColor.BorderStrong, RoundedCornerShape(16.dp))
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.size(26.dp).background(FinikColor.Surface, CircleShape))
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
    FinikTheme { ProfileScreen(onBack = {}, onOpenBadges = {}, onOpenAdult = {}) }
}
