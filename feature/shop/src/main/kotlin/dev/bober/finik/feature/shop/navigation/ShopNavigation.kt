package dev.bober.finik.feature.shop.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.shop.ShopScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object ShopRoute

fun NavController.navigateToShop(navOptions: NavOptions? = null) {
    navigate(route = ShopRoute, navOptions = navOptions)
}

fun NavGraphBuilder.shopScreen() {
    composable<ShopRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        ShopScreen(
            items = state.shop,
            pet = state.pet,
            onCustomize = vm::customizeAppearance,
            plan = state.plan,
            planConfirmed = state.planConfirmed,
            onCheck = vm.repo::buyCheck,
            onBuy = vm::buy,
        )
    }
}
