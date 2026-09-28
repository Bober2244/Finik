package dev.bober.finik.feature.tasks.navigation

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
import dev.bober.finik.feature.tasks.TasksScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object TasksRoute

fun NavController.navigateToTasks(navOptions: NavOptions? = null) {
    navigate(route = TasksRoute, navOptions = navOptions)
}

fun NavGraphBuilder.tasksScreen(
    contentPadding: PaddingValues,
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenShop: () -> Unit,
) {
    composable<TasksRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        TasksScreen(
            modifier = Modifier.padding(contentPadding),
            onOpenPlan = onOpenPlan,
            onOpenGoal = onOpenGoal,
            onOpenShop = onOpenShop,
            tasks = state.tasks,
            quiz = vm.repo.quiz,
            scenarios = vm.repo.scenarios,
            onComplete = vm::completeTask,
        )
    }
}
