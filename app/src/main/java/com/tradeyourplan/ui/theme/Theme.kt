// app/src/main/java/com/tradeyourplan/ui/theme/Theme.kt
package com.tradeyourplan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    onPrimary = OnPrimary,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    tertiary = Accent,
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Destructive,
    onError = Color.White,
    outline = Border
)

private val WarmEncouragingColorScheme = lightColorScheme(
    primary = WarmPrimary,
    primaryContainer = WarmPrimaryContainer,
    onPrimary = WarmOnPrimary,
    onPrimaryContainer = WarmOnPrimaryContainer,
    secondary = WarmSecondary,
    tertiary = WarmAccent,
    background = WarmBackground,
    surface = WarmSurface,
    onBackground = WarmOnBackground,
    onSurface = WarmOnSurface,
    error = Destructive,
    onError = Color.White,
    outline = WarmBorder
)

private val MinimalLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimary = LightOnPrimary,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    tertiary = LightAccent,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    error = Destructive,
    onError = Color.White,
    outline = LightBorder
)

enum class ThemeMode {
    PROFESSIONAL_DARK,
    WARM_ENCOURAGING,
    MINIMAL_LIGHT;

    val displayName: String
        get() = when (this) {
            PROFESSIONAL_DARK -> "专业深色"
            WARM_ENCOURAGING -> "温馨鼓励"
            MINIMAL_LIGHT -> "极简浅色"
        }
}

@Composable
fun TradeYourPlanTheme(
    themeMode: ThemeMode = ThemeMode.PROFESSIONAL_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.PROFESSIONAL_DARK -> ProfessionalDarkColorScheme
        ThemeMode.WARM_ENCOURAGING -> WarmEncouragingColorScheme
        ThemeMode.MINIMAL_LIGHT -> MinimalLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
