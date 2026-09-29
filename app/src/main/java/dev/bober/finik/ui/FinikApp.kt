package dev.bober.finik.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import dev.bober.finik.core.designsystem.component.FinikWorldBackground
import dev.bober.finik.core.designsystem.component.ServerConnectionDialog
import dev.bober.finik.core.designsystem.theme.FinikColor
import dev.bober.finik.core.designsystem.theme.nunito
import dev.bober.finik.core.navigation.topLevelNavOptions
import dev.bober.finik.core.pet.LocalPetAnimationEnabled
import dev.bober.finik.core.pet.PetRuntimeProvider
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
fun FinikApp(
    modifier: Modifier = Modifier,
    vm: FinikViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val loadProblem by vm.loadProblem.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val serverAddress by vm.serverAddress.collectAsStateWithLifecycle()
    var serverDialog by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    var toast by remember { mutableStateOf<ToastEvent?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val readyForRefresh by rememberUpdatedState(state.ready && !busy)
    DisposableEffect(lifecycleOwner, vm) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && readyForRefresh) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        vm.toasts.collect { event ->
            toast = event
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(5000)
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
    val shellAvailable = state.ready && state.onboarded
    val shellVisible = shellAvailable && currentTab != null
    val shellOffset by animateFloatAsState(
        targetValue = if (shellVisible) 0f else -1f,
        animationSpec = tween(280),
        label = "shellOffset",
    )
    val shellModifier = Modifier
        .graphicsLayer { translationX = size.width * shellOffset }
        .then(if (shellVisible) Modifier else Modifier.clearAndSetSemantics { })

    if (serverDialog && vm.canChangeServer) {
        ServerConnectionDialog(
            currentAddress = serverAddress,
            busy = busy,
            onConnect = { address, callback -> vm.connectToServer(address, callback) },
            onDismiss = { serverDialog = false },
        )
    }

    CompositionLocalProvider(LocalPetAnimationEnabled provides state.motionOn) {
    Box(modifier = modifier.fillMaxSize()) {
        FinikWorldBackground(modifier = Modifier.matchParentSize())
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = FinikColor.Ink,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(modifier = Modifier.statusBarsPadding()) {
                if (shellVisible) {
                    FinikShellTopBar(
                        coins = state.plan.freeCoins,
                        stageName = state.pet.stage.name,
                        xpPercent = state.pet.xp,
                        onStageClick = { navController.navigateToGrowth() },
                        onProfileClick = { navController.navigateToProfile() },
                        onHelpClick = { navController.navigateToHelp() },
                        modifier = shellModifier,
                        enabled = shellVisible,
                    )
                }
                if (state.ready && (!state.online || state.demoMode || loadProblem != null)) {
                    Surface(color = FinikColor.CoinChip) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                                if (state.demoMode) Text("Деморежим · отдельный профиль", style = nunito(13))
                                if (!state.online) Text("Нет связи · доступен только просмотр", style = nunito(13))
                                loadProblem?.let { Text(it, style = nunito(12)) }
                            }
                            if (state.demoMode) TextButton(onClick = { vm.leaveDemo() }, enabled = !busy) { Text("Выйти") }
                            if (!state.online || loadProblem != null) TextButton(onClick = { vm.refresh() }, enabled = !busy) { Text("Повторить") }
                        }
                    }
                }
                if (vm.canChangeServer && !state.online) {
                    TextButton(onClick = { serverDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Изменить адрес сервера")
                    }
                }
                if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = FinikColor.Green)
                }
            },
            bottomBar = {
                if (shellVisible) {
                    FinikBottomNav(
                        items = TopLevelDestination.entries.map { it.toNavItem() },
                        selectedId = currentTab?.name,
                        onItemClick = { item ->
                            navController.navigateToTopLevel(TopLevelDestination.valueOf(item.id))
                        },
                        modifier = shellModifier,
                        enabled = shellVisible,
                    )
                }
            },
        ) { innerPadding ->
            if (loadProblem != null && !state.ready) {
                SaveRecoveryScreen(
                    problem = loadProblem ?: "Не удалось открыть сохранение.",
                    onRetry = { vm.retryLoad() },
                    modifier = Modifier.padding(innerPadding),
                )
            } else if (state.ready) {
                PetRuntimeProvider {
                    FinikNavHost(
                        navController = navController,
                        startOnboarding = !state.onboarded,
                        topLevelPadding = innerPadding,
                        modifier = if (currentTab == null) Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding) else Modifier.fillMaxSize(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = FinikColor.Green)
                    Text(text = "Загружаем Финика…", style = nunito(16), color = FinikColor.Ink)
                }
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
}

@Composable
private fun SaveRecoveryScreen(
    problem: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Не удалось открыть профиль", style = nunito(24), color = FinikColor.Ink)
        Text(text = problem, style = nunito(16), color = FinikColor.Ink, modifier = Modifier.padding(top = 12.dp, bottom = 24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Повторить") }
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
