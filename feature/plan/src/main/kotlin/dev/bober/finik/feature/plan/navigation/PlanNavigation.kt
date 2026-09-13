package dev.bober.finik.feature.plan.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.plan.PlanScreen
import kotlinx.serialization.Serializable

/** Вкладка «План». */
@Serializable
data object PlanRoute

fun NavController.navigateToPlan(navOptions: NavOptions? = null) {
    navigate(route = PlanRoute, navOptions = navOptions)
}

fun NavGraphBuilder.planScreen() {
    composable<PlanRoute> {
        PlanScreen()
    }
}
