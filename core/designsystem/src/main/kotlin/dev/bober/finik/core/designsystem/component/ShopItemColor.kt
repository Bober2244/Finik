package dev.bober.finik.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.model.ShopItem
import dev.bober.finik.core.model.SpendCategory

/** Keep the item's category color when a product illustration is unavailable. */
val ShopItem.glyphColor: Color
    @Composable get() = when (id) {
        "s3" -> FinikColor.FoodPot
        "s5" -> FinikColor.PlayPot
        "s6" -> FinikColor.WaterCan
        else -> when (category) {
            SpendCategory.FOOD -> FinikColor.FoodBright
            SpendCategory.WATER -> FinikColor.Water
            SpendCategory.PLAY -> FinikColor.Play
            SpendCategory.SAVE -> FinikColor.Green
        }
    }
