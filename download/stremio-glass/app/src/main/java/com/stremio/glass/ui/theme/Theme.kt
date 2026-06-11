package com.stremio.glass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = AccentSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentTertiary,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = GlassTint,
    error = ErrorRed,
    onError = TextPrimary
)

data class ExtendedColors(
    val glassTint: Color = GlassTint,
    val glassHighlight: Color = GlassHighlight,
    val glassBorder: Color = GlassBorder,
    val textTertiary: Color = TextTertiary,
    val imdbYellow: Color = ImdbYellow,
    val cardBackground: Color = DarkCard,
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

@Composable
fun StremioGlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Always dark for the glass effect

    CompositionLocalProvider(LocalExtendedColors provides ExtendedColors()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StremioTypography,
            content = content
        )
    }
}

object StremioTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
