package dev.bober.finik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.OutlineButton
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.pet.PetAnimation
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Приветствие: питомец и три понятных направления для первых действий. */
@Composable
internal fun WelcomeScreen(
    onStart: () -> Unit,
    onDemo: (Boolean) -> Unit = {},
    actionsEnabled: Boolean = true,
    canChangeServer: Boolean = false,
    onOpenServer: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var demoChoice by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {
        val compact = maxHeight < 680.dp
        var petInteractionActive by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState(), enabled = !petInteractionActive)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = if (compact) 20.dp else 36.dp,
                    bottom = if (compact) 20.dp else 40.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(
                if (compact) 18.dp else 30.dp,
                Alignment.CenterVertically,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
            ) {
                PetFigure(
                    species = PetSpecies.OWL,
                    spec = PetFigureSpec.Welcome.copy(
                        width = if (compact) 170.dp else 190.dp,
                        height = if (compact) 180.dp else 210.dp,
                        live3d = true,
                    ),
                    action = PetAnimation.GREET,
                    onInteractionChange = { petInteractionActive = it },
                )
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
                        text = "Заботься о сове и учись планировать.",
                        style = nunito(16, FontWeight.SemiBold, lineHeight = 1.5),
                        color = FinikColor.Text43,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatTile(label = "Нужное", icon = FinikIcons.Food, valueColor = FinikColor.GreenStat)
                    StatTile(label = "Желаемое", icon = FinikIcons.Play, valueColor = FinikColor.RedStat)
                    StatTile(label = "Копилка", icon = FinikIcons.Goal, valueColor = FinikColor.CoinInk40)
                }
                PrimaryButton(
                    text = "Создать сову",
                    onClick = onStart,
                    enabled = actionsEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    icon = FinikIcons.Pet,
                )
                OutlineButton(text = "Демо для экспертов", onClick = { demoChoice = true }, enabled = actionsEnabled, modifier = Modifier.fillMaxWidth())
                Text(text = "Для игры нужно подключение к интернету.", style = nunito(12), color = FinikColor.Text43)
                if (canChangeServer) OutlineButton(text = "Адрес сервера", onClick = onOpenServer, modifier = Modifier.fillMaxWidth())
            }
        }
    }
    if (demoChoice) AlertDialog(
        onDismissRequest = { demoChoice = false },
        title = { Text("Как начать демо?") },
        text = { Text("Отдельный профиль позволяет ускорять дни и недели. Обычная игра не изменится.") },
        confirmButton = { TextButton(onClick = { demoChoice = false; onDemo(true) }) { Text("Готовый профиль") } },
        dismissButton = { TextButton(onClick = { demoChoice = false; onDemo(false) }) { Text("С нуля") } },
    )
}

@Composable
private fun RowScope.StatTile(label: String, icon: ImageVector, valueColor: Color) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(FinikColor.WhiteGlass, RoundedCornerShape(14.dp))
            .border(1.dp, FinikColor.BorderStrong, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(24.dp))
        Text(text = label, style = nunito(12, FontWeight.ExtraBold), color = FinikColor.Ink)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 828)
@Composable
private fun WelcomePreview() {
    FinikTheme { WelcomeScreen(onStart = {}) }
}
