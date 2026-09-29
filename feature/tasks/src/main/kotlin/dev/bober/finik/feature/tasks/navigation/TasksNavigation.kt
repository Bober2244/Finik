package dev.bober.finik.feature.tasks.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.tasks.TasksScreen
import dev.bober.finik.feature.tasks.TaskSessionSheet
import dev.bober.finik.feature.tasks.ReviewSheet
import dev.bober.finik.core.model.TaskItem
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
        val busy by vm.busy.collectAsStateWithLifecycle()
        var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
        var reviewOpen by remember { mutableStateOf(false) }
        TasksScreen(
            modifier = Modifier.padding(contentPadding),
            onOpenPlan = onOpenPlan,
            onOpenGoal = onOpenGoal,
            onOpenShop = onOpenShop,
            tasks = state.tasks,
            actionsEnabled = state.online && !busy,
            onTask = { selectedTask = it },
            onReview = { reviewOpen = true },
        )
        selectedTask?.let { task ->
            TaskSessionSheet(
                task = state.tasks.firstOrNull { it.id == task.id } ?: task,
                vm = vm,
                onClose = { selectedTask = null },
                onOpenPlan = { selectedTask = null; onOpenPlan() },
                onOpenGoal = { selectedTask = null; onOpenGoal() },
                onOpenShop = { selectedTask = null; onOpenShop() },
            )
        }
        if (reviewOpen) ReviewSheet(vm, onClose = { reviewOpen = false })
    }
}
