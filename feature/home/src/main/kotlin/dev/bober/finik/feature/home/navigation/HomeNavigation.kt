package dev.bober.finik.feature.home.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.pet.PetAnimation
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.home.HomeScreen
import dev.bober.finik.feature.home.HomeExtrasSheet
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(route = HomeRoute, navOptions = navOptions)
}

fun NavGraphBuilder.homeScreen(
    contentPadding: PaddingValues,
    onOpenPlan: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenTasks: () -> Unit,
    onCloseWeek: () -> Unit,
) {
    composable<HomeRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        var extrasOpen by remember { mutableStateOf(false) }
        var action by remember { mutableStateOf(PetAnimation.IDLE) }
        var actionEventId by remember { mutableLongStateOf(0L) }
        LaunchedEffect(vm) {
            vm.careEvents.collect { event ->
                action = when (event.category) {
                    SpendCategory.WATER -> PetAnimation.DRINK
                    SpendCategory.FOOD -> PetAnimation.EAT
                    SpendCategory.PLAY -> PetAnimation.DANCE
                    SpendCategory.SAVE -> PetAnimation.GREET
                }
                actionEventId = event.sequence
            }
        }
        HomeScreen(
            modifier = Modifier.padding(contentPadding),
            onOpenPlan = onOpenPlan,
            pet = state.pet,
            needs = state.needs,
            plan = state.plan,
            care = state.care,
            streak = state.streak,
            planConfirmed = state.planConfirmed,
            actionsEnabled = state.online && !busy,
            demoMode = state.demoMode,
            canAdvanceTime = state.canAdvanceTime,
            weekLabel = "Неделя ${state.weekNumber} · до ${localDate(state.weekEnd, state.timezone)} · ${state.timezone}",
            todayEvent = state.todayEvent,
            wordOfDay = state.wordOfDay,
            onChooseEvent = vm::chooseEvent,
            onAdvanceDay = vm::advanceDemoDay,
            onOpenStories = { extrasOpen = true },
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
        if (extrasOpen) HomeExtrasSheet(vm = vm, onClose = { extrasOpen = false })
    }
}

private fun localDate(value: String, timezone: String): String = runCatching {
    Instant.parse(value).atZone(ZoneId.of(timezone)).format(DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale.forLanguageTag("ru")))
}.getOrDefault(value.take(10))
