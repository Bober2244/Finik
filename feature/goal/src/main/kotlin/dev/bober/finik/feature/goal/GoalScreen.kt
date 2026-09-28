package dev.bober.finik.feature.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.ScreenTitle
import dev.bober.finik.core.designsystem.component.TitleRow
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.color
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.HistoryWeek
import dev.bober.finik.core.model.MoneyTransaction
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SavingsGoal
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TransactionKind
import dev.bober.finik.core.model.WeekPlan
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Вкладка «Мечта»: копилка, прогноз по факту и история реальных действий. */
@Composable
internal fun GoalScreen(
    modifier: Modifier = Modifier,
    goal: SavingsGoal = SampleData.goal,
    goals: List<SavingsGoal> = SampleData.goals,
    plan: WeekPlan = SampleData.plan,
    history: List<HistoryWeek> = SampleData.history,
    transactions: List<MoneyTransaction> = emptyList(),
    averageWeeklySave: Double? = null,
    weeksLeft: Int? = null,
    weeksDone: Int = 0,
    onDeposit: (Int) -> Unit = {},
    onSelectGoal: (String) -> Unit = {},
    onWithdraw: (Int) -> Unit = {},
) {
    var withdrawOpen by rememberSaveable { mutableStateOf(false) }
    val plannedSave = plan.entry(SpendCategory.SAVE).planned
    val availableToDeposit = plan.entry(SpendCategory.SAVE).left.coerceAtLeast(0)
    val actualAverage = averageWeeklySave?.takeIf { it > 0.0 }
    val withdrawAmount = 5
    val savedAfterWithdrawal = (goal.saved - withdrawAmount).coerceAtLeast(0)
    val weeksAfterWithdrawal = actualAverage?.let { ceil((goal.target - savedAfterWithdrawal).coerceAtLeast(0) / it).toInt() }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FinikColor.Background)
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ScreenTitle(text = "Мечта")
            GoalCard(goal = goal, plannedSave = plannedSave, averageWeeklySave = actualAverage, weeksLeft = weeksLeft, weeksDone = weeksDone)
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                goals.forEach { option ->
                    val selected = option.id == goal.id
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) FinikColor.GreenSelected else FinikColor.Surface)
                            .border(2.dp, if (selected) FinikColor.Green else FinikColor.Border, RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { onSelectGoal(option.id) }
                            .semantics {
                                contentDescription = "${option.title}, цель ${option.target} монет${if (selected) ", выбрана" else ""}"
                            }
                            .heightIn(min = 64.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(FinikIcons.Goal, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (selected) FinikColor.Green else FinikColor.Text50)
                        Text(text = option.title, style = nunito(14, FontWeight.SemiBold, lineHeight = 1.25), color = FinikColor.Ink, maxLines = 2)
                        Text(text = "${option.target} монет", style = nunito(12.5), color = FinikColor.Text50)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlineButton(
                    text = "Отложить 5",
                    onClick = { onDeposit(5) },
                    modifier = Modifier.weight(1f),
                    textStyle = nunito(15),
                    borderColor = FinikColor.GreenBorderBtn,
                    enabled = availableToDeposit >= 5,
                    icon = FinikIcons.Add,
                )
                PrimaryButton(
                    text = "Отложить всё",
                    onClick = { onDeposit(availableToDeposit) },
                    modifier = Modifier.weight(1f),
                    height = 52.dp,
                    radius = 14.dp,
                    textStyle = nunito(15),
                    enabled = availableToDeposit > 0,
                    icon = FinikIcons.Goal,
                )
            }
            Text(text = "Можно отложить: $availableToDeposit монет", style = nunito(13), color = FinikColor.Text46)
            if (goal.saved >= withdrawAmount) {
                OutlineButton(
                    text = "Снять $withdrawAmount",
                    onClick = { withdrawOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = FinikIcons.Remove,
                )
            }
            HistoryCard(history = history, transactions = transactions)
        }
        if (withdrawOpen) {
            FinikConfirmSheet(
                title = "Снять $withdrawAmount из копилки?",
                body = "Накопления станут $savedAfterWithdrawal монет. " +
                    if (weeksAfterWithdrawal != null) "При прежнем темпе до цели около $weeksAfterWithdrawal периодов."
                    else "Пока нет фактических пополнений, поэтому срок неизвестен.",
                confirmText = "Снять",
                warning = true,
                onConfirm = {
                    onWithdraw(withdrawAmount)
                    withdrawOpen = false
                },
                onDismiss = { withdrawOpen = false },
            )
        }
    }
}

