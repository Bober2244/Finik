package dev.bober.finik.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.tasks.TasksScreen
import kotlinx.serialization.Serializable

/** Вкладка «Задания». */
@Serializable
data object TasksRoute

fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    navigate(route = TasksRoute, navOptions = navOptions)
}

/** Задания с целью в другой вкладке открывают её через колбэки. */
fun NavGraphBuilder.tasksScreen(
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenShop: () -> Unit,
) {
    composable<TasksRoute> {
        TasksScreen(onOpenPlan = onOpenPlan, onOpenGoal = onOpenGoal, onOpenShop = onOpenShop)
    }
}
