package dev.bober.finik.feature.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.BackHeader
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikProgressBar
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.color
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.GrowthStage
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.WeekLogEntry
import dev.bober.finik.core.model.growthStages

/** «Взросление питомца»: текущая стадия с опытом, список стадий и итоги недели. */
@Composable
internal fun GrowthScreen(
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
    modifier: Modifier = Modifier,
    pet: PetProfile = SampleData.pet,
    stages: List<GrowthStage> = growthStages,
    weekLog: List<WeekLogEntry> = SampleData.weekLog,
    hasReport: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackHeader(title = "Взросление питомца", onBack = onBack)

        FinikCard(background = FinikColor.GreenCard, borderColor = null, gap = 9.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = pet.stage.name, style = nunito(16), color = FinikColor.Ink)
                Text(
                    text = if (pet.stageIndex == stages.lastIndex) "Все стадии открыты" else "до следующей стадии ${(100 - pet.xp).coerceAtLeast(0)} опыта",
                    style = nunito(12.5),
                    color = FinikColor.GreenInk42,
                )
            }
            FinikProgressBar(progress = if (pet.stageIndex == stages.lastIndex) 1f else pet.xp / 100f, color = FinikColor.Green, height = 14.dp, track = FinikColor.WhiteGlassSoft)
            Text(
                text = "Опыт даёт не трата, а выполненный план: отложил вовремя — растёт быстрее.",
                style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.GreenInk40s,
            )
        }

        stages.forEachIndexed { index, stage ->
            StageRow(stage = stage, index = index, currentIndex = pet.stageIndex)
        }

        FinikCard(
            modifier = if (hasReport) Modifier.clickable(onClick = onOpenReport) else Modifier,
            gap = 9.dp,
        ) {
            Text(text = "Итоги недели", style = nunito(14.5), color = FinikColor.Ink)
            if (!hasReport) Text(text = "Появятся после закрытия первого периода.", style = nunito(13), color = FinikColor.Text50)
            weekLog.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(entry.tone.color, if (entry.rounded) CircleShape else RoundedCornerShape(3.dp)),
                    )
                    Text(
                        text = entry.text,
                        style = nunito(13, FontWeight.SemiBold),
                        color = FinikColor.Text40,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = entry.delta, style = nunito(12.5), color = entry.tone.color)
                }
            }
        }
    }
}

@Composable
private fun StageRow(stage: GrowthStage, index: Int, currentIndex: Int) {
    val open = index <= currentIndex
    val current = index == currentIndex
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (current) FinikColor.GreenSelected else FinikColor.Surface)
            .border(1.dp, if (current) FinikColor.GreenBorderActive else FinikColor.BorderSoft, shape)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.BottomCenter) {
            Icon(
                imageVector = when (index) {
                    0 -> FinikIcons.StageBaby
                    1 -> FinikIcons.StageExplorer
                    2 -> FinikIcons.StageTeen
                    3 -> FinikIcons.StageAdult
                    else -> FinikIcons.StageWise
                },
                contentDescription = null,
                modifier = Modifier.size((26 + index.coerceAtMost(4) * 3).dp),
                tint = if (open) FinikColor.GreenStageOpen else FinikColor.Text58,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = stage.name, style = nunito(15), color = FinikColor.Ink)
            Text(
                text = if (open) stage.note else "закрыто",
                style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.35),
                color = FinikColor.Text50,
            )
        }
        Text(
            text = if (open) "открыто" else "${stage.requiredXp} опыта",
            style = nunito(12),
            color = FinikColor.Text52,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun GrowthScreenPreview() {
    FinikTheme { GrowthScreen(onBack = {}, onOpenReport = {}) }
}
