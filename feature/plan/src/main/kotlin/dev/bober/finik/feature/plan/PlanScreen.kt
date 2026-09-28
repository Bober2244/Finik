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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SegmentedBar
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
    planConfirmed: Boolean = false,
    onAdjust: (SpendCategory, Int) -> Unit = { _, _ -> },
    onAdvice: () -> Unit = {},
    onReset: () -> Unit = {},
    onConfirm: () -> Unit = {},
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
        val availableBudget = (plan.total + plan.freeCoins).coerceAtLeast(1)
        plan.entries.forEach { entry ->
            PlanEntryCard(
                entry = entry,
                allocationPercent = (entry.planned * 100f / availableBudget).toInt(),
                allocationProgress = entry.planned.toFloat() / availableBudget,
                confirmed = planConfirmed,
                onAdjust = { delta -> onAdjust(entry.category, delta) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            OutlineButton(
                text = "Совет 40/30/10/20",
                onClick = onAdvice,
                modifier = Modifier.weight(1f),
            )
            OutlineButton(
                text = "Сброс",
                onClick = onReset,
                modifier = Modifier.width(80.dp),
                textStyle = nunito(14),
            )
        }
        if (!planConfirmed) {
            PrimaryButton(
                text = if (plan.freeCoins == 0) "Подтвердить план" else "Распредели ещё ${plan.freeCoins} монет",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = plan.freeCoins == 0,
            )
        } else {
            Text(
                text = "План подтверждён. Покупки и уход уменьшают остаток нужной статьи.",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.4),
                color = FinikColor.Text46,
            )
        }
    }
}

@Composable
private fun UnallocatedCard(plan: WeekPlan) {
    val savePercent = plan.percentOf(SpendCategory.SAVE)
    val hint = when {
        plan.freeCoins > 0 -> "Совет: еда 40%, вода 30%, игры 10%, копилка 20%."
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
        Text(
            text = "Распределено ${plan.total} из ${plan.total + plan.freeCoins}",
            style = nunito(12.5, FontWeight.SemiBold),
            color = FinikColor.CoinInk44,
        )
        SegmentedBar(
            segments = plan.entries.map { it.planned.toFloat() to it.category.color },
            totalWeight = (plan.total + plan.freeCoins).toFloat(),
            track = FinikColor.IconButton,
        )
        Text(text = hint, style = nunito(14, FontWeight.SemiBold, lineHeight = 1.45), color = FinikColor.CoinInk42)
    }
}

@Composable
private fun PlanEntryCard(
    entry: PlanEntry,
    allocationPercent: Int,
    allocationProgress: Float,
    confirmed: Boolean,
    onAdjust: (Int) -> Unit,
) {
    FinikCard(
        radius = 16.dp,
        borderColor = if (entry.spent > entry.planned) FinikColor.RedBorderPlan else FinikColor.Border,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
        gap = 9.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(14.dp).background(entry.category.color, RoundedCornerShape(5.dp)))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(text = entry.category.label, style = nunito(14.5), color = FinikColor.Ink)
                Text(text = entry.category.note, style = nunito(13, FontWeight.SemiBold), color = FinikColor.Text50)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                StepperButton(text = "−", label = "Уменьшить бюджет: ${entry.category.label}", onClick = { onAdjust(-1) }, enabled = !confirmed)
                Text(
                    text = entry.planned.toString(),
                    style = nunito(18, FontWeight.ExtraBold),
                    color = FinikColor.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(34.dp),
                )
                StepperButton(text = "+", label = "Увеличить бюджет: ${entry.category.label}", onClick = { onAdjust(1) }, enabled = !confirmed)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            FinikProgressBar(
                progress = allocationProgress,
                color = entry.category.color,
                track = FinikColor.IconButton,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$allocationPercent% бюджета",
                style = nunito(13),
                color = FinikColor.Text50,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        Text(
            text = "${entry.spent} из ${entry.planned} потрачено · осталось ${entry.left.coerceAtLeast(0)} монет",
            style = nunito(13, FontWeight.SemiBold),
            color = FinikColor.Text46,
        )
    }
}

@Composable
private fun StepperButton(text: String, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(FinikColor.SurfaceMuted)
            .border(1.dp, FinikColor.BorderStrong, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = nunito(20), color = if (enabled) FinikColor.Ink else FinikColor.Text58)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun PlanScreenPreview() {
    FinikTheme { PlanScreen() }
}
