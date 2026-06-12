package com.stremio.glass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stremio.glass.ui.components.liquidglass.*
import com.stremio.glass.ui.theme.*

@Composable
fun SettingsScreen() {
    var autoPlay by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    var subtitleEnabled by remember { mutableStateOf(true) }
    var preferredQuality by remember { mutableIntStateOf(0) }
    val qualities = listOf("Auto", "4K", "1080p", "720p", "480p")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Playback section
        SettingsSectionHeader(title = "Playback")
        SettingsToggleItem(
            title = "Auto-play Next Episode",
            description = "Automatically play the next episode when current one ends",
            checked = autoPlay,
            onToggle = { autoPlay = it }
        )
        SettingsToggleItem(
            title = "Subtitles",
            description = "Enable subtitles by default",
            checked = subtitleEnabled,
            onToggle = { subtitleEnabled = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quality section
        SettingsSectionHeader(title = "Stream Quality")
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qualities.forEachIndexed { index, quality ->
                    LiquidChip(
                        text = quality,
                        selected = preferredQuality == index,
                        onClick = { preferredQuality = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Appearance section
        SettingsSectionHeader(title = "Appearance")
        SettingsToggleItem(
            title = "Dark Mode",
            description = "Use dark theme (recommended for Liquid Glass effects)",
            checked = darkMode,
            onToggle = { darkMode = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // About section
        SettingsSectionHeader(title = "About")
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            SettingInfoRow(label = "App", value = "Stremio Glass")
            SettingInfoRow(label = "Version", value = "1.0.0")
            SettingInfoRow(label = "Engine", value = "Liquid Glass")
            SettingInfoRow(label = "Protocol", value = "Stremio Addon v3")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cache section
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            LiquidFilledButton(
                onClick = { /* clear cache */ },
                text = "Clear Cache",
                tint = ErrorRed.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            LiquidToggle(
                selected = checked,
                onToggle = onToggle
            )
        }
    }
}

@Composable
private fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
