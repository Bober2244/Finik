package dev.bober.finik.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.home.HomeScreen
import kotlinx.serialization.Serializable

/** Вкладка «Питомец». */
@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(route = HomeRoute, navOptions = navOptions)
}

fun NavGraphBuilder.homeScreen(
    onOpenPlan: () -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(onOpenPlan = onOpenPlan)
    }
}
