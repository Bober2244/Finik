package dev.bober.finik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.StepLabel
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.pet.PetAnimation
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Шаг 2 из 2: имя питомца и недельный доход (20 / 40 / 60 монет). */
@Composable
internal fun NamePetScreen(
    onFinish: (name: String, income: Int) -> Unit,
    modifier: Modifier = Modifier,
    species: PetSpecies = PetSpecies.OWL,
    appearance: PetAppearance = PetAppearance(),
    actionsEnabled: Boolean = true,
) {
    var name by rememberSaveable { mutableStateOfString() }
    var income by rememberSaveable { mutableStateOfInt(SampleData.WEEKLY_INCOME) }
    var petInteractionActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState(), enabled = !petInteractionActive)
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            StepLabel(text = "Шаг 2 из 2")
            Text(text = "Назови сову", style = unbounded(23, lineHeight = 1.15), color = FinikColor.Ink)
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PetFigure(
                species = species,
                spec = PetFigureSpec.Naming,
                appearance = appearance,
                action = PetAnimation.GREET,
                onInteractionChange = { petInteractionActive = it },
            )
        }

        NameField(value = name, onValueChange = { name = it })

        FinikCard(radius = 16.dp, gap = 10.dp) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(FinikIcons.Goal, contentDescription = null, tint = FinikColor.GreenInk40, modifier = Modifier.size(20.dp))
                Text(text = "Монет на неделю", style = nunito(14), color = FinikColor.Ink)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SampleData.incomeOptions.forEach { (value, label) ->
                    IncomeOption(
                        value = value,
                        label = label,
                        selected = income == value,
                        onClick = { income = value },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = "Начать неделю",
            onClick = { onFinish(name.ifBlank { species.title }, income) },
            modifier = Modifier.fillMaxWidth(),
            enabled = actionsEnabled && name.isNotBlank(),
            icon = FinikIcons.Confirm,
        )
    }
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interaction,
        textStyle = nunito(17, color = FinikColor.Ink),
        cursorBrush = SolidColor(FinikColor.Green),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(FinikColor.Surface, shape)
            .border(2.dp, if (focused) FinikColor.Green else FinikColor.InputBorder, shape)
            .semantics { contentDescription = "Имя питомца" },
        decorationBox = { inner ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(FinikIcons.Pet, contentDescription = null, tint = FinikColor.GreenInk40, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(text = "Имя питомца", style = nunito(17), color = FinikColor.Text58)
                    }
                    inner()
                }
            }
        },
    )
}

@Composable
private fun RowScope.IncomeOption(
    value: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(if (selected) FinikColor.GreenSelected else FinikColor.Surface)
            .border(2.dp, if (selected) FinikColor.Green else FinikColor.Border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
    ) {
        Text(text = value.toString(), style = unbounded(19, lineHeight = 1), color = FinikColor.TextWarm34)
        Text(text = label, style = nunito(11.5), color = FinikColor.Text50)
    }
}

private fun mutableStateOfString() = androidx.compose.runtime.mutableStateOf("")
private fun mutableStateOfInt(value: Int) = androidx.compose.runtime.mutableIntStateOf(value)

@Preview(showBackground = true, widthDp = 412, heightDp = 828)
@Composable
private fun NamePetPreview() {
    FinikTheme { NamePetScreen(onFinish = { _, _ -> }) }
}
