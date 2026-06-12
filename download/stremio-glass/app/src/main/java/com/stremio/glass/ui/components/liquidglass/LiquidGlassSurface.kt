package com.stremio.glass.ui.components.liquidglass

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.stremio.glass.ui.theme.*

/**
 * Core Liquid Glass surface composable.
 * Uses blur, tint overlays, and border highlights to create the glass effect.
 * On API 33+ the backdrop library's drawBackdrop handles full AGSL refraction.
 * This provides a beautiful fallback that works on all API levels.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 12.dp,
    tintColor: Color = GlassTint,
    borderColor: Color = GlassBorder,
    highlightColor: Color = GlassHighlight,
    enabled: Boolean = true,
    pressed: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val blurRadiusPx = with(density) { blurRadius.toPx() }

    val animatedPress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    )

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = RoundedCornerShape(cornerRadius)
                clip = true
                // On API 31+, use RenderEffect for blur
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && enabled) {
                    val blurPx = blurRadiusPx * (1f - animatedPress * 0.3f)
                    renderEffect = android.graphics.RenderEffect
                        .createBlurEffect(blurPx, blurPx, android.graphics.Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
            }
            .drawBehind {
                // Background tint
                drawRoundRect(
                    color = tintColor.copy(alpha = tintColor.alpha * (0.8f + animatedPress * 0.2f)),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )

                // Top-left highlight arc (simulates light refraction)
                val highlightWidth = size.width * 0.6f
                val highlightHeight = size.height * 0.3f
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            highlightColor.copy(alpha = 0.25f - animatedPress * 0.1f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(highlightWidth, highlightHeight)
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )

                // Border
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(cornerRadiusPx),
                    style = Stroke(width = 1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Liquid Glass Card with inner content padding.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 12.dp,
    tintColor: Color = GlassTint,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = cornerRadius,
        blurRadius = blurRadius,
        tintColor = tintColor
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * A simpler inline glass modifier that can be applied to any composable.
 */
fun Modifier.liquidGlass(
    cornerRadius: Dp = 16.dp,
    blurRadius: Dp = 12.dp,
    tintColor: Color = GlassTint,
    borderColor: Color = GlassBorder,
    highlightColor: Color = GlassHighlight,
    pressed: Boolean = false
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawBehind {
        val cornerRadiusPx = cornerRadius.toPx()
        drawRoundRect(
            color = tintColor,
            cornerRadius = CornerRadius(cornerRadiusPx)
        )
        // Highlight
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(highlightColor, Color.Transparent),
                start = Offset.Zero,
                end = Offset(size.width * 0.5f, size.height * 0.3f)
            ),
            cornerRadius = CornerRadius(cornerRadiusPx)
        )
        // Border
        drawRoundRect(
            color = borderColor,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = 1.dp.toPx())
        )
    }
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(if (pressed) blurRadius * 0.7f else blurRadius)
        } else Modifier
    )


