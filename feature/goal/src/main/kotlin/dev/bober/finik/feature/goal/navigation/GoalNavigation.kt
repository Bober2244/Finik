package dev.bober.finik.feature.goal.navigation

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
import dev.bober.finik.core.data.game.GameEngine
import dev.bober.finik.feature.goal.GoalScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object GoalRoute

fun NavController.navigateToGoal(navOptions: NavOptions? = null) {
    navigate(route = GoalRoute, navOptions = navOptions)
}

fun NavGraphBuilder.goalScreen(contentPadding: PaddingValues) {
    composable<GoalRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        GoalScreen(
            modifier = Modifier.padding(contentPadding),
            goal = state.selectedGoal,
            goals = state.goals,
            plan = state.plan,
            history = state.history,
            transactions = state.transactions,
            averageWeeklySave = GameEngine.averageActualWeeklyContribution(state, state.selectedGoal.id),
            weeksLeft = GameEngine.weeksToGoal(state, state.selectedGoal),
            weeksDone = state.weeksDone,
            onDeposit = vm::deposit,
            onSelectGoal = vm::selectGoal,
            onWithdraw = vm::withdraw,
        )
    }
}
