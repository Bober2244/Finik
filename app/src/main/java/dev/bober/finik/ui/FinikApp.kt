package dev.bober.finik.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.bober.finik.core.designsystem.component.FinikBottomNav
import dev.bober.finik.core.designsystem.component.FinikShellTopBar
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.model.SampleData
import dev.bober.finik.core.navigation.topLevelNavOptions
import dev.bober.finik.feature.goal.navigation.navigateToGoal
import dev.bober.finik.feature.growth.navigation.navigateToGrowth
import dev.bober.finik.feature.home.navigation.HomeRoute
import dev.bober.finik.feature.home.navigation.navigateToHome
import dev.bober.finik.feature.plan.navigation.navigateToPlan
import dev.bober.finik.feature.profile.navigation.navigateToProfile
import dev.bober.finik.feature.shop.navigation.navigateToShop
import dev.bober.finik.feature.tasks.navigation.navigateToTasks
import dev.bober.finik.navigation.FinikNavHost
import dev.bober.finik.navigation.TopLevelDestination
import dev.bober.finik.navigation.toNavItem

/**
 * Корневой composable. Верхняя панель (монеты, стадия, профиль) и нижняя навигация
 * показываются только на вкладках; онбординг и оверлеи занимают весь экран.
 */
@Composable
fun FinikApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val currentTab = TopLevelDestination.entries.firstOrNull { destination ->
        currentDestination?.hierarchy?.any { it.hasRoute(destination.route) } == true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinikColor.Background,
        // Панели сами учитывают системные отступы; экраны без панелей — тоже.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (currentTab != null) {
                FinikShellTopBar(
                    coins = SampleData.FREE_COINS,
                    stageName = SampleData.pet.stage.name,
                    xpPercent = SampleData.pet.xp,
                    onStageClick = { navController.navigateToGrowth() },
                    onProfileClick = { navController.navigateToProfile() },
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
        FinikNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
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
