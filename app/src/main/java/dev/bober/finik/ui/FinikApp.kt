package dev.bober.finik.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.data.ToastEvent
import dev.bober.finik.core.designsystem.component.FinikBottomNav
import dev.bober.finik.core.designsystem.component.FinikShellTopBar
import dev.bober.finik.core.designsystem.component.FinikToast
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.navigation.topLevelNavOptions
import dev.bober.finik.feature.goal.navigation.navigateToGoal
import dev.bober.finik.feature.growth.navigation.navigateToGrowth
import dev.bober.finik.feature.home.navigation.HomeRoute
import dev.bober.finik.feature.home.navigation.navigateToHome
import dev.bober.finik.feature.onboarding.navigation.OnboardingGraph
import dev.bober.finik.feature.plan.navigation.navigateToPlan
import dev.bober.finik.feature.profile.navigation.navigateToHelp
import dev.bober.finik.feature.profile.navigation.navigateToProfile
import dev.bober.finik.feature.shop.navigation.navigateToShop
import dev.bober.finik.feature.tasks.navigation.navigateToTasks
import dev.bober.finik.navigation.FinikNavHost
import dev.bober.finik.navigation.TopLevelDestination
import dev.bober.finik.navigation.toNavItem
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinikApp(modifier: Modifier = Modifier) {
    val vm: FinikViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    var toast by remember { mutableStateOf<ToastEvent?>(null) }

    LaunchedEffect(Unit) {
        vm.toasts.collect { event ->
            toast = event
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(3200)
            toast = null
        }
    }
    LaunchedEffect(state.ready, state.onboarded) {
        if (state.ready && !state.onboarded) {
            val onOnboarding = currentDestination?.hierarchy?.any { it.hasRoute(OnboardingGraph::class) } == true
            if (!onOnboarding && currentDestination != null) {
                navController.navigate(OnboardingGraph, navOptions { popUpTo(0) { inclusive = true } })
            }
        }
    }

    val currentTab = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.route) } == true
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FinikColor.Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (currentTab != null) {
                    FinikShellTopBar(
                        coins = state.spendableFree,
                        stageName = state.pet.stage.name,
                        xpPercent = state.pet.xp,
                        onStageClick = { navController.navigateToGrowth() },
                        onProfileClick = { navController.navigateToProfile() },
                        onHelpClick = { navController.navigateToHelp() },
                    )
                }
            },
            bottomBar = {
                if (currentTab != null) {
                    FinikBottomNav(
                        items = TopLevelDestination.entries.map { it.toNavItem() },
                        selectedId = currentTab.name,
                        onItemClick = { item ->
                            navController.navigateToTopLevel(TopLevelDestination.valueOf(item.id))
                        },
                    )
                }
            },
        ) { innerPadding ->
            if (state.ready) {
                FinikNavHost(
                    navController = navController,
                    startOnboarding = !state.onboarded,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 88.dp),
        ) {
            toast?.let { FinikToast(text = it.text, warning = it.warning) }
        }
    }
}

private fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    val navOptions = topLevelNavOptions<HomeRoute>()
    when (destination) {
        TopLevelDestination.HOME -> navigateToHome(navOptions)
        TopLevelDestination.PLAN -> navigateToPlan(navOptions)
        TopLevelDestination.TASKS -> navigateToTasks(navOptions)
        TopLevelDestination.SHOP -> navigateToShop(navOptions)
        TopLevelDestination.GOAL -> navigateToGoal(navOptions)
    }
}
