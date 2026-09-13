package dev.bober.finik.core.navigation

import androidx.navigation.NavOptions
import androidx.navigation.navOptions

/**
 * Опции для переключения вкладок нижней навигации: стек очищается до корневой вкладки [Root]
 * с сохранением состояния, один экземпляр вкладки, восстановление её состояния.
 */
inline fun <reified Root : Any> topLevelNavOptions(): NavOptions = navOptions {
    popUpTo<Root> {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