@Composable
private fun GoalCard(goal: SavingsGoal, plannedSave: Int, averageWeeklySave: Double?, weeksLeft: Int?, weeksDone: Int) {
    val forecast = when {
        goal.remaining == 0 -> "Цель достигнута"
        averageWeeklySave != null && weeksLeft != null -> "факт ≈${averageWeeklySave.roundToInt()}/период, ≈$weeksLeft до цели"
        weeksDone > 0 -> "Срок появится после пополнения"
        else -> "Срок появится после первого периода"
    }
    FinikCard(
        radius = 20.dp,
        background = FinikColor.GreenCard,
        borderColor = null,
        contentPadding = PaddingValues(16.dp),
        gap = 0.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Jar(percent = goal.percent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(text = goal.title, style = nunito(15, lineHeight = 1.3), color = FinikColor.Ink)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(text = goal.saved.toString(), style = unbounded(30, lineHeight = 1), color = FinikColor.GreenInk34)
                    Text(
                        text = "из ${goal.target} · ${goal.percent}%",
                        style = nunito(14),
                        color = FinikColor.GreenInk44,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(text = "Осталось ${goal.remaining} монет", style = nunito(14, FontWeight.ExtraBold, lineHeight = 1.35), color = FinikColor.GreenInk34)
                Text(text = "План $plannedSave / период · $forecast", style = nunito(13.5, lineHeight = 1.4), color = FinikColor.GreenInk40s)
            }
        }
    }
}

/** Банка: 92×118, обводка 4dp, заливка монетным цветом снизу на `percent`, крышка 46×12 сверху. */
@Composable
private fun Jar(percent: Int) {
    Box(modifier = Modifier.size(width = 104.dp, height = 130.dp)) {
        val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 26.dp, bottomStart = 26.dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 92.dp, height = 118.dp)
                .clip(shape)
                .background(FinikColor.WhiteGlassSoft)
                .border(4.dp, FinikColor.GreenJar, shape),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(percent / 100f)
                    .background(FinikColor.Coin),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 46.dp, height = 12.dp)
                .background(FinikColor.GreenJar, RoundedCornerShape(6.dp)),
        )
    }
}

@Composable
private fun HistoryCard(history: List<HistoryWeek>, transactions: List<MoneyTransaction>) {
    FinikCard(gap = 11.dp) {
        TitleRow(title = "Что откладывал", trailing = "Периодов: ${history.size}", modifier = Modifier.fillMaxWidth())
        if (history.isEmpty()) {
            Text(text = "История появится после первого периода", style = nunito(14), color = FinikColor.Text46)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                history.takeLast(5).forEach { week ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = week.value.toString(), style = nunito(13), color = FinikColor.Text46)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(week.barHeight.dp)
                                .background(
                                    week.tone.color,
                                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomEnd = 4.dp, bottomStart = 4.dp),
                                ),
                        )
                        Text(text = week.label, style = nunito(13), color = FinikColor.Text54)
                    }
                }
            }
        }
        Text(text = "Последние действия", style = nunito(15, FontWeight.ExtraBold), color = FinikColor.Ink)
        if (transactions.isEmpty()) {
            Text(text = "Операций пока нет", style = nunito(14), color = FinikColor.Text46)
        } else transactions.takeLast(8).asReversed().forEach { item ->
            val incoming = item.kind == TransactionKind.INCOME || item.kind == TransactionKind.DEPOSIT
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.source, style = nunito(14, FontWeight.Bold), color = FinikColor.Ink)
                    Text(text = "Период ${item.week}" + (item.category?.let { " · ${it.label}" } ?: ""), style = nunito(12.5), color = FinikColor.Text50)
                }
                Text(text = "${if (incoming) "+" else "−"}${item.amount}", style = nunito(14, FontWeight.ExtraBold), color = if (incoming) FinikColor.GreenInk34 else FinikColor.Text42)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun GoalScreenPreview() {
    FinikTheme { GoalScreen() }
}
