package com.stremio.glass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremio.glass.data.model.Addon
import com.stremio.glass.ui.components.liquidglass.*
import com.stremio.glass.ui.theme.*
import com.stremio.glass.viewmodel.AddonsViewModel

@Composable
fun AddonsScreen(
    viewModel: AddonsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var addonUrlInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Addons",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            LiquidIconButton(onClick = { viewModel.showAddDialog() }) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Addon",
                    tint = AccentPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add addon dialog
        if (uiState.showAddDialog) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Add Addon URL",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = addonUrlInput,
                    onValueChange = { addonUrlInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://example.com/manifest.json") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = AccentPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.hideAddDialog() }) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidFilledButton(
                        onClick = {
                            if (addonUrlInput.isNotBlank()) {
                                viewModel.installAddon(addonUrlInput.trim())
                                addonUrlInput = ""
                            }
                        },
                        text = if (uiState.isInstalling) "Installing..." else "Install",
                        enabled = addonUrlInput.isNotBlank() && !uiState.isInstalling
                    )
                }
                if (uiState.installError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.installError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Installed addons section
        Text(
            text = "Installed",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        if (uiState.installedAddons.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No addons installed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.installedAddons, key = { it.manifestUrl }) { addon ->
                AddonItem(
                    addon = addon,
                    isInstalled = true,
                    onToggle = { enabled -> viewModel.toggleAddon(addon.manifestUrl, enabled) },
                    onUninstall = { viewModel.uninstallAddon(addon.manifestUrl) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Community addons section
        Text(
            text = "Community",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.communityAddons.filter { community ->
                uiState.installedAddons.none { it.manifestUrl == community.manifestUrl }
            }, key = { it.manifestUrl }) { addon ->
                AddonItem(
                    addon = addon,
                    isInstalled = false,
                    onInstall = { viewModel.installAddon(addon.manifestUrl) }
                )
            }
        }
    }
}

@Composable
private fun AddonItem(
    addon: Addon,
    isInstalled: Boolean,
    onToggle: ((Boolean) -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    onInstall: (() -> Unit)? = null
) {
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        blurRadius = 4.dp,
        tintColor = if (isInstalled && addon.enabled) AccentPrimary.copy(alpha = 0.08f) else GlassTint.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Addon icon/logo
            if (addon.manifest.logo.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = addon.manifest.logo,
                    contentDescription = addon.manifest.name,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                LiquidGlassSurface(
                    modifier = Modifier.size(40.dp),
                    cornerRadius = 10.dp,
                    blurRadius = 2.dp,
                    tintColor = AccentPrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = addon.manifest.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = addon.manifest.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = addon.manifest.description.take(60),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = addon.manifest.types.joinToString(", "),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            if (isInstalled) {
                LiquidToggle(
                    selected = addon.enabled,
                    onToggle = { onToggle?.invoke(it) }
                )
            } else {
                LiquidIconButton(onClick = { onInstall?.invoke() }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Install",
                        tint = AccentPrimary
                    )
                }
            }
        }
    }
}
