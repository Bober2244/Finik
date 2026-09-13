package dev.bober.finik.feature.growth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.growth.GrowthScreen
import kotlinx.serialization.Serializable

/** Полноэкранный оверлей «Рост Финика» (открывается из верхней панели). */
@Serializable
data object GrowthRoute

fun NavController.navigateToGrowth(navOptions: NavOptions? = null) {
    navigate(route = GrowthRoute, navOptions = navOptions)
}

fun NavGraphBuilder.growthScreen(
    onBack: () -> Unit,
    onOpenReport: () -> Unit,
) {
    composable<GrowthRoute> {
        GrowthScreen(onBack = onBack, onOpenReport = onOpenReport)
    }
}
