package dev.bober.finik.feature.report.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import dev.bober.finik.feature.report.ReportScreen
import kotlinx.serialization.Serializable

/** Отчёт по итогам недели (вариант 2c макета). */
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
        ReportScreen(onBack = onBack, onNewPlan = onNewPlan, onRepeat = onBack)
    }
}
