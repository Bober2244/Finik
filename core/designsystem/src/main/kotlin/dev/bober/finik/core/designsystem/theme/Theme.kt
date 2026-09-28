package dev.bober.finik.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val lightFinikColorScheme = lightColorScheme(
    primary = LightFinikPalette.Green,
    onPrimary = LightFinikPalette.InkDeep,
    primaryContainer = LightFinikPalette.GreenCard,
    onPrimaryContainer = LightFinikPalette.GreenInk34,
    secondary = LightFinikPalette.Coin,
    onSecondary = LightFinikPalette.CoinInk,
    secondaryContainer = LightFinikPalette.GreenPill,
    onSecondaryContainer = LightFinikPalette.GreenInk36,
    tertiary = LightFinikPalette.Play,
    onTertiary = LightFinikPalette.InkDeep,
    tertiaryContainer = LightFinikPalette.ChipTrait,
    onTertiaryContainer = LightFinikPalette.Ink,
    background = LightFinikPalette.Background,
    onBackground = LightFinikPalette.Ink,
    surface = LightFinikPalette.Surface,
    onSurface = LightFinikPalette.Ink,
    surfaceVariant = LightFinikPalette.Chip,
    onSurfaceVariant = LightFinikPalette.Text50,
    surfaceTint = LightFinikPalette.Green,
    surfaceDim = LightFinikPalette.Background,
    surfaceBright = LightFinikPalette.Surface,
    surfaceContainerLowest = LightFinikPalette.Background,
    surfaceContainerLow = LightFinikPalette.Surface,
    surfaceContainer = LightFinikPalette.SurfaceMuted,
    surfaceContainerHigh = LightFinikPalette.IconButton,
    surfaceContainerHighest = LightFinikPalette.Chip,
    outline = LightFinikPalette.Border,
    outlineVariant = LightFinikPalette.BorderSoft,
    error = LightFinikPalette.Red,
    onError = LightFinikPalette.Surface,
    errorContainer = LightFinikPalette.RedNoteBg,
    onErrorContainer = LightFinikPalette.RedNoteInk,
)

private val darkFinikColorScheme = darkColorScheme(
    primary = DarkFinikPalette.Green,
    onPrimary = DarkFinikPalette.InkDeep,
    primaryContainer = DarkFinikPalette.GreenCard,
    onPrimaryContainer = DarkFinikPalette.GreenInk34,
    secondary = DarkFinikPalette.Coin,
    onSecondary = DarkFinikPalette.CoinInkStreak,
    secondaryContainer = DarkFinikPalette.GreenPill,
    onSecondaryContainer = DarkFinikPalette.GreenInk36,
    tertiary = DarkFinikPalette.Play,
    onTertiary = DarkFinikPalette.InkDeep,
    tertiaryContainer = DarkFinikPalette.ChipTrait,
    onTertiaryContainer = DarkFinikPalette.Ink,
    background = DarkFinikPalette.Background,
    onBackground = DarkFinikPalette.Ink,
    surface = DarkFinikPalette.Surface,
    onSurface = DarkFinikPalette.Ink,
    surfaceVariant = DarkFinikPalette.Chip,
    onSurfaceVariant = DarkFinikPalette.Text50,
    surfaceTint = DarkFinikPalette.Green,
    surfaceDim = DarkFinikPalette.Background,
    surfaceBright = DarkFinikPalette.SurfaceMuted,
    surfaceContainerLowest = DarkFinikPalette.Background,
    surfaceContainerLow = DarkFinikPalette.Surface,
    surfaceContainer = DarkFinikPalette.SurfaceMuted,
    surfaceContainerHigh = DarkFinikPalette.IconButton,
    surfaceContainerHighest = DarkFinikPalette.Chip,
    outline = DarkFinikPalette.Border,
    outlineVariant = DarkFinikPalette.BorderSoft,
    error = DarkFinikPalette.Red,
    onError = DarkFinikPalette.InkDeep,
    errorContainer = DarkFinikPalette.RedNoteBg,
    onErrorContainer = DarkFinikPalette.RedNoteInk,
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

/** Следует системной светлой или тёмной теме во всех экранах приложения. */
@Composable
fun FinikTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalFinikPalette provides if (isDark) DarkFinikPalette else LightFinikPalette) {
        MaterialTheme(
            colorScheme = if (isDark) darkFinikColorScheme else lightFinikColorScheme,
            typography = FinikTypography,
            shapes = FinikShapes,
            content = content,
        )
    }
}
