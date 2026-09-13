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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.CoinIcon
import dev.bober.finik.core.designsystem.component.ScreenTitle
import dev.bober.finik.core.designsystem.component.TagChip
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.chipBackground
import dev.bober.finik.core.designsystem.theme.chipInk
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.SampleData
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
) {
    var quizOpen by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                ScreenTitle(text = "Задания")
                Text(
                    text = "$done/${tasks.size} · $available монет доступно",
                    style = nunito(13, FontWeight.ExtraBold),
                    color = FinikColor.Text44,
                )
            }
            tasks.forEach { task ->
                TaskCard(
                    task = task,
                    onClick = {
                        when (task.target) {
                            TaskTarget.QUIZ -> quizOpen = true
                            TaskTarget.PLAN -> onOpenPlan()
                            TaskTarget.GOAL -> onOpenGoal()
                            TaskTarget.SHOP -> onOpenShop()
                            null -> Unit // TODO(logic): выполнить задание на месте
                        }
                    },
                )
            }
        }

        if (quizOpen) {
            QuizSheet(
                questions = SampleData.quiz,
                onClose = { quizOpen = false },
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
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (task.done) FinikColor.Green else FinikColor.Chip,
                    if (task.kind == TaskKind.WEEK) RoundedCornerShape(9.dp) else CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (task.done) {
                Box(modifier = Modifier.size(12.dp).background(FinikColor.Surface, RoundedCornerShape(3.dp)))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = task.title, style = nunito(15, lineHeight = 1.25), color = FinikColor.Ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TagChip(
                    text = task.kind.label,
                    background = task.kind.chipBackground,
                    ink = task.kind.chipInk,
                )
                Text(
                    text = if (task.done) "выполнено" else task.subtitle,
                    style = nunito(11.5, FontWeight.SemiBold),
                    color = FinikColor.Text50,
                )
            }
        }
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
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun TasksScreenPreview() {
    FinikTheme { TasksScreen(onOpenPlan = {}, onOpenGoal = {}, onOpenShop = {}) }
}
