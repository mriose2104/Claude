package com.misgastos.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtendedColors(
    val expenseUp: Color,
    val expenseDown: Color,
    val warning: Color,
    val danger: Color
)

private val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(ExpenseUp, ExpenseDown, WarningAmber, DangerRed)
}

val MaterialTheme.extended: ExtendedColors
    @Composable get() = LocalExtendedColors.current

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    tertiary = Tertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF1A1C1B),
    onSurface = Color(0xFF1A1C1B)
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF00391F),
    secondary = SecondaryDark,
    onSecondary = Color(0xFF00344E),
    tertiary = TertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFE3E8E4),
    onSurface = Color(0xFFE3E8E4)
)

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun MisGastosTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = ExtendedColors(ExpenseUp, ExpenseDown, WarningAmber, DangerRed)

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MisGastosTypography,
            content = content
        )
    }
}
