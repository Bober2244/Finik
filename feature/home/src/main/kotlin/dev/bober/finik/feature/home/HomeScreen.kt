package dev.bober.finik.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SegmentedBar
import dev.bober.finik.core.designsystem.component.TitleRow
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
import dev.bober.finik.core.model.TodayEvent
import dev.bober.finik.core.model.WordOfDay
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
    actionsEnabled: Boolean = true,
    demoMode: Boolean = false,
    canAdvanceTime: Boolean = false,
    weekLabel: String = "Календарная неделя",
    todayEvent: TodayEvent? = null,
    wordOfDay: WordOfDay? = null,
    onChooseEvent: (String, String) -> Unit = { _, _ -> },
    onAdvanceDay: () -> Unit = {},
    onOpenStories: () -> Unit = {},
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
    var confirmClose by rememberSaveable { mutableStateOf(false) }
    var petInteractionActive by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), enabled = !petInteractionActive)
            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeroCard(
            pet = pet,
            needs = needs,
            action = action,
            actionEventId = actionEventId,
            onPetInteractionChange = { petInteractionActive = it },
        )
        Text(text = weekLabel, style = nunito(13), color = FinikColor.Text46)
        CareRow(actions = care, plan = plan, planConfirmed = planConfirmed, actionsEnabled = actionsEnabled, onCare = onCare)
        todayEvent?.let { event ->
            FinikCard(gap = 8.dp) {
                Text(text = event.title, style = nunito(16, FontWeight.ExtraBold), color = FinikColor.Ink)
                Text(text = event.text, style = nunito(14), color = FinikColor.Text46)
                if (event.chosen == null) event.options.forEach { option ->
                    OutlineButton(text = option.label, onClick = { onChooseEvent(event.id, option.key) }, enabled = actionsEnabled, modifier = Modifier.fillMaxWidth())
                } else Text(text = "Решение принято", style = nunito(13), color = FinikColor.Green)
            }
        }
        wordOfDay?.let { word ->
            FinikCard(gap = 5.dp) {
                Text(text = "Слово дня: ${word.word}", style = nunito(15, FontWeight.Bold), color = FinikColor.Ink)
                Text(text = word.meaning, style = nunito(13), color = FinikColor.Text46)
            }
        }
        OutlineButton(text = "Поговорить с совой", onClick = onOpenStories, modifier = Modifier.fillMaxWidth())
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
        if (demoMode && canAdvanceTime) {
            OutlineButton(text = "Демо: следующий день", onClick = onAdvanceDay, enabled = actionsEnabled, modifier = Modifier.fillMaxWidth())
            PrimaryButton(
                text = "Демо: завершить неделю",
                enabled = actionsEnabled,
                onClick = { confirmClose = true },
                modifier = Modifier.fillMaxWidth(),
                icon = FinikIcons.TaskWeek,
            )
        } else if (!planConfirmed) {
            PrimaryButton(
                text = if (plan.freeCoins > 0) "Распределить ${plan.freeCoins} монет" else "Подтвердить план",
                onClick = onOpenPlan,
                modifier = Modifier.fillMaxWidth(),
                icon = if (plan.freeCoins > 0) FinikIcons.Plan else FinikIcons.Confirm,
            )
        }
    }
    if (confirmClose) {
        FinikConfirmSheet(
            title = "Перейти к следующей неделе в демо?",
            body = "Время демопрофиля ускорится. Сервер рассчитает расходы, накопления и новый доход. Обычная игра не изменится.",
            confirmText = "Завершить",
            onConfirm = {
                confirmClose = false
                onCloseWeek()
            },
            onDismiss = { confirmClose = false },
        )
    }
}

@Composable
private fun HeroCard(
    pet: PetProfile,
    needs: List<NeedLevel>,
    action: PetAnimation,
    actionEventId: Long,
    onPetInteractionChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val petSize = (maxWidth * .76f).coerceAtMost(250.dp)
            val stackedNeeds = maxWidth < 290.dp || LocalDensity.current.fontScale > 1.2f
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = pet.name,
                    style = unbounded(22, lineHeight = 1.1),
                    color = FinikColor.Ink,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${pet.stage.name} · ${pet.mood.label}",
                    style = nunito(12.5),
                    color = FinikColor.TextWarm44,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp),
                )
                PetFigure(
                    species = pet.species,
                    spec = PetFigureSpec(
                        width = petSize,
                        height = petSize,
                        stageIndex = pet.stageIndex,
                        live3d = true,
                        portraitScale = 1.04f,
                        modelScaleMultiplier = 1.45f,
                    ),
                    mood = pet.mood,
                    appearance = pet.appearance,
                    action = action,
                    actionEventId = actionEventId,
                    onInteractionChange = onPetInteractionChange,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (stackedNeeds) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        needs.forEach { need -> NeedIndicator(need, Modifier.fillMaxWidth()) }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        needs.forEach { need ->
                            NeedIndicator(need, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NeedIndicator(need: NeedLevel, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = need.category.label, style = nunito(11.5), color = FinikColor.Text45)
            Text(text = "${need.percent}%", style = nunito(11.5), color = FinikColor.Text45)
        }
        FinikProgressBar(
            progress = need.percent / 100f,
            color = if (need.category == SpendCategory.FOOD) FinikColor.FoodBright else need.category.color,
            height = 8.dp,
        )
    }
}

