package com.stremio.glass.ui.components.liquidglass

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stremio.glass.ui.theme.*

/**
 * Liquid Glass Card for media posters/items.
 * Uses glass overlay on top of poster image with press animation.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidPosterCard(
    title: String,
    posterUrl: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    subtitle: String = "",
    rating: String = "",
    badge: String? = null,
    cornerRadius: Dp = 12.dp
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 0.5f else 1f,
        animationSpec = tween(150)
    )

    Box(
        modifier = modifier
            .width(130.dp)
            .height(195.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = (4 * elevation).dp.toPx()
                shape = RoundedCornerShape(cornerRadius)
                clip = true
            }
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                },
                onLongClick = onLongClick
            )
    ) {
        // Poster image
        AsyncImage(
            model = posterUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Glass overlay gradient at bottom for title
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(androidx.compose.ui.Alignment.BottomCenter),
            cornerRadius = 0.dp,
            blurRadius = 2.dp,
            tintColor = Color.Black.copy(alpha = 0.5f),
            borderColor = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                androidx.compose.material3.Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2
                )
                if (subtitle.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Rating badge
        if (rating.isNotEmpty()) {
            LiquidGlassSurface(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .padding(6.dp),
                cornerRadius = 8.dp,
                blurRadius = 2.dp,
                tintColor = Color.Black.copy(alpha = 0.5f)
            ) {
                androidx.compose.material3.Text(
                    text = rating,
                    style = MaterialTheme.typography.labelSmall,
                    color = ImdbYellow,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Badge (e.g. "NEW", "4K")
        if (badge != null) {
            LiquidGlassSurface(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopStart)
                    .padding(6.dp),
                cornerRadius = 6.dp,
                blurRadius = 2.dp,
                tintColor = AccentPrimary.copy(alpha = 0.5f)
            ) {
                androidx.compose.material3.Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(120)
            isPressed = false
        }
    }
}

/**
 * Wide landscape card for featured/hero content.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidWideCard(
    title: String,
    backgroundImageUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    description: String = "",
    cornerRadius: Dp = 16.dp
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = RoundedCornerShape(cornerRadius)
                clip = true
            }
            .clip(RoundedCornerShape(cornerRadius))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
    ) {
        // Background image
        AsyncImage(
            model = backgroundImageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Full glass overlay
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(androidx.compose.ui.Alignment.BottomCenter),
            cornerRadius = 0.dp,
            blurRadius = 4.dp,
            tintColor = Color.Black.copy(alpha = 0.4f),
            borderColor = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                androidx.compose.material3.Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 1
                )
                if (subtitle.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                if (description.isNotEmpty()) {
                    androidx.compose.material3.Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 2
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(120)
            isPressed = false
        }
    }
}
