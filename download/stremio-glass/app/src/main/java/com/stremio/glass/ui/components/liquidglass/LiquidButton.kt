package com.stremio.glass.ui.components.liquidglass

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.stremio.glass.ui.theme.*

/**
 * Liquid Glass Button - A frosted glass button with interactive press effects.
 * Follows the Kyant0 LiquidButton design: vibrancy + blur + refraction lens,
 * with press scaling and interactive highlight.
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = AccentPrimary.copy(alpha = 0.15f),
    pressed: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1f
            isPressed -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val tintAlpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.1f
            isPressed -> tint.alpha * 1.5f
            else -> tint.alpha
        },
        animationSpec = tween(150)
    )

    LiquidGlassSurface(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        cornerRadius = 28.dp,
        blurRadius = 8.dp,
        tintColor = tint.copy(alpha = tintAlpha.coerceAtMost(1f)),
        pressed = isPressed
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

/**
 * Liquid Glass Icon Button - Compact glass button for icon actions.
 */
@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = GlassTint,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )

    LiquidGlassSurface(
        modifier = modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        cornerRadius = 14.dp,
        blurRadius = 6.dp,
        tintColor = tint,
        pressed = isPressed
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center,
            content = content
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(120)
            isPressed = false
        }
    }
}

/**
 * Liquid Glass Filled Button - Primary action button with accent color.
 */
@Composable
fun LiquidFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    tint: Color = AccentPrimary.copy(alpha = 0.4f)
) {
    LiquidButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        tint = tint
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) TextPrimary else TextTertiary
        )
    }
}
