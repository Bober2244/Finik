package dev.bober.finik.feature.onboarding.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.designsystem.component.ServerConnectionDialog
import dev.bober.finik.core.model.PetAppearance
import dev.bober.finik.core.model.PetFurColor
import dev.bober.finik.core.model.PetSpecies
import dev.bober.finik.feature.onboarding.NamePetScreen
import dev.bober.finik.feature.onboarding.PickPetScreen
import dev.bober.finik.feature.onboarding.WelcomeScreen
import dev.bober.finik.feature.onboarding.toSelectedAccessories
import dev.bober.finik.feature.onboarding.toStoredAccessoryIds
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object OnboardingGraph

@Serializable
internal data object WelcomeRoute

@Serializable
internal data object PickPetRoute

@Serializable
internal data class NamePetRoute(val species: String, val fur: String, val accessories: String = "")

fun NavGraphBuilder.onboardingGraph(
    navController: NavController,
    onFinished: () -> Unit,
) {
    navigation<OnboardingGraph>(startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            val vm: FinikViewModel = koinViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val busy by vm.busy.collectAsStateWithLifecycle()
            val serverAddress by vm.serverAddress.collectAsStateWithLifecycle()
            var serverDialog by remember { mutableStateOf(false) }
            var newDemo by remember { mutableStateOf(false) }
            LaunchedEffect(state.onboarded, state.demoMode, busy, serverDialog) {
                if (state.onboarded && !serverDialog) onFinished()
                else if (newDemo && state.demoMode && !busy) {
                    newDemo = false
                    navController.navigate(PickPetRoute)
                }
            }
            WelcomeScreen(
                actionsEnabled = state.online && !busy,
                onStart = { navController.navigate(PickPetRoute) },
                onDemo = { prepared -> newDemo = !prepared; vm.startDemo(prepared) },
                canChangeServer = vm.canChangeServer,
                onOpenServer = { serverDialog = true },
            )
            if (serverDialog && vm.canChangeServer) ServerConnectionDialog(
                currentAddress = serverAddress,
                busy = busy,
                onConnect = { address, result -> vm.connectToServer(address, result) },
                onDismiss = { serverDialog = false },
            )
        }
        composable<PickPetRoute> {
            PickPetScreen(
                onPick = { species, appearance ->
                    navController.navigate(
                        NamePetRoute(species.name, appearance.furColor.name, appearance.accessories.toStoredAccessoryIds()),
                    )
                },
            )
        }
        composable<NamePetRoute> { entry ->
            val route = entry.toRoute<NamePetRoute>()
            val vm: FinikViewModel = koinViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val busy by vm.busy.collectAsStateWithLifecycle()
            LaunchedEffect(state.onboarded) {
                if (state.onboarded) onFinished()
            }
            val appearance = PetAppearance(
                furColor = PetFurColor.fromStored(route.fur),
                accessories = route.accessories.toSelectedAccessories(),
            )
            NamePetScreen(
                species = PetSpecies.fromStored(route.species),
                appearance = appearance,
                actionsEnabled = state.online && !busy,
                onFinish = { name, income ->
                    vm.createProfile(
                        name,
                        PetSpecies.fromStored(route.species),
                        appearance,
                        income,
                    )
                },
            )
        }
    }
}
