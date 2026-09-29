package dev.bober.finik.feature.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.PetAccessory
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.pet.PetAnimation
import dev.bober.finik.core.pet.PetFigure
import dev.bober.finik.core.pet.PetFigureSpec

/** Preview is local until Save; the repository persists the entire appearance atomically. */
@Composable
internal fun PetCustomization(
    pet: PetProfile,
    onSave: (PetAppearance) -> Unit,
    onPetInteractionChange: (Boolean) -> Unit,
    actionsEnabled: Boolean = true,
) {
    var draft by remember(pet.appearance, pet.species) { mutableStateOf(pet.appearance) }
    var action by remember { mutableStateOf(PetAnimation.IDLE) }
    var actionEventId by remember { mutableLongStateOf(0L) }
    FinikCard(gap = 14.dp) {
        Text("Стиль: ${pet.name}", style = nunito(18), color = FinikColor.Ink)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            PetFigure(
                species = pet.species,
                spec = PetFigureSpec.hero(pet.stageIndex).copy(width = 240.dp, height = 260.dp),
                mood = pet.mood,
                appearance = draft,
                action = action,
                actionEventId = actionEventId,
                onInteractionChange = onPetInteractionChange,
            )
        }
        Text("Проведите пальцем по сове, чтобы повернуть её. Масштаб меняется двумя пальцами.", style = nunito(12), color = FinikColor.Text50)
        Text("Выберите аксессуары", style = nunito(14), color = FinikColor.Ink)
        Text("Можно надеть несколько. Нажмите ещё раз, чтобы снять.", style = nunito(12), color = FinikColor.Text50)
        AccessoryRow(
            values = PetAccessory.entries,
            selected = draft.accessories,
            onSelect = { accessory ->
                draft = draft.copy(
                    accessories = when {
                        accessory == PetAccessory.NONE -> emptySet()
                        accessory in draft.accessories -> draft.accessories - accessory
                        else -> draft.accessories + accessory
                    },
                )
            },
        )
        Text("Цвет перьев", style = nunito(14), color = FinikColor.Ink)
        ChoiceRow(
            values = PetFurColor.entries,
            selected = draft.furColor,
            label = { it.title },
            swatch = { Color(it.swatchArgb) },
            onSelect = { draft = draft.copy(furColor = it) },
        )
        Text("Внешность сохраняется в профиле совы. Для сохранения нужно подключение.", style = nunito(12), color = FinikColor.Text50)
        PrimaryButton(
            text = if (draft == pet.appearance) "Внешность сохранена" else "Сохранить внешность",
            enabled = actionsEnabled && draft != pet.appearance,
            onClick = { onSave(draft) },
            modifier = Modifier.fillMaxWidth(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Посмотреть в движении", style = nunito(14), color = FinikColor.Ink)
            ChoiceRow(
                values = listOf(
                    PetAnimation.IDLE, PetAnimation.CALM, PetAnimation.WALK, PetAnimation.RUN,
                    PetAnimation.DRINK, PetAnimation.GREET, PetAnimation.HELP, PetAnimation.EAT,
                    PetAnimation.DANCE,
                ),
                selected = action,
                label = {
                    when (it) {
                        PetAnimation.IDLE -> "Спокойно стоит"
                        PetAnimation.CALM -> "Отдыхает"
                        PetAnimation.WALK -> "Идёт"
                        PetAnimation.RUN -> "Бежит"
                        PetAnimation.DRINK -> "Пьёт"
                        PetAnimation.GREET -> "Приветствует"
                        PetAnimation.HELP -> "Зовёт на помощь"
                        PetAnimation.EAT -> "Ест"
                        PetAnimation.DANCE -> "Танцует"
                    }
                },
                onSelect = { action = it; actionEventId++ },
            )
        }
    }
}

@Composable
private fun AccessoryRow(
    values: List<PetAccessory>,
    selected: Set<PetAccessory>,
    onSelect: (PetAccessory) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEach { accessory ->
            val active = if (accessory == PetAccessory.NONE) selected.isEmpty() else accessory in selected
            val shape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier.width(152.dp).heightIn(min = 48.dp).clip(shape)
                    .background(if (active) FinikColor.GreenSelected else FinikColor.Surface)
                    .border(1.dp, if (active) FinikColor.Green else FinikColor.Border, shape)
                    .clickable { onSelect(accessory) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (active) "✓" else "+", style = nunito(13), color = FinikColor.GreenInk40)
                Text(accessory.title, style = nunito(12.5), color = FinikColor.Ink)
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    swatch: ((T) -> Color)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEach { value ->
            val active = value == selected
            val shape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier.width(152.dp).heightIn(min = 48.dp).clip(shape)
                    .background(if (active) FinikColor.GreenSelected else FinikColor.Surface)
                    .border(1.dp, if (active) FinikColor.Green else FinikColor.Border, shape)
                    .clickable { onSelect(value) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                swatch?.let { Box(Modifier.size(20.dp).background(it(value), CircleShape)) }
                Text(label(value), style = nunito(12.5), color = FinikColor.Ink)
            }
        }
    }
}
