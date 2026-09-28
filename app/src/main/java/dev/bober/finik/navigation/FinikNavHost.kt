package dev.bober.finik.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import dev.bober.finik.core.navigation.FinikTransitions
import dev.bober.finik.core.navigation.topLevelNavOptions
import dev.bober.finik.feature.goal.navigation.goalScreen
import dev.bober.finik.feature.goal.navigation.navigateToGoal
import dev.bober.finik.feature.growth.navigation.growthScreen
import dev.bober.finik.feature.home.navigation.HomeRoute
import dev.bober.finik.feature.home.navigation.homeScreen
import dev.bober.finik.feature.home.navigation.navigateToHome
import dev.bober.finik.feature.onboarding.navigation.OnboardingGraph
import dev.bober.finik.feature.onboarding.navigation.onboardingGraph
import dev.bober.finik.feature.plan.navigation.navigateToPlan
import dev.bober.finik.feature.plan.navigation.planScreen
import dev.bober.finik.feature.profile.navigation.profileScreens
import dev.bober.finik.feature.report.navigation.navigateToReport
import dev.bober.finik.feature.report.navigation.reportScreen
import dev.bober.finik.feature.shop.navigation.navigateToShop
import dev.bober.finik.feature.shop.navigation.shopScreen
import dev.bober.finik.feature.tasks.navigation.navigateToTasks
import dev.bober.finik.feature.tasks.navigation.tasksScreen

@Composable
fun FinikNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    topLevelPadding: PaddingValues = PaddingValues(),
    startOnboarding: Boolean = true,
) {
    NavHost(
        navController = navController,
        startDestination = if (startOnboarding) OnboardingGraph else HomeRoute,
        modifier = modifier,
        enterTransition = {
            val transition = if (initialState.isTopLevel() && targetState.isTopLevel()) {
                FinikTransitions.enter
            } else {
                FinikTransitions.nestedEnter
            }
            transition()
        },
        exitTransition = {
            val transition = if (initialState.isTopLevel() && targetState.isTopLevel()) {
                FinikTransitions.exit
            } else {
                FinikTransitions.nestedExit
            }
            transition()
        },
        popEnterTransition = {
            val transition = if (initialState.isTopLevel() && targetState.isTopLevel()) {
                FinikTransitions.popEnter
            } else {
                FinikTransitions.nestedPopEnter
            }
            transition()
        },
        popExitTransition = {
            val transition = if (initialState.isTopLevel() && targetState.isTopLevel()) {
                FinikTransitions.popExit
            } else {
                FinikTransitions.nestedPopExit
            }
            transition()
        },
    ) {
        onboardingGraph(
            navController = navController,
            onFinished = {
                navController.navigateToHome(
                    navOptions { popUpTo<OnboardingGraph> { inclusive = true } },
                )
            },
        )

        homeScreen(
            contentPadding = topLevelPadding,
            onOpenPlan = { navController.navigateToPlan(topLevelNavOptions<HomeRoute>()) },
            onOpenGoal = { navController.navigateToGoal(topLevelNavOptions<HomeRoute>()) },
            onOpenTasks = { navController.navigateToTasks(topLevelNavOptions<HomeRoute>()) },
            onCloseWeek = { navController.navigateToReport() },
        )
        planScreen(contentPadding = topLevelPadding)
        tasksScreen(
            contentPadding = topLevelPadding,
            onOpenPlan = { navController.navigateToPlan(topLevelNavOptions<HomeRoute>()) },
            onOpenGoal = { navController.navigateToGoal(topLevelNavOptions<HomeRoute>()) },
            onOpenShop = { navController.navigateToShop(topLevelNavOptions<HomeRoute>()) },
        )
        shopScreen(contentPadding = topLevelPadding)
        goalScreen(contentPadding = topLevelPadding)

        growthScreen(
            onBack = { navController.popBackStack() },
            onOpenReport = { navController.navigateToReport() },
        )
        profileScreens(navController = navController)
        reportScreen(
            onBack = { navController.popBackStack() },
            onNewPlan = { navController.navigateToPlan(topLevelNavOptions<HomeRoute>()) },
        )
    }
}

private fun NavBackStackEntry.isTopLevel(): Boolean =
    TopLevelDestination.entries.any { tab ->
        destination.hierarchy.any { it.hasRoute(tab.route) }
    }
