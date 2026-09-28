package dev.bober.finik.feature.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SquareIconButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.ScenarioTask

@Composable
internal fun ScenarioSheet(
    scenario: ScenarioTask,
    onClose: () -> Unit,
    onComplete: (correct: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var picked by rememberSaveable { mutableIntStateOf(-1) }
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
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Ситуация", style = nunito(12), color = FinikColor.Text50)
                SquareIconButton(onClick = onClose, size = 48.dp, background = FinikColor.Chip, contentDescription = "Закрыть ситуацию") {
                    Text(text = "×", style = nunito(18), color = FinikColor.Text44)
                }
            }
            Text(text = scenario.prompt, style = unbounded(18, lineHeight = 1.3), color = FinikColor.Ink)
            scenario.choices.forEachIndexed { index, choice ->
                val selected = picked == index
                val shape = RoundedCornerShape(14.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(
                            when {
                                selected && choice.correct -> FinikColor.GreenSelected
                                selected -> FinikColor.RedPickedBg
                                else -> FinikColor.Surface
                            },
                        )
                        .border(
                            2.dp,
                            when {
                                selected && choice.correct -> FinikColor.Green
                                selected -> FinikColor.RedPickedBorder
                                else -> FinikColor.Border
                            },
                            shape,
                        )
                        .clickable(enabled = picked < 0) { picked = index }
                        .semantics { this.selected = selected }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = choice.label, style = nunito(15, lineHeight = 1.3), color = FinikColor.Ink)
                }
            }
            if (picked >= 0) {
                val choice = scenario.choices[picked]
                Text(
                    text = choice.explanation,
                    style = nunito(13, FontWeight.SemiBold, lineHeight = 1.45),
                    color = if (choice.correct) FinikColor.GreenInkQuiz else FinikColor.Red,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                PrimaryButton(
                    text = if (choice.correct) "Завершить задание" else "Попробовать ещё",
                    onClick = {
                        if (choice.correct) onComplete(true) else picked = -1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp,
                    radius = 12.dp,
                )
            } else {
                Text(
                    text = "Выбери, что сделаешь — потом покажу, почему так.",
                    style = nunito(13, FontWeight.SemiBold, lineHeight = 1.45),
                    color = FinikColor.GreenInkQuiz,
                )
            }
        }
    }
    }
}
