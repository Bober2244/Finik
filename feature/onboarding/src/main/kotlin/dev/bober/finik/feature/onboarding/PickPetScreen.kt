package dev.bober.finik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.StepLabel
import dev.bober.finik.core.designsystem.component.TagChip
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.pet.PetAnimation
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Шаг 1 из 2: знакомство с совой и выбор её внешности. */
@Composable
internal fun PickPetScreen(
    onPick: (PetSpecies, PetAppearance) -> Unit,
    modifier: Modifier = Modifier,
) {
    var fur by rememberSaveable { mutableStateOf(PetFurColor.BLUE.name) }
    var accessoryIds by rememberSaveable { mutableStateOf("") }
    val appearance = PetAppearance(
        furColor = PetFurColor.fromStored(fur),
        accessories = accessoryIds.toSelectedAccessories(),
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinikColor.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            StepLabel(text = "Шаг 1 из 2")
            Text(text = "Познакомься с совой", style = unbounded(23, lineHeight = 1.15), color = FinikColor.Ink)
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PetFigure(
                species = PetSpecies.OWL,
                spec = PetFigureSpec.hero(0).copy(width = 210.dp, height = 230.dp),
                appearance = appearance,
                action = PetAnimation.GREET,
            )
        }
        PetOptionCard()
        Text(text = "Цвет перьев", style = nunito(14), color = FinikColor.Ink)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PetFurColor.entries.forEach { color ->
                Column(
                    modifier = Modifier.width(112.dp)
                        .heightIn(min = 76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (color == appearance.furColor) FinikColor.GreenSelected else FinikColor.Surface)
                        .border(2.dp, if (color == appearance.furColor) FinikColor.Green else FinikColor.Border, RoundedCornerShape(12.dp))
                        .clickable { fur = color.name }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Box(Modifier.size(22.dp).background(Color(color.swatchArgb), CircleShape))
                    Text(text = color.title, style = nunito(11), color = FinikColor.Ink)
                }
            }
        }
        Text(text = "Аксессуары", style = nunito(14), color = FinikColor.Ink)
        Text(text = "Можно выбрать несколько. Изменить их можно будет позже в лавке.", style = nunito(12), color = FinikColor.Text50)
        AccessoryOptions(
            selected = appearance.accessories,
            onSelect = { accessory ->
                accessoryIds = when {
                    accessory == PetAccessory.NONE -> emptySet()
                    accessory in appearance.accessories -> appearance.accessories - accessory
                    else -> appearance.accessories + accessory
                }.toStoredAccessoryIds()
            },
        )
        PrimaryButton(
            text = "Дальше",
            onClick = { onPick(PetSpecies.OWL, appearance) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Забота помогает сове расти. Корми её, давай воду, играй и выполняй план недели.",
            style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45),
            color = FinikColor.Text52,
        )
    }
}

@Composable
private fun AccessoryOptions(
    selected: Set<PetAccessory>,
    onSelect: (PetAccessory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PetAccessory.entries.forEach { accessory ->
            val active = if (accessory == PetAccessory.NONE) selected.isEmpty() else accessory in selected
            val shape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier.width(158.dp)
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(if (active) FinikColor.GreenSelected else FinikColor.Surface)
                    .border(2.dp, if (active) FinikColor.Green else FinikColor.Border, shape)
                    .clickable { onSelect(accessory) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (active) "✓" else "+", style = nunito(13), color = FinikColor.GreenInk40)
                Text(accessory.title, style = nunito(12), color = FinikColor.Ink)
            }
        }
    }
}

@Composable
private fun PetOptionCard() {
    val species = PetSpecies.OWL
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(shape)
            .background(FinikColor.GreenSelected)
            .border(2.dp, FinikColor.Green, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = species.title, style = nunito(17), color = FinikColor.Ink)
        Text(
            text = species.description,
            style = nunito(13, FontWeight.SemiBold, lineHeight = 1.35),
            color = FinikColor.Text47,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TagChip(text = species.trait, background = FinikColor.ChipTrait, ink = FinikColor.TextWarm42)
            TagChip(text = species.bonus, background = FinikColor.GreenChipBonus, ink = FinikColor.GreenInk40)
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 828)
@Composable
private fun PickPetPreview() {
    FinikTheme { PickPetScreen(onPick = { _, _ -> }) }
}
