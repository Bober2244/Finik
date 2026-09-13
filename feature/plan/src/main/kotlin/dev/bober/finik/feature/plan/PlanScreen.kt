package dev.bober.finik.feature.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.color
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.PlanEntry
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.WeekPlan

/** Вкладка «План»: нераспределённые монеты, статьи со степперами, совет 40/30/10/20. */
@Composable
internal fun PlanScreen(
    modifier: Modifier = Modifier,
    plan: WeekPlan = SampleData.plan,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UnallocatedCard(plan = plan)
        plan.entries.forEach { entry ->
            PlanEntryCard(entry = entry, percent = plan.percentOf(entry.category))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            OutlineButton(
                text = "Совет 40/30/10/20",
                onClick = { /* TODO(logic): авто-распределение */ },
                modifier = Modifier.weight(1f),
            )
            OutlineButton(
                text = "↺",
                onClick = { /* TODO(logic): сброс плана */ },
                modifier = Modifier.width(52.dp),
                textStyle = nunito(16),
            )
        }
    }
}

@Composable
private fun UnallocatedCard(plan: WeekPlan) {
    val savePercent = plan.percentOf(SpendCategory.SAVE)
    val hint = when {
        plan.freeCoins > 0 -> "Разложи остаток: сначала еда и вода, потом игры, в копилку — не меньше 20%."
        savePercent < 20 -> "В копилке меньше 20%. До цели так идти долго."
        else -> "План собран. Теперь траты идут только из статей."
    }
    FinikCard(
        radius = 20.dp,
        background = FinikColor.CoinChip,
        borderColor = null,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        gap = 10.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "НЕ РАСПРЕДЕЛЕНО",
                    style = nunito(11.5).copy(letterSpacing = 0.1.em),
                    color = FinikColor.CoinInk47,
                )
                Text(text = plan.freeCoins.toString(), style = unbounded(32, lineHeight = 1), color = FinikColor.CoinInk34)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = "доход ${plan.weeklyIncome} / неделю", style = nunito(12.5), color = FinikColor.CoinInk44)
                Text(text = "в копилку $savePercent%", style = nunito(12.5), color = FinikColor.CoinInk44)
            }
        }
        Text(text = hint, style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45), color = FinikColor.CoinInk42)
    }
}

@Composable
private fun PlanEntryCard(entry: PlanEntry, percent: Int) {
    FinikCard(
        radius = 16.dp,
        borderColor = if (entry.left <= 0) FinikColor.RedBorderPlan else FinikColor.Border,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
        gap = 9.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(14.dp).background(entry.category.color, RoundedCornerShape(5.dp)))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(text = entry.category.label, style = nunito(14.5), color = FinikColor.Ink)
                Text(text = entry.category.note, style = nunito(11.5, FontWeight.SemiBold), color = FinikColor.Text50)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                StepperButton(text = "−", onClick = { /* TODO(logic) */ })
                Text(
                    text = entry.planned.toString(),
                    style = nunito(18, FontWeight.ExtraBold),
                    color = FinikColor.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(34.dp),
                )
                StepperButton(text = "+", onClick = { /* TODO(logic) */ })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            FinikProgressBar(
                progress = percent / 100f,
                color = entry.category.color,
                track = FinikColor.IconButton,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$percent% · ост. ${entry.left}",
                style = nunito(11.5),
                color = FinikColor.Text50,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.width(100.dp),
            )
        }
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(FinikColor.SurfaceMuted)
            .border(1.dp, FinikColor.Border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = nunito(20), color = FinikColor.Ink)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun PlanScreenPreview() {
    FinikTheme { PlanScreen() }
}
