package com.stremio.glass.ui.components.liquidglass

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Capability detection for Liquid Glass effects.
 * API 33+ = full glass (AGSL shaders + blur + vibrancy)
 * API 31+ = partial (blur + vibrancy, no refraction)
 * Below 31 = fallback (simple tint overlay)
 */
enum class GlassCapability {
    FULL,       // API 33+ - all effects
    PARTIAL,    // API 31-32 - blur + vibrancy only
    FALLBACK    // Below 31 - simple tint
}

val LocalGlassCapability = compositionLocalOf {
    when {
        Build.VERSION.SDK_INT >= 33 -> GlassCapability.FULL
        Build.VERSION.SDK_INT >= 31 -> GlassCapability.PARTIAL
        else -> GlassCapability.FALLBACK
    }
}

val LocalGlassInsets = compositionLocalOf { PaddingValues(0.dp) }

data class GlassDefaults(
    val blurRadius: Dp = 8.dp,
    val vibrancyAmount: Float = 1.5f,
    val refractionHeight: Dp = 12.dp,
    val refractionAmount: Dp = 24.dp,
    val chromaticAberration: Float = 0.5f,
    val shadowRadius: Dp = 8.dp,
    val highlightIntensity: Float = 1f,
    val cornerRadius: Dp = 16.dp
)

val LocalGlassDefaults = compositionLocalOf { GlassDefaults() }

@Composable
fun GlassTheme(
    defaults: GlassDefaults = GlassDefaults(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalGlassDefaults provides defaults,
        content = content
    )
}
