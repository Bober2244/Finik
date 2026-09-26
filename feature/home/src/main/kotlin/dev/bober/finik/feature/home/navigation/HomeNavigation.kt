package dev.bober.finik.feature.home.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.pet.PetAnimation
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.home.HomeScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(route = HomeRoute, navOptions = navOptions)
}

fun NavGraphBuilder.homeScreen(
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenTasks: () -> Unit,
    onCloseWeek: () -> Unit,
) {
    composable<HomeRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        var action by remember { mutableStateOf(PetAnimation.IDLE) }
        var actionEventId by remember { mutableLongStateOf(0L) }
        LaunchedEffect(vm) {
            vm.careEvents.collect { event ->
                action = when (event.category) {
                    SpendCategory.WATER -> PetAnimation.DRINK
                    SpendCategory.FOOD -> PetAnimation.WALK
                    SpendCategory.PLAY -> PetAnimation.RUN
                    SpendCategory.SAVE -> PetAnimation.THINK
                }
                actionEventId = event.sequence
            }
        }
        HomeScreen(
            onOpenPlan = onOpenPlan,
            pet = state.pet,
            needs = state.needs,
            plan = state.plan,
            care = state.care,
            streak = state.streak,
            planConfirmed = state.planConfirmed,
            demoMode = state.demoMode,
            goalTitle = state.selectedGoal.title,
            goalSaved = state.selectedGoal.saved,
            goalTarget = state.selectedGoal.target,
            activeTask = state.activeTask?.title,
            onCare = vm::care,
            action = action,
            actionEventId = actionEventId,
            onOpenGoal = onOpenGoal,
            onOpenTasks = onOpenTasks,
            onCloseWeek = {
                vm.closeWeek { onCloseWeek() }
            },
        )
    }
}
