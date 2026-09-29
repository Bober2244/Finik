package dev.bober.finik.feature.report.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.feature.report.ReportScreen
import dev.bober.finik.feature.report.EmptyReportScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object ReportRoute

fun NavController.navigateToReport(navOptions: NavOptions? = null) {
    navigate(route = ReportRoute, navOptions = navOptions)
}

fun NavGraphBuilder.reportScreen(
    onBack: () -> Unit,
    onNewPlan: () -> Unit,
) {
    composable<ReportRoute> {
        val vm: FinikViewModel = koinViewModel()
        val state by vm.state.collectAsStateWithLifecycle()
        val busy by vm.busy.collectAsStateWithLifecycle()
        val report = state.report
        if (report == null) {
            EmptyReportScreen(onBack = onBack)
            return@composable
        }
        ReportScreen(
            onBack = onBack,
            onNewPlan = onNewPlan,
            onRepeat = {
                vm.repeatLastPlan()
                onNewPlan()
            },
            week = report.week,
            rows = report.rows,
            summary = report.summary,
            note = report.note,
            canRepeat = state.online && !busy && !state.planConfirmed,
        )
    }
}
