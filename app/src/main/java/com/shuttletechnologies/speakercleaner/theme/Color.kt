package com.shuttletechnologies.speakercleaner.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val accent: Color,
    val accentSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val cardGlow: Color
)

val DarkStudioColors = AppColorScheme(
    isDark = true,
    background = Color(0xFF0A0E17),
    surface = Color(0xFF101524),
    surfaceElevated = Color(0xFF161F33),
    border = Color(0xFF24324F),
    accent = Color(0xFF00E5FF),
    accentSecondary = Color(0xFF00B0FF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF7E8EA8),
    success = Color(0xFF00E676),
    warning = Color(0xFFFFD600),
    danger = Color(0xFFFF1744),
    cardGlow = Color(0x3300E5FF)
)

val PitchBlackAmoledColors = AppColorScheme(
    isDark = true,
    background = Color(0xFF000000),
    surface = Color(0xFF08080C),
    surfaceElevated = Color(0xFF101016),
    border = Color(0xFF20202C),
    accent = Color(0xFF00F0FF),
    accentSecondary = Color(0xFF0091EA),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFC4CBD4),
    textMuted = Color(0xFF737A8C),
    success = Color(0xFF00E676),
    warning = Color(0xFFFFD600),
    danger = Color(0xFFFF1744),
    cardGlow = Color(0x3300F0FF)
)

val CrispLightColors = AppColorScheme(
    isDark = false,
    background = Color(0xFFF4F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFFFFFFF),
    border = Color(0xFFD6DFEB),
    accent = Color(0xFF0284C7),
    accentSecondary = Color(0xFF0EA5E9),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFEF4444),
    cardGlow = Color(0x1A0284C7)
)
