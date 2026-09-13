package dev.bober.finik.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import dev.bober.finik.core.designsystem.component.StepLabel
import dev.bober.finik.core.designsystem.component.TagChip
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.designsystem.theme.unbounded
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Шаг 1 из 2: список из трёх ростков с чертой и бонусом. */
@Composable
internal fun PickPetScreen(
    onPick: (PetSpecies) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Text(text = "Выбери росток", style = unbounded(23, lineHeight = 1.15), color = FinikColor.Ink)
        }

        PetSpecies.entries.forEach { species ->
            PetOptionCard(
                species = species,
                selected = species == SampleData.pet.species,
                onClick = { onPick(species) },
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Черта влияет на расходы: одному чаще нужна вода, другому — игры.",
            style = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45),
            color = FinikColor.Text52,
        )
    }
}

@Composable
private fun PetOptionCard(
    species: PetSpecies,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(shape)
            .background(if (selected) FinikColor.GreenSelected else FinikColor.Surface)
            .border(2.dp, if (selected) FinikColor.Green else FinikColor.Border, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PetFigure(species = species, spec = PetFigureSpec.Card, animate = false)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
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
}

@Preview(showBackground = true, widthDp = 412, heightDp = 828)
@Composable
private fun PickPetPreview() {
    FinikTheme { PickPetScreen(onPick = {}) }
}
