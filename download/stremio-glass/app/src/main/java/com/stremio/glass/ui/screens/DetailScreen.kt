package com.stremio.glass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stremio.glass.data.model.MetaItem
import com.stremio.glass.data.model.Stream
import com.stremio.glass.data.model.Video
import com.stremio.glass.ui.components.liquidglass.*
import com.stremio.glass.ui.theme.*
import com.stremio.glass.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onPlayStream: (Stream) -> Unit,
    onPlayEpisode: (metaType: String, metaId: String, videoId: String, videoTitle: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
        return
    }

    if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Could not load details",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                LiquidFilledButton(onClick = { viewModel.loadMeta() }, text = "Retry")
            }
        }
        return
    }

    val meta = uiState.meta ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = meta.background.ifEmpty { meta.poster },
                contentDescription = meta.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            LiquidGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter),
                cornerRadius = 0.dp,
                blurRadius = 2.dp,
                tintColor = Color.Black.copy(alpha = 0.5f),
                borderColor = Color.Transparent
            ) {}

            // Back button
            LiquidIconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                tint = Color.Black.copy(alpha = 0.3f)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meta.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (meta.releaseInfo.isNotEmpty() || meta.type.isNotEmpty()) {
                        Text(
                            text = buildString {
                                append(meta.type.replaceFirstChar { it.uppercase() })
                                if (meta.releaseInfo.isNotEmpty()) append(" ${meta.releaseInfo}")
                                if (meta.runtime.isNotEmpty()) append(" ${meta.runtime}")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Bookmark button
                LiquidIconButton(
                    onClick = { viewModel.toggleLibrary() },
                    tint = if (uiState.isInLibrary) AccentPrimary.copy(alpha = 0.2f) else GlassTint
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (uiState.isInLibrary) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (uiState.isInLibrary) AccentPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating and year chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (meta.imdbRating.isNotEmpty() || meta.rating.isNotEmpty()) {
                    item {
                        LiquidChip(
                            text = "IMDb ${meta.imdbRating.ifEmpty { meta.rating }}",
                            tint = Color(0xFFFFC107).copy(alpha = 0.15f)
                        )
                    }
                }
                items(meta.genre.take(4)) { genre ->
                    item {
                        LiquidChip(text = genre)
                    }
                }
                if (meta.year.isNotEmpty()) {
                    item {
                        LiquidChip(text = meta.year)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            if (meta.description.isNotEmpty()) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Cast
            if (meta.cast.isNotEmpty()) {
                Text(
                    text = "Cast",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta.cast.take(8).joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Director
            if (meta.director.isNotEmpty()) {
                Text(
                    text = "Director",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta.director.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Episodes (for series)
            if (meta.videos.isNotEmpty()) {
                val seasons = meta.videos.mapNotNull { it.season }.distinct().sorted()
                val selectedSeason = uiState.selectedSeason ?: seasons.firstOrNull()
                val episodes = meta.videos.filter { it.season == selectedSeason }

                Text(
                    text = "Episodes",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Season selector
                if (seasons.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(seasons) { season ->
                            LiquidChip(
                                text = "Season $season",
                                selected = season == selectedSeason,
                                onClick = { viewModel.selectSeason(season) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Episode list - wired up with play functionality
                for (episode in episodes) {
                    LiquidGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        cornerRadius = 12.dp,
                        blurRadius = 3.dp,
                        tintColor = GlassTint.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "E${episode.episode} ${episode.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                if (episode.overview.isNotEmpty()) {
                                    Text(
                                        text = episode.overview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 2
                                    )
                                }
                            }
                            // Play button - navigates to player and fetches streams in background
                            LiquidIconButton(
                                onClick = {
                                    onPlayEpisode(
                                        meta.type,
                                        meta.id,
                                        episode.id,
                                        "S${episode.season}E${episode.episode} ${episode.title}"
                                    )
                                }
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = AccentPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Streams
            Text(
                text = "Streams",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoadingStreams) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = AccentPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading streams...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            } else if (uiState.streams.isEmpty()) {
                Text(
                    text = "No streams available. Install streaming addons.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.streams) { stream ->
                        LiquidStreamChip(
                            stream = stream,
                            onClick = { onPlayStream(stream) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
