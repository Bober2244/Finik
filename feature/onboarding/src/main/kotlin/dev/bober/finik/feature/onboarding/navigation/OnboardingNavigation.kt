package dev.bober.finik.feature.onboarding.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.model.PetPotStyle
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.feature.onboarding.NamePetScreen
import dev.bober.finik.feature.onboarding.PickPetScreen
import dev.bober.finik.feature.onboarding.WelcomeScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object PickPetRoute

@Serializable
internal data class NamePetRoute(val species: String, val pot: String)

fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onFinished: () -> Unit,
) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeScreen(onStart = { navController.navigate(PickPetRoute) })
        }
        composable<PickPetRoute> {
            PickPetScreen(
                onPick = { species, pot ->
                    navController.navigate(NamePetRoute(species.name, pot.name))
                },
            )
        }
        composable<NamePetRoute> { entry ->
            val route = entry.toRoute<NamePetRoute>()
            val vm: FinikViewModel = koinViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            LaunchedEffect(state.onboarded) {
                if (state.onboarded) onFinished()
            }
            NamePetScreen(
                species = runCatching { PetSpecies.valueOf(route.species) }.getOrDefault(PetSpecies.FINIK),
                potStyle = runCatching { PetPotStyle.valueOf(route.pot) }.getOrDefault(PetPotStyle.CLAY),
                onFinish = { name, income ->
                    vm.createProfile(
                        name,
                        runCatching { PetSpecies.valueOf(route.species) }.getOrDefault(PetSpecies.FINIK),
                        runCatching { PetPotStyle.valueOf(route.pot) }.getOrDefault(PetPotStyle.CLAY),
                        income,
                    )
                },
            )
        }
    }
}
