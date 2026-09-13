package dev.bober.finik.navigation

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.bober.finik.core.designsystem.component.DotShape
import dev.bober.finik.core.designsystem.component.FinikNavItem
import dev.bober.finik.feature.goal.navigation.GoalRoute
import dev.bober.finik.feature.home.navigation.HomeRoute
import dev.bober.finik.feature.plan.navigation.PlanRoute
import dev.bober.finik.feature.shop.navigation.ShopRoute
import dev.bober.finik.feature.tasks.navigation.TasksRoute
import kotlin.reflect.KClass

/** Вкладки нижней навигации. Порядок enum = порядок в баре; фигуры точек — из макета. */
enum class TopLevelDestination(
    val label: String,
    val glyph: Shape,
    val route: KClass<*>,
) {
    HOME(label = "Питомец", glyph = DotShape.Circle, route = HomeRoute::class),
    PLAN(label = "План", glyph = DotShape.rounded(4.dp), route = PlanRoute::class),
    TASKS(label = "Задания", glyph = DotShape.leaf(4.dp), route = TasksRoute::class),
    SHOP(label = "Лавка", glyph = DotShape.rounded(5.dp), route = ShopRoute::class),
    GOAL(label = "Мечта", glyph = DotShape.drop(5.dp), route = GoalRoute::class),
}

fun TopLevelDestination.toNavItem(): FinikNavItem = FinikNavItem(id = name, label = label, glyph = glyph)
