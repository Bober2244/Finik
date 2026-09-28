package dev.bober.finik.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import dev.bober.finik.core.designsystem.component.FinikNavItem
import dev.bober.finik.core.designsystem.component.FinikIcons
import dev.bober.finik.feature.goal.navigation.GoalRoute
import dev.bober.finik.feature.home.navigation.HomeRoute
import dev.bober.finik.feature.plan.navigation.PlanRoute
import dev.bober.finik.feature.shop.navigation.ShopRoute
import dev.bober.finik.feature.tasks.navigation.TasksRoute
import kotlin.reflect.KClass

/** Вкладки нижней навигации. Порядок enum = порядок в баре. */
enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: KClass<*>,
) {
    HOME(label = "Питомец", icon = FinikIcons.Pet, route = HomeRoute::class),
    PLAN(label = "План", icon = FinikIcons.Plan, route = PlanRoute::class),
    TASKS(label = "Задания", icon = FinikIcons.Tasks, route = TasksRoute::class),
    SHOP(label = "Лавка", icon = FinikIcons.Shop, route = ShopRoute::class),
    GOAL(label = "Мечта", icon = FinikIcons.Goal, route = GoalRoute::class),
}

fun TopLevelDestination.toNavItem(): FinikNavItem = FinikNavItem(id = name, label = label, icon = icon)
