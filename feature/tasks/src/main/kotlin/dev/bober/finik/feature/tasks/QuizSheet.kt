package dev.bober.finik.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SquareIconButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.QuizQuestion
import dev.bober.finik.core.model.SampleData

/**
 * Мини-урок: затемнение и шторка снизу с вопросом, тремя вариантами (А/Б/В) и разбором.
 * Неправильный ответ объясняется и позволяет повторить вопрос до завершения урока.
 */
@Composable
internal fun QuizSheet(
    questions: List<QuizQuestion>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onComplete: (correct: Boolean) -> Unit = {},
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var picked by rememberSaveable { mutableIntStateOf(-1) }
    if (questions.isEmpty()) return
    val question = questions[step.coerceIn(0, questions.lastIndex)]

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            )
            .navigationBarsPadding()
            .padding(14.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(FinikColor.Surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Мини-урок · вопрос ${step + 1} из ${questions.size}",
                    style = nunito(12),
                    color = FinikColor.Text50,
                )
                SquareIconButton(onClick = onClose, size = 48.dp, background = FinikColor.Chip, contentDescription = "Закрыть урок") {
                    Text(text = "×", style = nunito(18), color = FinikColor.Text44)
                }
            }
            Text(text = question.question, style = unbounded(19, lineHeight = 1.3), color = FinikColor.Ink)

            question.options.forEachIndexed { index, label ->
                val isPicked = picked == index
                val isRight = picked >= 0 && index == question.rightIndex
                QuizOption(
                    key = listOf("А", "Б", "В", "Г", "Д").getOrElse(index) { (index + 1).toString() },
                    label = label,
                    dot = when {
                        isRight -> FinikColor.Green
                        isPicked -> FinikColor.RedPicked
                        else -> FinikColor.QuizKey
                    },
                    border = when {
                        isRight -> FinikColor.Green
                        isPicked -> FinikColor.RedPickedBorder
                        else -> FinikColor.Border
                    },
                    background = when {
                        isRight -> FinikColor.GreenSelected
                        isPicked -> FinikColor.RedPickedBg
                        else -> FinikColor.Surface
                    },
                    selected = isPicked,
                    onClick = {
                        if (picked < 0) picked = index
                    },
                )
            }

            val answered = picked >= 0
            val correct = answered && picked == question.rightIndex
            Text(
                text = when {
                    !answered -> "Выбери ответ — покажу разбор."
                    correct -> "Верно. ${question.explanation}"
                    else -> "Не так. ${question.explanation}"
                },
                style = nunito(13, FontWeight.SemiBold, lineHeight = 1.45),
                color = if (answered && !correct) FinikColor.Red else FinikColor.GreenInkQuiz,
                modifier = Modifier.heightIn(min = 38.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (answered) {
                PrimaryButton(
                    text = when {
                        !correct -> "Попробовать ещё"
                        step < questions.lastIndex -> "Следующий вопрос"
                        else -> "Завершить урок"
                    },
                    onClick = {
                        when {
                            !correct -> picked = -1
                            step < questions.lastIndex -> { step += 1; picked = -1 }
                            else -> { onComplete(true); onClose() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp,
                    radius = 12.dp,
                )
            }
        }
    }
    }
}

@Composable
private fun QuizOption(
    key: String,
    label: String,
    dot: Color,
    border: Color,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(shape)
            .background(background)
            .border(2.dp, border, shape)
            .clickable(onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(26.dp).background(dot, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = key,
                style = nunito(12, FontWeight.ExtraBold),
                color = if (dot.luminance() > 0.179f) Color.Black else Color.White,
            )
        }
        Text(text = label, style = nunito(15, lineHeight = 1.3), color = FinikColor.Ink)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 700)
@Composable
private fun QuizSheetPreview() {
    FinikTheme { QuizSheet(questions = SampleData.quiz, onClose = {}) }
}
