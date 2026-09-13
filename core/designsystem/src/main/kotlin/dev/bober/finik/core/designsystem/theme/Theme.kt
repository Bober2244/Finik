package dev.bober.finik.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val FinikColorScheme = lightColorScheme(
    primary = FinikColor.Green,
    onPrimary = FinikColor.Surface,
    primaryContainer = FinikColor.GreenCard,
    onPrimaryContainer = FinikColor.GreenInk34,
    secondary = FinikColor.Coin,
    onSecondary = FinikColor.CoinInk,
    background = FinikColor.Background,
    onBackground = FinikColor.Ink,
    surface = FinikColor.Surface,
    onSurface = FinikColor.Ink,
    surfaceVariant = FinikColor.Chip,
    onSurfaceVariant = FinikColor.Text50,
    outline = FinikColor.Border,
    error = FinikColor.Red,
    onError = FinikColor.Surface,
)

private val FinikTypography = Typography(
    headlineLarge = unbounded(34, lineHeight = 1.05),
    headlineMedium = unbounded(23, lineHeight = 1.15),
    headlineSmall = unbounded(21),
    titleLarge = unbounded(19, lineHeight = 1.1),
    titleMedium = nunito(17),
    titleSmall = nunito(14.5),
    bodyLarge = nunito(16, FontWeight.SemiBold, lineHeight = 1.5),
    bodyMedium = nunito(13, FontWeight.SemiBold, lineHeight = 1.35),
    bodySmall = nunito(12.5, FontWeight.SemiBold, lineHeight = 1.45),
    labelLarge = nunito(17),
    labelMedium = nunito(12.5),
    labelSmall = nunito(11),
)

private val FinikShapes = Shapes(
    extraSmall = RoundedCornerShape(7.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/**
 * Тема приложения. Макет светлый и один; тёмной темы в нём нет,
 * поэтому цвета фиксированы и не зависят от системной темы.
 */
@Composable
fun FinikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinikColorScheme,
        typography = FinikTypography,
        shapes = FinikShapes,
        content = content,
    )
}
