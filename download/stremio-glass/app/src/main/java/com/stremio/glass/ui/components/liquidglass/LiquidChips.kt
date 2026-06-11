package com.stremio.glass.ui.components.liquidglass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stremio.glass.data.model.Stream
import com.stremio.glass.ui.theme.*

/**
 * Liquid Glass chip for displaying stream source info.
 */
@Composable
fun LiquidStreamChip(
    stream: Stream,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qualityLabel = when {
        stream.name.contains("4K", ignoreCase = true) -> "4K"
        stream.name.contains("1080", ignoreCase = true) -> "FHD"
        stream.name.contains("720", ignoreCase = true) -> "HD"
        stream.name.contains("480", ignoreCase = true) -> "SD"
        stream.name.contains("CAM", ignoreCase = true) -> "CAM"
        else -> "AUTO"
    }

    val tint = when (qualityLabel) {
        "4K" -> Stream4K.copy(alpha = 0.2f)
        "FHD" -> StreamHD.copy(alpha = 0.15f)
        "HD" -> StreamHD.copy(alpha = 0.1f)
        "CAM" -> StreamCAM.copy(alpha = 0.15f)
        else -> GlassTint
    }

    LiquidButton(
        onClick = onClick,
        modifier = modifier,
        tint = tint
    ) {
        androidx.compose.material3.Text(
            text = qualityLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
        if (stream.name.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            androidx.compose.material3.Text(
                text = stream.name,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Liquid Glass chip for genre/tag display.
 */
@Composable
fun LiquidChip(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    tint: Color = GlassTint.copy(alpha = 0.5f),
    selected: Boolean = false
) {
    val chipTint = if (selected) AccentPrimary.copy(alpha = 0.2f) else tint

    if (onClick != null) {
        LiquidButton(
            onClick = onClick,
            modifier = modifier,
            tint = chipTint
        ) {
            androidx.compose.material3.Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) AccentPrimary else TextSecondary
            )
        }
    } else {
        LiquidGlassSurface(
            modifier = modifier,
            cornerRadius = 8.dp,
            blurRadius = 4.dp,
            tintColor = chipTint
        ) {
            androidx.compose.material3.Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) AccentPrimary else TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
