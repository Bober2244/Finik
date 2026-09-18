package dev.bober.finik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Приветствие: питомец, слоган, три цифры экономики и кнопка «Создать питомца». */
@Composable
internal fun WelcomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .radialGlow()
            .systemBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PetFigure(species = PetSpecies.FINIK, spec = PetFigureSpec.Welcome)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Финик",
                    style = unbounded(34, lineHeight = 1.05, letterSpacing = (-0.02).em),
                    color = FinikColor.Ink,
                )
                Text(
                    text = "Сначала реши: потратить на нужное, на желаемое или отложить. Потом увидишь, как это меняет питомца.",
                    style = nunito(16, FontWeight.SemiBold, lineHeight = 1.5),
                    color = FinikColor.Text43,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 290.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatTile(value = "нужное", label = "еда и вода", valueColor = FinikColor.GreenStat)
                StatTile(value = "желаемое", label = "игры, можно ждать", valueColor = FinikColor.RedStat)
                StatTile(value = "копилка", label = "на мечту", valueColor = FinikColor.CoinInk40)
            }
            PrimaryButton(
                text = "Создать питомца",
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RowScope.StatTile(value: String, label: String, valueColor: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(FinikColor.WhiteGlass, RoundedCornerShape(14.dp))
            .border(1.dp, FinikColor.BorderStrong, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = value, style = nunito(14, FontWeight.ExtraBold, lineHeight = 1.15), color = valueColor)
        Text(text = label, style = nunito(12), color = FinikColor.Text48)
    }
}

/** `radial-gradient(120% 60% at 50% 6%, зелёное свечение 0%, фон 68%)`. */
private fun Modifier.radialGlow(): Modifier = drawBehind {
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to FinikColor.GreenGlow, 0.68f to FinikColor.Background),
            center = Offset(size.width / 2f, size.height * 0.06f),
            radius = size.height * 0.6f,
        ),
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 828)
@Composable
private fun WelcomePreview() {
    FinikTheme { WelcomeScreen(onStart = {}) }
}
