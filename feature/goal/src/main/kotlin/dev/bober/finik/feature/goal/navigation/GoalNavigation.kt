package dev.bober.finik.feature.goal.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.goal.GoalScreen
import kotlinx.serialization.Serializable

/** Вкладка «Мечта». */
@Serializable
data object GoalRoute

fun NavController.navigateToGoal(navOptions: NavOptions? = null) {
    navigate(route = GoalRoute, navOptions = navOptions)
}

fun NavGraphBuilder.goalScreen() {
    composable<GoalRoute> {
        GoalScreen()
    }
}
