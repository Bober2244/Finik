package dev.bober.finik.feature.goal.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.goal.GoalScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object GoalRoute

fun NavController.navigateToGoal(navOptions: NavOptions? = null) {
    navigate(route = GoalRoute, navOptions = navOptions)
}

fun NavGraphBuilder.goalScreen() {
    composable<GoalRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        GoalScreen(
            goal = state.selectedGoal,
            goals = state.goals,
            plan = state.plan,
            history = state.history,
            onDeposit = vm::deposit,
            onSelectGoal = vm::selectGoal,
            onWithdraw = vm::withdraw,
        )
    }
}
