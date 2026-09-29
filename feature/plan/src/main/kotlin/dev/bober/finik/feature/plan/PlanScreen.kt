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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikIcons
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
    actionsEnabled: Boolean = true,
    onAdjust: (SpendCategory, Int) -> Unit = { _, _ -> },
    onAdvice: () -> Unit = {},
    onReset: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UnallocatedCard(plan = plan)
        Text(
            text = if (planConfirmed) "Добавляй свободные монеты в любую статью. Уже распределённые деньги переносить нельзя." else "Можно подтвердить план, оставив часть монет свободными.",
            style = nunito(13), color = FinikColor.Text46,
        )
        val availableBudget = (plan.total + plan.freeCoins).coerceAtLeast(1)
        plan.entries.forEach { entry ->
            PlanEntryCard(
                entry = entry,
                allocationPercent = (entry.planned * 100f / availableBudget).toInt(),
                allocationProgress = entry.planned.toFloat() / availableBudget,
                confirmed = planConfirmed,
                canIncrease = actionsEnabled && plan.freeCoins > 0,
                actionsEnabled = actionsEnabled,
                onAdjust = { delta -> onAdjust(entry.category, delta) },
            )
        }
        if (!planConfirmed) Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            OutlineButton(
                text = "Распределить",
                onClick = onAdvice,
                enabled = actionsEnabled,
                modifier = Modifier.weight(1f),
                icon = FinikIcons.Advice,
            )
            OutlineButton(
                text = "Сброс",
                onClick = onReset,
                enabled = actionsEnabled,
                modifier = Modifier.width(108.dp),
                textStyle = nunito(14),
                icon = FinikIcons.Reset,
            )
        }
        if (!planConfirmed) {
            PrimaryButton(
                text = "Подтвердить план",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = actionsEnabled,
                icon = FinikIcons.Confirm,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(FinikIcons.Confirm, contentDescription = null, modifier = Modifier.size(20.dp), tint = FinikColor.Green)
                Text(text = "План действует · можно пополнять статьи", style = nunito(14, FontWeight.SemiBold), color = FinikColor.Text46)
            }
        }
    }
}

@Composable
private fun UnallocatedCard(plan: WeekPlan) {
    val savePercent = plan.percentOf(SpendCategory.SAVE)
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
                    text = "СВОБОДНО",
                    style = nunito(11.5).copy(letterSpacing = 0.1.em),
                    color = FinikColor.CoinInk47,
                )
                Text(text = plan.freeCoins.toString(), style = unbounded(32, lineHeight = 1), color = FinikColor.CoinInk34)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = "Доход ${plan.weeklyIncome} / нед.", style = nunito(12.5), color = FinikColor.CoinInk44)
                Text(text = "Копилка $savePercent%", style = nunito(12.5), color = FinikColor.CoinInk44)
            }
        }
        Text(
            text = "Распределено ${plan.total} / ${plan.total + plan.freeCoins}",
            style = nunito(12.5, FontWeight.SemiBold),
            color = FinikColor.CoinInk44,
        )
        SegmentedBar(
            segments = plan.entries.map { it.planned.toFloat() to it.category.color },
            totalWeight = (plan.total + plan.freeCoins).toFloat(),
            track = FinikColor.IconButton,
        )
        if (plan.freeCoins == 0 && savePercent < 20) {
            Text(text = "Копилка ниже 20%", style = nunito(13, FontWeight.SemiBold), color = FinikColor.CoinInk42)
        }
    }
}

@Composable
private fun PlanEntryCard(
    entry: PlanEntry,
    allocationPercent: Int,
    allocationProgress: Float,
    confirmed: Boolean,
    canIncrease: Boolean,
    actionsEnabled: Boolean,
    onAdjust: (Int) -> Unit,
) {
    val categoryIcon = when (entry.category) {
        SpendCategory.FOOD -> FinikIcons.Food
        SpendCategory.WATER -> FinikIcons.Water
        SpendCategory.PLAY -> FinikIcons.Play
        SpendCategory.SAVE -> FinikIcons.Goal
    }
    FinikCard(
        radius = 16.dp,
        borderColor = if (entry.spent > entry.planned) FinikColor.RedBorderPlan else FinikColor.Border,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
        gap = 9.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(entry.category.color.copy(alpha = 0.16f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(categoryIcon, contentDescription = null, modifier = Modifier.size(20.dp), tint = entry.category.color)
            }
            Text(text = entry.category.label, style = nunito(14.5), color = FinikColor.Ink, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                StepperButton(icon = FinikIcons.Remove, label = "Уменьшить бюджет: ${entry.category.label}", onClick = { onAdjust(-1) }, enabled = actionsEnabled && !confirmed && entry.planned > entry.spent)
                Text(
                    text = entry.planned.toString(),
                    style = nunito(18, FontWeight.ExtraBold),
                    color = FinikColor.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(34.dp),
                )
                StepperButton(icon = FinikIcons.Add, label = "Увеличить бюджет: ${entry.category.label}", onClick = { onAdjust(1) }, enabled = canIncrease)
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
                text = "$allocationPercent%",
                style = nunito(13),
                color = FinikColor.Text50,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        Text(
            text = "Потрачено ${entry.spent} · Осталось ${entry.left.coerceAtLeast(0)}",
            style = nunito(13, FontWeight.SemiBold),
            color = FinikColor.Text46,
        )
    }
}

@Composable
private fun StepperButton(icon: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean = true) {
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
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = if (enabled) FinikColor.Ink else FinikColor.Text58)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun PlanScreenPreview() {
    FinikTheme { PlanScreen() }
}