@Composable
private fun CareRow(
    actions: List<CareAction>,
    plan: WeekPlan,
    planConfirmed: Boolean,
    actionsEnabled: Boolean,
    onCare: (SpendCategory) -> Unit,
) {
    if (LocalDensity.current.fontScale > 1.2f) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            actions.forEach { action ->
                val left = plan.entry(action.category).left
                CareButton(
                    action = action,
                    left = left,
                    enabled = actionsEnabled && planConfirmed && (left >= action.cost || (action.category != SpendCategory.PLAY && plan.entry(SpendCategory.SAVE).left >= action.cost)),
                    horizontal = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onCare(action.category) },
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            actions.forEach { action ->
                val left = plan.entry(action.category).left
                CareButton(
                    action = action,
                    left = left,
                    enabled = actionsEnabled && planConfirmed && (left >= action.cost || (action.category != SpendCategory.PLAY && plan.entry(SpendCategory.SAVE).left >= action.cost)),
                    horizontal = false,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = { onCare(action.category) },
                )
            }
        }
    }
}

@Composable
private fun CareButton(
    action: CareAction,
    left: Int,
    enabled: Boolean,
    horizontal: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val container = modifier
            .heightIn(min = if (horizontal) 70.dp else 94.dp)
            .clip(shape)
            .background(if (enabled) FinikColor.Surface else FinikColor.SurfaceSoft)
            .border(1.dp, if (enabled) FinikColor.Border else FinikColor.RedBorderCare, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (horizontal) 14.dp else 6.dp, vertical = 10.dp)
    val icon = when (action.category) {
        SpendCategory.FOOD -> FinikIcons.Food
        SpendCategory.WATER -> FinikIcons.Water
        SpendCategory.PLAY -> FinikIcons.Play
        SpendCategory.SAVE -> FinikIcons.Goal
    }
    if (horizontal) {
        Row(
            modifier = container,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(26.dp), tint = action.category.color)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CareButtonText(action, left, enabled)
            }
        }
    } else {
        Column(
            modifier = container,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(26.dp), tint = action.category.color)
            CareButtonText(action, left, enabled)
        }
    }
}

@Composable
private fun CareButtonText(action: CareAction, left: Int, enabled: Boolean) {
    Text(text = action.label, style = nunito(14), color = FinikColor.Ink)
    Text(
        text = if (enabled && left < action.cost) "−${action.cost} мон. из копилки" else "−${action.cost} мон. · $left осталось",
        style = nunito(11.5),
        color = if (enabled) FinikColor.Text48 else FinikColor.RedCost,
        textAlign = TextAlign.Center,
    )
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
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        FinikCard(
            modifier = Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = onOpenGoal),
            radius = 16.dp,
            background = FinikColor.GreenCard,
            borderColor = null,
            gap = 6.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier.size(30.dp).background(FinikColor.GreenPill, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FinikIcons.Goal, contentDescription = null, modifier = Modifier.size(18.dp), tint = FinikColor.GreenInk40)
                }
                Text(text = "Копилка", style = nunito(13, FontWeight.Bold), color = FinikColor.GreenInk42, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = FinikColor.GreenInk42)
            }
            Text(text = "$saved / $target", style = unbounded(18, lineHeight = 1), color = FinikColor.GreenInk34)
            Text(text = title, style = nunito(12, FontWeight.SemiBold, lineHeight = 1.3), color = FinikColor.GreenInk40s, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FinikCard(
            modifier = Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = onOpenTasks),
            radius = 16.dp,
            gap = 6.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier.size(30.dp).background(FinikColor.Chip, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(FinikIcons.Tasks, contentDescription = null, modifier = Modifier.size(18.dp), tint = FinikColor.Text40)
                }
                Text(text = "Задания", style = nunito(13, FontWeight.Bold), color = FinikColor.Text40, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = FinikColor.Text50)
            }
            Text(
                text = task ?: "Все задания сделаны",
                style = nunito(14, FontWeight.SemiBold, lineHeight = 1.3),
                color = FinikColor.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
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
            OutlineButton(
                text = "Открыть",
                onClick = onEdit,
                height = 48.dp,
                icon = Icons.Rounded.Edit,
            )
        }
        SegmentedBar(
            segments = plan.entries.map { it.planned.toFloat() to it.category.color },
            totalWeight = (plan.total + plan.freeCoins).toFloat(),
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
            title = "Игровые дни",
            trailing = "$done / ${days.size} · +5 монет",
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
