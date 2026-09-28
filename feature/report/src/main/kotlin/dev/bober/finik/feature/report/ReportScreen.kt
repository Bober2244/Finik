package dev.bober.finik.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.SquareIconButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.color
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.ReportRow
import dev.bober.finik.core.model.SampleData

/**
 * Отчёт «Неделя N закрыта»: план против факта по статьям, последствия
 * и выбор — новый план или повторить прошлый.
 */
@Composable
internal fun ReportScreen(
    onBack: () -> Unit,
    onNewPlan: () -> Unit,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    week: Int = SampleData.REPORT_WEEK,
    rows: List<ReportRow> = SampleData.reportRows,
    summary: String = SampleData.REPORT_SUMMARY,
    note: String = SampleData.REPORT_NOTE,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SquareIconButton(onClick = onBack, size = 42.dp) {
            Text(text = "←", style = nunito(18), color = FinikColor.Ink)
        }

        FinikCard(radius = 20.dp, contentPadding = PaddingValues(16.dp), gap = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = "Неделя $week закрыта", style = unbounded(19, lineHeight = 1.15), color = FinikColor.Ink)
                Text(text = summary, style = nunito(12.5), color = FinikColor.Text50)
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { row -> ReportBar(row = row) }
            }

            Text(
                text = note,
                style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45),
                color = FinikColor.RedNoteInk,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FinikColor.RedNoteBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 13.dp, vertical = 12.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PrimaryButton(
                    text = "Новый план",
                    onClick = onNewPlan,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                    radius = 12.dp,
                    textStyle = nunito(14),
                )
                OutlineButton(
                    text = "Повторить",
                    onClick = onRepeat,
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                    radius = 12.dp,
                )
            }
        }
    }
}

@Composable
internal fun EmptyReportScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(FinikColor.Background).systemBarsPadding()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SquareIconButton(onClick = onBack, size = 48.dp) {
            Text(text = "Назад", style = nunito(14), color = FinikColor.Ink)
        }
        FinikCard {
            Text(text = "Отчёта пока нет", style = unbounded(19), color = FinikColor.Ink)
            Text(text = "Подтверди план и закрой первый период — здесь появятся настоящие план и факт.", style = nunito(14), color = FinikColor.Text46)
        }
    }
}

/** Полоска «план против факта»: перерасход — бледная заливка на всю ширину и красный маркер справа. */
@Composable
private fun ReportBar(row: ReportRow) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = row.category.label, style = nunito(12.5), color = FinikColor.Ink)
            Text(text = "${row.planned} план · ${row.actual} факт", style = nunito(12.5), color = FinikColor.Text50)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(FinikColor.IconButton),
        ) {
            if (row.isOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(row.category.color.copy(alpha = 0.35f)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(3.dp)
                        .background(FinikColor.RedMarker),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(row.progress)
                        .background(row.category.color),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 700)
@Composable
private fun ReportScreenPreview() {
    FinikTheme { ReportScreen(onBack = {}, onNewPlan = {}, onRepeat = {}) }
}
