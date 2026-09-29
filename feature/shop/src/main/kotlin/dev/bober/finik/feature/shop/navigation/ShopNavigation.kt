package dev.bober.finik.feature.shop.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

fun NavGraphBuilder.shopScreen(contentPadding: PaddingValues) {
    composable<ShopRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        ShopScreen(
            modifier = Modifier.padding(contentPadding),
            items = state.shop,
            pet = state.pet,
            onCustomize = vm::customizeAppearance,
            plan = state.plan,
            planConfirmed = state.planConfirmed,
            actionsEnabled = state.online && !busy,
            onCheck = vm.repo::buyCheck,
            onBuy = vm::buy,
        )
    }
}
