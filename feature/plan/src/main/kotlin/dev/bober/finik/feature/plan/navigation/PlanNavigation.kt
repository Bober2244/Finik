package dev.bober.finik.feature.plan.navigation

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
import dev.bober.finik.feature.plan.PlanScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object PlanRoute

fun NavController.navigateToPlan(navOptions: NavOptions? = null) {
    navigate(route = PlanRoute, navOptions = navOptions)
}

fun NavGraphBuilder.planScreen(contentPadding: PaddingValues) {
    composable<PlanRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        PlanScreen(
            modifier = Modifier.padding(contentPadding),
            plan = state.plan,
            planConfirmed = state.planConfirmed,
            actionsEnabled = state.online && !busy,
            onAdjust = vm::changePlan,
            onAdvice = vm::applyAdvice,
            onReset = vm::resetPlan,
            onConfirm = vm::confirmPlan,
        )
    }
}
