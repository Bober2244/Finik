package dev.bober.finik.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import dev.bober.finik.feature.onboarding.NamePetScreen
import dev.bober.finik.feature.onboarding.PickPetScreen
import dev.bober.finik.feature.onboarding.WelcomeScreen
import kotlinx.serialization.Serializable

/** Вложенный граф онбординга: приветствие → выбор ростка → имя и первый план. */
@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object PickPetRoute

@Serializable
internal data object NamePetRoute

fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onFinished: () -> Unit,
) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeScreen(onStart = { navController.navigate(PickPetRoute) })
        }
        composable<PickPetRoute> {
            PickPetScreen(onPick = { navController.navigate(NamePetRoute) })
        }
        composable<NamePetRoute> {
            NamePetScreen(onFinish = onFinished)
        }
    }
}
