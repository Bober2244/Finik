package dev.bober.finik.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.GradientCard
import dev.bober.finik.core.designsystem.component.LinkButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SegmentedBar
import dev.bober.finik.core.designsystem.component.ShapeDot
import dev.bober.finik.core.designsystem.component.TitleRow
import dev.bober.finik.core.designsystem.component.glyphShape
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.color
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.CareAction
import dev.bober.finik.core.model.NeedLevel
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.StreakDay
import dev.bober.finik.core.model.WeekPlan
import dev.bober.finik.core.pet.PetAnimation
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/**
 * Вкладка «Питомец»: герой с потребностями, три действия ухода,
 * карточка плана недели и полоса ежедневных входов.
 */
@Composable
internal fun HomeScreen(
    onOpenPlan: () -> Unit,
    modifier: Modifier = Modifier,
    pet: PetProfile = SampleData.pet,
    needs: List<NeedLevel> = SampleData.needs,
    plan: WeekPlan = SampleData.plan,
    care: List<CareAction> = SampleData.careActions,
    streak: List<StreakDay> = SampleData.streakDays,
    planConfirmed: Boolean = true,
    demoMode: Boolean = true,
    goalTitle: String = SampleData.goal.title,
    goalSaved: Int = SampleData.goal.saved,
    goalTarget: Int = SampleData.goal.target,
    activeTask: String? = SampleData.tasks.first().title,
    action: PetAnimation = PetAnimation.IDLE,
    actionEventId: Long = 0L,
    onCare: (SpendCategory) -> Unit = {},
    onOpenGoal: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onCloseWeek: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeroCard(pet = pet, needs = needs, action = action, actionEventId = actionEventId)
        CareRow(actions = care, plan = plan, planConfirmed = planConfirmed, onCare = onCare)
        SnapshotRow(
            saved = goalSaved,
            target = goalTarget,
            title = goalTitle,
            task = activeTask,
            onOpenGoal = onOpenGoal,
            onOpenTasks = onOpenTasks,
        )
        PlanCard(plan = plan, onEdit = onOpenPlan)
        StreakCard(days = streak)
        if (demoMode && planConfirmed) {
            PrimaryButton(
                text = "Закрыть неделю",
                onClick = onCloseWeek,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (!planConfirmed) {
            FinikCard(background = FinikColor.CoinChip, borderColor = null) {
                Text(
                    text = "Сначала разложи ${plan.freeCoins} свободных монет в плане: нужное, желаемое, копилка.",
                    style = nunito(13, FontWeight.SemiBold, lineHeight = 1.4),
                    color = FinikColor.CoinInk42,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(pet: PetProfile, needs: List<NeedLevel>, action: PetAnimation, actionEventId: Long) {
    GradientCard(
        brush = Brush.verticalGradient(listOf(FinikColor.GreenHeroTop, FinikColor.SurfaceCream)),
        radius = 20.dp,
        gap = 0.dp,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PetFigure(
                species = pet.species,
                spec = PetFigureSpec.hero(pet.stageIndex),
                mood = pet.mood,
                appearance = pet.appearance,
                action = action,
                actionEventId = actionEventId,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = pet.name, style = unbounded(19, lineHeight = 1.1), color = FinikColor.Ink)
                    Text(
                        text = "${pet.stage.name} · ${pet.mood.label}",
                        style = nunito(12.5),
                        color = FinikColor.TextWarm44,
                    )
                }
                needs.forEach { need ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = need.category.label, style = nunito(11.5), color = FinikColor.Text45)
                            Text(text = "${need.percent}%", style = nunito(11.5), color = FinikColor.Text45)
                        }
                        FinikProgressBar(
                            progress = need.percent / 100f,
                            color = if (need.category == SpendCategory.FOOD) FinikColor.FoodBright else need.category.color,
                            height = 9.dp,
                        )
                    }
                }
                Text(
                    text = pet.moodNote,
                    style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.4),
                    color = FinikColor.Text42,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun CareRow(
    actions: List<CareAction>,
    plan: WeekPlan,
    planConfirmed: Boolean,
    onCare: (SpendCategory) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        actions.forEach { action ->
            val left = plan.entry(action.category).left
            val canAfford = planConfirmed && left >= action.cost
            CareButton(action = action, left = left, enabled = canAfford, onClick = { onCare(action.category) })
        }
    }
}

@Composable
private fun RowScope.CareButton(
    action: CareAction,
    left: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 94.dp)
            .clip(shape)
            .background(if (enabled) FinikColor.Surface else FinikColor.SurfaceSoft)
            .border(1.dp, if (enabled) FinikColor.Border else FinikColor.RedBorderCare, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        ShapeDot(color = action.category.color, shape = action.category.glyphShape, size = 26.dp)
        Text(text = action.label, style = nunito(14), color = FinikColor.Ink)
        Text(
            text = "${action.cost} из $left · ${action.category.label}",
            style = nunito(11.5),
            color = if (enabled) FinikColor.Text48 else FinikColor.RedCost,
        )
    }
}

@Composable
private fun SnapshotRow(
    saved: Int,
    target: Int,
    title: String,
    task: String?,
    onOpenGoal: () -> Unit,
    onOpenTasks: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        FinikCard(
            modifier = Modifier.weight(1f).clickable(onClick = onOpenGoal),
            radius = 16.dp,
            background = FinikColor.GreenCard,
            borderColor = null,
            gap = 6.dp,
        ) {
            Text(text = "Копилка", style = nunito(12), color = FinikColor.GreenInk42)
            Text(text = "$saved / $target", style = unbounded(18, lineHeight = 1), color = FinikColor.GreenInk34)
            Text(text = title, style = nunito(12, FontWeight.SemiBold, lineHeight = 1.3), color = FinikColor.GreenInk40s, maxLines = 2)
        }
        FinikCard(
            modifier = Modifier.weight(1f).clickable(onClick = onOpenTasks),
            radius = 16.dp,
            gap = 6.dp,
        ) {
            Text(text = "Задание", style = nunito(12), color = FinikColor.Text50)
            Text(
                text = task ?: "Все задания сделаны",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.3),
                color = FinikColor.Ink,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun PlanCard(plan: WeekPlan, onEdit: () -> Unit) {
    FinikCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(text = "План недели", style = nunito(14.5), color = FinikColor.Ink)
            LinkButton(text = "изменить", onClick = onEdit)
        }
        SegmentedBar(
            segments = plan.entries.map { it.planned.toFloat() to it.category.color },
            height = 14.dp,
        )
        val rows = plan.entries.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEach { entry ->
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(entry.category.color, RoundedCornerShape(4.dp)),
                            )
                            Text(text = entry.category.label, style = nunito(12.5), color = FinikColor.Text40)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "${entry.left} / ${entry.planned}", style = nunito(12.5), color = FinikColor.Text50)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(days: List<StreakDay>) {
    val done = days.count { it.done }
    FinikCard(gap = 10.dp) {
        TitleRow(
            title = "Вход каждый день",
            trailing = "$done из ${days.size} · на седьмой день +5",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(
                            if (day.done) FinikColor.Coin else FinikColor.Chip,
                            RoundedCornerShape(10.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = day.label,
                        style = nunito(12),
                        color = if (day.done) FinikColor.CoinInkStreak else FinikColor.Text58,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun HomeScreenPreview() {
    FinikTheme { HomeScreen(onOpenPlan = {}) }
}
