package dev.bober.finik.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.BackHeader
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.GlossaryTerm

@Composable
internal fun HelpScreen(
    terms: List<GlossaryTerm>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Подсказка", onBack = onBack)
        FinikCard(background = FinikColor.GreenCard, borderColor = null, gap = 10.dp) {
            Text(text = "Три решения каждую неделю", style = nunito(15), color = FinikColor.Ink)
            Text(
                text = "1. Потратить на нужное — еда и вода.\n2. Потратить на желаемое — игрушки, можно подождать.\n3. Отложить в копилку — на мечту.",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.GreenInk40s,
            )
        }
        terms.forEach { term ->
            FinikCard(gap = 6.dp) {
                Text(text = term.term, style = nunito(15), color = FinikColor.Ink)
                Text(text = term.meaning, style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4), color = FinikColor.Text46)
            }
        }
    }
}

@Composable
internal fun AdultScreen(
    weeksDone: Int,
    saved: Int,
    stageName: String,
    tasksDone: Int,
    tasksTotal: Int,
    demoMode: Boolean,
    onBack: () -> Unit,
    onToggleDemo: (Boolean) -> Unit,
    onBonus: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Для взрослых", onBack = onBack)
        Text(
            text = "Зачем это приложение",
            style = unbounded(19, lineHeight = 1.2),
            color = FinikColor.Ink,
        )
        FinikCard(gap = 8.dp) {
            Text(
                text = "Ребёнок учится делить карманные монеты на нужное, желаемое и накопления. Оценки и сравнения с другими детьми здесь нет.",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.Text42,
            )
        }
        FinikCard(gap = 8.dp) {
            Text(text = "Общий прогресс", style = nunito(15), color = FinikColor.Ink)
            Text(text = "Недель с планом: $weeksDone", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Отложено на цель: $saved", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Стадия питомца: $stageName", style = nunito(14), color = FinikColor.Text46)
            Text(text = "Темы: план, копилка, покупки · задания $tasksDone из $tasksTotal", style = nunito(14), color = FinikColor.Text46)
        }
        SettingsRow(
            icon = {},
            title = if (demoMode) "Демо: недели без календаря" else "Обычный режим",
            onClick = { onToggleDemo(!demoMode) },
        ) {
            ToggleLite(on = demoMode)
        }
        PrimaryButton(
            text = "Добавить 10 монет поддержки",
            onClick = onBonus,
            modifier = Modifier.fillMaxWidth(),
        )
        SettingsRow(
            icon = {},
            title = "Сбросить тестовый профиль",
            titleColor = FinikColor.RedReset,
            borderColor = FinikColor.RedBorderReset,
            onClick = onReset,
        )
    }
}

@Composable
private fun ToggleLite(on: Boolean) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(if (on) FinikColor.Green else FinikColor.BorderStrong, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(3.dp)
            .then(Modifier),
    ) {
        Text(text = if (on) "вкл" else "выкл", style = nunito(12), color = FinikColor.Surface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
