package dev.bober.finik.feature.profile.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.designsystem.component.ServerConnectionDialog
import dev.bober.finik.feature.profile.AdultScreen
import dev.bober.finik.feature.profile.BadgesScreen
import dev.bober.finik.feature.profile.HelpScreen
import dev.bober.finik.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object ProfileRoute

@Serializable
data object BadgesRoute

@Serializable
data object HelpRoute

@Serializable
data object AdultRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(route = ProfileRoute, navOptions = navOptions)
}

fun NavController.navigateToHelp(navOptions: NavOptions? = null) {
    navigate(route = HelpRoute, navOptions = navOptions)
}

fun NavGraphBuilder.profileScreens(navController: NavController) {
    composable<ProfileRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        val serverAddress by vm.serverAddress.collectAsStateWithLifecycle()
        var serverDialog by remember { mutableStateOf(false) }
        ProfileScreen(
            onBack = { navController.popBackStack() },
            onOpenBadges = { navController.navigate(BadgesRoute) },
            onOpenAdult = { navController.navigate(AdultRoute) },
            earnedTotal = state.earnedTotal,
            savedTotal = state.goals.sumOf { it.saved },
            weeksDone = state.weeksDone,
            income = state.nextWeekIncome,
            actionsEnabled = state.online && !busy,
            demoMode = state.demoMode,
            onDemo = { prepared -> vm.startDemo(prepared); navController.popBackStack() },
            canChangeServer = vm.canChangeServer,
            onOpenServer = { serverDialog = true },
            motionOn = state.motionOn,
            badgesDone = state.badges.count { it.isDone },
            badgesTotal = state.badges.size,
            onIncome = vm::setIncome,
            onMotion = vm::setMotion,
        )
        if (serverDialog && vm.canChangeServer) ServerConnectionDialog(
            currentAddress = serverAddress,
            busy = busy,
            onConnect = { address, result -> vm.connectToServer(address, result) },
            onDismiss = { serverDialog = false },
        )
    }
    composable<BadgesRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        BadgesScreen(onBack = { navController.popBackStack() }, badges = state.badges)
    }
    composable<HelpRoute> {
        val vm: FinikViewModel = koinViewModel()
        HelpScreen(terms = vm.repo.glossary, onBack = { navController.popBackStack() })
    }
    composable<AdultRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        val onlineExtras by vm.extrasEnabled.collectAsStateWithLifecycle()
        AdultScreen(
            weeksDone = state.weeksDone,
            saved = state.selectedGoal.saved,
            stageName = state.pet.stage.name,
            tasksDone = state.tasks.count { it.done },
            tasksTotal = state.tasks.size,
            onlineExtras = onlineExtras,
            actionsEnabled = state.online && !busy,
            wordOfDay = state.wordOfDay,
            todayEvent = state.todayEvent,
            onBack = { navController.popBackStack() },
            onBonus = { pin, amount, reason -> vm.parentBonus(pin, amount, reason) },
            onLoadExtra = vm::loadExtra,
            onLoadQuiz = vm::loadAiQuiz,
            onAnswerQuiz = vm::answerAiQuiz,
            onChooseEvent = vm::chooseEvent,
            onChat = vm::chat,
            onReset = {
                vm.resetProfile { navController.popBackStack() }
            },
        )
    }
}
