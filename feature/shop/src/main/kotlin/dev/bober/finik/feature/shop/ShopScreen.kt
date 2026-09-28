package dev.bober.finik.feature.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.FinikConfirmSheet
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.ScreenTitle
import dev.bober.finik.core.designsystem.component.TagChip
import dev.bober.finik.core.designsystem.component.glyphColor
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.PetProfile
import dev.bober.finik.core.model.PetAppearance
import androidx.compose.material3.FilterChip
import dev.bober.finik.core.model.BuyCheck
import dev.bober.finik.core.model.ItemGlyph
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.WeekPlan
import androidx.compose.foundation.background as bg

@Composable
internal fun ShopScreen(
    modifier: Modifier = Modifier,
    pet: PetProfile = SampleData.pet,
    onCustomize: (PetAppearance) -> Unit = {},
    items: List<ShopItem> = SampleData.shop,
    plan: WeekPlan = SampleData.plan,
    planConfirmed: Boolean = true,
    onCheck: (String) -> BuyCheck? = { null },
    onBuy: (String) -> Unit = {},
) {
    var appearanceTab by rememberSaveable { mutableStateOf(false) }
    var petInteractionActive by remember { mutableStateOf(false) }
    var pendingId by rememberSaveable { mutableStateOf<String?>(null) }
    var blocked by rememberSaveable { mutableStateOf<String?>(null) }
    val pending = items.firstOrNull { it.id == pendingId }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .bg(FinikColor.Background)
                .verticalScroll(rememberScrollState(), enabled = !petInteractionActive)
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                ScreenTitle(text = "Лавка")
                Text(
                    text = when {
                        appearanceTab -> "Цвета и аксессуары"
                        planConfirmed -> "Списывается из статьи плана"
                        else -> "Сначала подтверди план"
                    },
                    style = nunito(12),
                    color = FinikColor.Text48,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = 160.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !appearanceTab, onClick = { appearanceTab = false }, label = { Text("Уход и игры") })
                FilterChip(selected = appearanceTab, onClick = { appearanceTab = true }, label = { Text("Внешность") })
            }
            if (appearanceTab) {
                PetCustomization(
                    pet = pet,
                    onSave = onCustomize,
                    onPetInteractionChange = { petInteractionActive = it },
                )
            } else BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth < 380.dp) 1 else 2
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items.chunked(columns).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            pair.forEach { item ->
                                ShopCard(
                                    item = item,
                                    left = plan.entry(item.category).left,
                                    planConfirmed = planConfirmed,
                                    onClick = {
                                        val check = onCheck(item.id)
                                        if (check == null || check.allowed) pendingId = item.id
                                        else blocked = check.message
                                    },
                                )
                            }
                            if (pair.size < columns) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        pending?.let { item ->
            FinikConfirmSheet(
                title = "Купить «${item.name}»?",
                body = "${item.cost} монет из статьи «${item.category.label}» (${item.kind.label}). ${item.effect}",
                confirmText = "Купить",
                onConfirm = {
                    onBuy(item.id)
                    pendingId = null
                },
                onDismiss = { pendingId = null },
            )
        }
        blocked?.let { message ->
            FinikConfirmSheet(
                title = "Пока не хватает",
                body = message,
                confirmText = "Понятно",
                cancelText = "Закрыть",
                warning = true,
                onConfirm = { blocked = null },
                onDismiss = { blocked = null },
            )
        }
    }
}

@Composable
private fun RowScope.ShopCard(item: ShopItem, left: Int, planConfirmed: Boolean, onClick: () -> Unit) {
    val canBuy = planConfirmed && left >= item.cost
    FinikCard(
        modifier = Modifier.weight(1f),
        radius = 16.dp,
        borderColor = if (item.isSale) FinikColor.RedBorderSale else FinikColor.Border,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        gap = 8.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(38.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .bg(item.glyphColor.copy(alpha = 0.17f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = item.glyphColor,
                    )
                }
            }
            TagChip(
                text = if (item.isSale) "−${item.salePercent}%" else item.kind.label,
                background = if (item.isSale) FinikColor.RedSaleBg else FinikColor.Chip,
                ink = if (item.isSale) FinikColor.RedSaleInk else FinikColor.Text50,
                weight = FontWeight.ExtraBold,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = item.name, style = nunito(14.5, lineHeight = 1.2), color = FinikColor.Ink)
            Text(
                text = "${item.category.label} · осталось $left",
                style = nunito(13, FontWeight.SemiBold),
                color = FinikColor.Text50,
            )
        }
        Row(
            modifier = Modifier.heightIn(min = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = item.cost.toString(), style = nunito(17, FontWeight.ExtraBold), color = FinikColor.CoinInk36)
            item.oldCost?.let { old ->
                Text(
                    text = old.toString(),
                    style = nunito(12.5).copy(textDecoration = TextDecoration.LineThrough),
                    color = FinikColor.Text60,
                )
            }
        }
        PrimaryButton(
            text = when {
                !planConfirmed -> "Нужен план"
                canBuy -> "Купить"
                else -> "Мало монет"
            },
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            height = 48.dp,
            radius = 12.dp,
            textStyle = nunito(13.5),
            containerColor = if (canBuy) FinikColor.Green else FinikColor.DisabledButton,
            contentColor = if (canBuy) MaterialTheme.colorScheme.onPrimary else FinikColor.Text56,
        )
    }
}

/** Names identify known goods; category and glyph keep unknown catalog items recognizable. */
private fun ShopItem.icon(): ImageVector {
    val nameOrId = "$name $id".lowercase()
    return when {
        nameOrId.contains("бутыл") || nameOrId.contains("bottle") || nameOrId.contains("фляг") -> FinikIcons.ItemBottle
        nameOrId.contains("витамин") || nameOrId.contains("vitamin") || nameOrId.contains("лекар") -> FinikIcons.ItemVitamins
        nameOrId.contains("мяч") || nameOrId.contains("ball") -> FinikIcons.ItemBall
        nameOrId.contains("лежанк") || nameOrId.contains("кроват") || nameOrId.contains("bed") -> FinikIcons.ItemBed
        nameOrId.contains("бантик") || nameOrId.contains("бант") || nameOrId.contains("bow") -> FinikIcons.ItemBow
        nameOrId.contains("качел") || nameOrId.contains("swing") -> FinikIcons.ItemSwing
        nameOrId.contains("вод") || nameOrId.contains("water") || nameOrId.contains("пить") -> FinikIcons.ItemWater
        nameOrId.contains("корм") || nameOrId.contains("еда") || nameOrId.contains("food") || nameOrId.contains("feed") -> FinikIcons.ItemFood
        category == SpendCategory.WATER && glyph == ItemGlyph.BUCKET -> FinikIcons.ItemBottle
        category == SpendCategory.WATER -> FinikIcons.ItemWater
        category == SpendCategory.FOOD && glyph == ItemGlyph.ROUNDED -> FinikIcons.ItemVitamins
        category == SpendCategory.FOOD -> FinikIcons.ItemFood
        category == SpendCategory.PLAY && glyph == ItemGlyph.TALL_POT -> FinikIcons.ItemBed
        category == SpendCategory.PLAY && glyph == ItemGlyph.ROUNDED -> FinikIcons.ItemSwing
        category == SpendCategory.PLAY -> FinikIcons.ItemBall
        else -> FinikIcons.Goal
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ShopScreenPreview() {
    FinikTheme { ShopScreen() }
}
