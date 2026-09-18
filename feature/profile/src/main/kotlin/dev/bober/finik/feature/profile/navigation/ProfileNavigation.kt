package dev.bober.finik.feature.profile.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
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
        ProfileScreen(
            onBack = { navController.popBackStack() },
            onOpenBadges = { navController.navigate(BadgesRoute) },
            onOpenAdult = { navController.navigate(AdultRoute) },
            earnedTotal = state.earnedTotal,
            savedTotal = state.selectedGoal.saved,
            weeksDone = state.weeksDone,
            income = state.plan.weeklyIncome,
            soundOn = state.soundOn,
            badgesDone = state.badges.count { it.isDone },
            badgesTotal = state.badges.size,
            onIncome = vm::setIncome,
            onSound = vm::setSound,
            onReset = {
                vm.resetProfile()
                navController.popBackStack()
            },
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
        AdultScreen(
            weeksDone = state.weeksDone,
            saved = state.selectedGoal.saved,
            stageName = state.pet.stage.name,
            tasksDone = state.tasks.count { it.done },
            tasksTotal = state.tasks.size,
            demoMode = state.demoMode,
            onBack = { navController.popBackStack() },
            onToggleDemo = vm::setDemo,
            onBonus = vm::parentBonus,
            onReset = {
                vm.resetProfile()
                navController.popBackStack()
            },
        )
    }
}
