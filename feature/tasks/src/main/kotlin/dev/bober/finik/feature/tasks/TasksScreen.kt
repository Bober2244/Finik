package dev.bober.finik.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.CoinIcon
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import dev.bober.finik.core.designsystem.component.ScreenTitle
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.chipBackground
import dev.bober.finik.core.designsystem.theme.chipInk
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.ScenarioTask
import dev.bober.finik.core.model.TaskItem
import dev.bober.finik.core.model.TaskKind
import dev.bober.finik.core.model.TaskTarget

/** Вкладка «Задания»: список заданий с наградой; мини-урок открывает квиз-шторку. */
@Composable
internal fun TasksScreen(
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenShop: () -> Unit,
    modifier: Modifier = Modifier,
    tasks: List<TaskItem> = SampleData.tasks,
    quiz: List<QuizQuestion> = SampleData.quiz,
    scenarios: List<ScenarioTask> = emptyList(),
    onComplete: (taskId: String, correct: Boolean, reward: Int) -> Unit = { _, _, _ -> },
) {
    var quizTaskId by rememberSaveable { mutableStateOf<String?>(null) }
    var scenarioId by rememberSaveable { mutableStateOf<String?>(null) }
    var unavailable by rememberSaveable { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FinikColor.Background)
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val done = tasks.count { it.done }
            val available = tasks.filter { !it.done }.sumOf { it.reward }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ScreenTitle(text = "Задания")
                Text(
                    text = "Готово $done/${tasks.size} · награда $available монет",
                    style = nunito(14, FontWeight.ExtraBold),
                    color = FinikColor.Text44,
                )
            }
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = {
                        when (task.target) {
                            TaskTarget.QUIZ -> if (quiz.isNotEmpty()) quizTaskId = task.id else unavailable = "Урок пока недоступен. Попробуй позже."
                            TaskTarget.SCENARIO -> if (scenarios.any { it.id == task.id }) scenarioId = task.id else unavailable = "Ситуация пока недоступна. Попробуй позже."
                            TaskTarget.PLAN -> onOpenPlan()
                            TaskTarget.GOAL -> onOpenGoal()
                            TaskTarget.SHOP -> onOpenShop()
                            null -> unavailable = if (task.goalCount > 0) {
                                "Прогресс: ${task.progress} из ${task.goalCount}. Выполни игровые действия из описания задания."
                            } else {
                                "Это задание выполняется во время игры. Следуй его описанию."
                            }
                        }
                    },
                )
            }
        }

        if (quizTaskId != null) {
            val task = tasks.firstOrNull { it.id == quizTaskId }
            if (task != null) {
            QuizSheet(
                questions = quiz,
                onClose = { quizTaskId = null },
                onComplete = { correct ->
                    onComplete(task.id, correct, task.reward)
                    quizTaskId = null
                },
            )
            }
        }
        scenarioId?.let { id ->
            val task = tasks.firstOrNull { it.id == id }
            val scenario = scenarios.firstOrNull { it.id == id }
            if (scenario != null && task != null) {
                ScenarioSheet(
                    scenario = scenario,
                    onClose = { scenarioId = null },
                    onComplete = { correct ->
                        onComplete(task.id, correct, task.reward)
                        scenarioId = null
                    },
                )
            }
        }
        unavailable?.let { message ->
            FinikConfirmSheet(
                title = "Задание недоступно",
                body = message,
                confirmText = "Понятно",
                onConfirm = { unavailable = null },
                onDismiss = { unavailable = null },
            )
        }
    }
}

@Composable
private fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(shape)
            .background(if (task.done) FinikColor.GreenSelected else FinikColor.Surface)
            .border(1.dp, if (task.done) FinikColor.GreenBorderDone else FinikColor.Border, shape)
            .clickable(enabled = !task.done, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "${task.title}. ${if (task.done) "Выполнено" else task.subtitle}. Награда ${task.reward} монет" }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (task.done) FinikColor.Green else task.kind.chipBackground,
                    RoundedCornerShape(9.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = when {
                    task.done -> FinikIcons.TaskDone
                    task.kind == TaskKind.LESSON -> FinikIcons.TaskLesson
                    task.kind == TaskKind.WEEK -> FinikIcons.TaskWeek
                    task.kind == TaskKind.HABIT -> FinikIcons.TaskHabit
                    else -> FinikIcons.TaskDay
                },
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = if (task.done) MaterialTheme.colorScheme.onPrimary else task.kind.chipInk,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = task.title, style = nunito(15, lineHeight = 1.25), color = FinikColor.Ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (task.done) "Готово" else task.subtitle,
                style = nunito(13, FontWeight.SemiBold),
                color = FinikColor.Text50,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!task.done && task.goalCount > 0) {
                Text(text = "${task.progress}/${task.goalCount}", style = nunito(13), color = FinikColor.Text46)
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(34.dp)
                    .background(FinikColor.CoinChip, RoundedCornerShape(11.dp))
                    .padding(start = 7.dp, end = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                CoinIcon(size = 15.dp, borderWidth = 2.dp)
                Text(text = task.reward.toString(), style = nunito(15, FontWeight.ExtraBold), color = FinikColor.CoinInk)
            }
            if (!task.done) {
                Icon(FinikIcons.Next, contentDescription = null, modifier = Modifier.size(22.dp), tint = FinikColor.Green)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun TasksScreenPreview() {
    FinikTheme { TasksScreen(onOpenPlan = {}, onOpenGoal = {}, onOpenShop = {}) }
}
