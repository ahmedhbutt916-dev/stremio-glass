package com.stremio.glass.ui.components.liquidglass

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.stremio.glass.ui.theme.*

/**
 * Liquid Glass Toggle - A frosted glass toggle switch with spring physics animation.
 * Follows the Kyant0 LiquidToggle design: track capsule with thumb that slides
 * between on/off states, with blur/lens effects that respond to press state.
 */
@Composable
fun LiquidToggle(
    selected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        )
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )

    val thumbScaleX by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )

    val trackWidth = 64.dp
    val trackHeight = 28.dp
    val thumbSize = 24.dp
    val thumbTravel = 36.dp // distance the thumb moves

    val trackTintColor = lerp(
        GlassTint,
        AccentPrimary.copy(alpha = 0.3f),
        progress
    )

    // Track
    LiquidGlassSurface(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { onToggle(!selected) }
            ),
        cornerRadius = 14.dp,
        blurRadius = 6.dp * (1f - progress * 0.3f),
        tintColor = trackTintColor,
        pressed = isPressed
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
        ) {
            LiquidGlassSurface(
                modifier = Modifier
                    .offset(x = thumbTravel * progress)
                    .size(thumbSize)
                    .graphicsLayer {
                        scaleX = thumbScaleX * pressScale
                        scaleY = pressScale
                    },
                cornerRadius = 12.dp,
                blurRadius = 4.dp * (1f - progress * 0.5f),
                tintColor = if (selected) AccentPrimary.copy(alpha = 0.5f) else GlassTint.copy(alpha = 0.8f),
                pressed = isPressed
            ) {}
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
