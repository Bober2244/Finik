package dev.bober.finik.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.profile.BadgesScreen
import dev.bober.finik.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable

/** Оверлей «Профиль». */
@Serializable
data object ProfileRoute

/** Оверлей «Достижения» — открывается из профиля. */
@Serializable
data object BadgesRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(route = ProfileRoute, navOptions = navOptions)
}

fun NavGraphBuilder.profileScreens(navController: NavController) {
    composable<ProfileRoute> {
        ProfileScreen(
            onBack = { navController.popBackStack() },
            onOpenBadges = { navController.navigate(BadgesRoute) },
        )
    }
    composable<BadgesRoute> {
        BadgesScreen(onBack = { navController.popBackStack() })
    }
}
