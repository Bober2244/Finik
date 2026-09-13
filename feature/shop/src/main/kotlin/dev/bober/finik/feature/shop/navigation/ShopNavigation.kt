package dev.bober.finik.feature.shop.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.shop.ShopScreen
import kotlinx.serialization.Serializable

/** Вкладка «Лавка». */
@Serializable
data object ShopRoute

fun NavController.navigateToShop(navOptions: NavOptions? = null) {
    navigate(route = ShopRoute, navOptions = navOptions)
}

fun NavGraphBuilder.shopScreen() {
    composable<ShopRoute> {
        ShopScreen()
    }
}
