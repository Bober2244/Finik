package dev.bober.finik.feature.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.FinikCard
import dev.bober.finik.core.designsystem.component.PrimaryButton
import dev.bober.finik.core.designsystem.component.ScreenTitle
import dev.bober.finik.core.designsystem.component.ShapeDot
import dev.bober.finik.core.designsystem.component.TagChip
import dev.bober.finik.core.designsystem.component.glyphColor
import dev.bober.finik.core.designsystem.component.glyphShape
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.FinikTheme
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.WeekPlan
import androidx.compose.foundation.background as bg

/** Вкладка «Лавка»: сетка 2×3 товаров, скидки в процентах, покупка из статьи плана. */
@Composable
internal fun ShopScreen(
    modifier: Modifier = Modifier,
    items: List<ShopItem> = SampleData.shop,
    plan: WeekPlan = SampleData.plan,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .bg(FinikColor.Background)
            .verticalScroll(rememberScrollState())
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
                text = "Списывается из статьи плана",
                style = nunito(12),
                color = FinikColor.Text48,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(max = 160.dp),
            )
        }
        items.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                pair.forEach { item ->
                    ShopCard(item = item, left = plan.entry(item.category).left)
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RowScope.ShopCard(item: ShopItem, left: Int) {
    val canBuy = left >= item.cost
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
            ShapeDot(color = item.glyphColor, shape = item.glyphShape, size = 38.dp)
            if (item.isSale) {
                TagChip(
                    text = "−${item.salePercent}%",
                    background = FinikColor.RedSaleBg,
                    ink = FinikColor.RedSaleInk,
                    weight = FontWeight.ExtraBold,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = item.name, style = nunito(14.5, lineHeight = 1.2), color = FinikColor.Ink)
            Text(
                text = "${item.category.label} · осталось $left",
                style = nunito(11.5, FontWeight.SemiBold),
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
            text = if (canBuy) "Купить" else "Нет монет в статье",
            onClick = { /* TODO(logic): покупка */ },
            modifier = Modifier.fillMaxWidth(),
            height = 44.dp,
            radius = 12.dp,
            textStyle = nunito(13.5),
            containerColor = if (canBuy) FinikColor.Green else FinikColor.DisabledButton,
            contentColor = if (canBuy) FinikColor.Surface else FinikColor.Text56,
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ShopScreenPreview() {
    FinikTheme { ShopScreen() }
}
