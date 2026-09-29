package com.shuttletechnologies.speakercleaner.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalAppColors = staticCompositionLocalOf { DarkStudioColors }

@Composable
fun SpeakerCleanerTheme(
    themeMode: Int = 0, // 0: System, 1: Dark Studio, 2: AMOLED, 3: Light
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val appColors = when (themeMode) {
        1 -> DarkStudioColors
        2 -> PitchBlackAmoledColors
        3 -> CrispLightColors
        else -> if (systemInDark) DarkStudioColors else CrispLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !appColors.isDark
                insetsController.isAppearanceLightNavigationBars = !appColors.isDark
            }
        }
    }

    val materialColors = if (appColors.isDark) {
        darkColorScheme(
            primary = appColors.accent,
            secondary = appColors.accentSecondary,
            background = appColors.background,
            surface = appColors.surface,
            onPrimary = appColors.background,
            onSecondary = appColors.background,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = appColors.accent,
            secondary = appColors.accentSecondary,
            background = appColors.background,
            surface = appColors.surface,
            onPrimary = appColors.textPrimary,
            onSecondary = appColors.textPrimary,
            onBackground = appColors.textPrimary,
            onSurface = appColors.textPrimary
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}
