package com.filamentvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OledDarkColorScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = OledOnPrimary,
    primaryContainer = OledPrimaryContainer,
    onPrimaryContainer = OledOnPrimaryContainer,
    secondary = OledSecondary,
    onSecondary = OledOnSecondary,
    secondaryContainer = OledSecondaryContainer,
    onSecondaryContainer = OledOnSecondaryContainer,
    tertiary = OledTertiary,
    onTertiary = OledOnTertiary,
    tertiaryContainer = OledTertiaryContainer,
    onTertiaryContainer = OledOnTertiaryContainer,
    surface = OledSurface,
    surfaceVariant = OledSurfaceVariant,
    onSurface = OledOnSurface,
    onSurfaceVariant = OledOnSurfaceVariant,
    background = OledBackground,
    onBackground = OledOnSurface,
    error = OledError,
    onError = OledOnError,
    errorContainer = OledErrorContainer,
    onErrorContainer = OledOnErrorContainer,
    outline = OledOutline,
    outlineVariant = OledOutlineVariant,
    surfaceContainerLowest = OledSurfaceContainerLowest,
    surfaceContainerLow = OledSurfaceContainerLow,
    surfaceContainer = OledSurfaceContainer,
    surfaceContainerHigh = OledSurfaceContainerHigh,
    surfaceContainerHighest = OledSurfaceContainerHighest
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E)
)

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

@Composable
fun FilamentVaultTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> OledDarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) OledDarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
