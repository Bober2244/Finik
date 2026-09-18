package dev.bober.finik.feature.growth.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.growth.GrowthScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

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
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        GrowthScreen(
            onBack = onBack,
            onOpenReport = onOpenReport,
            pet = state.pet,
            weekLog = state.weekLog,
        )
    }
}
