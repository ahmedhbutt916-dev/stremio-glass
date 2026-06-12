package com.stremio.glass.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremio.glass.data.local.LibraryItemEntity
import com.stremio.glass.data.model.MetaItem
import com.stremio.glass.ui.components.liquidglass.*
import com.stremio.glass.ui.theme.*
import com.stremio.glass.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onMetaClick: (type: String, id: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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

    if (uiState.error != null && uiState.featured.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                LiquidFilledButton(onClick = { viewModel.refresh() }, text = "Retry")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Header
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Stremio Glass",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your streaming hub",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Continue Watching
        if (uiState.continueWatching.isNotEmpty()) {
            SectionHeader(title = "Continue Watching")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.continueWatching) { item ->
                    ContinueWatchingCard(item = item, onClick = {
                        onMetaClick(item.type, item.id)
                    })
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Featured
        if (uiState.featured.isNotEmpty()) {
            SectionHeader(title = "Featured")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.featured) { item ->
                    LiquidPosterCard(
                        title = item.name,
                        posterUrl = item.poster,
                        onClick = { onMetaClick(item.type, item.id) },
                        subtitle = item.releaseInfo,
                        rating = item.imdbRating.ifEmpty { item.rating }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Trending
        if (uiState.trending.isNotEmpty()) {
            SectionHeader(title = "Trending")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.trending) { item ->
                    LiquidPosterCard(
                        title = item.name,
                        posterUrl = item.poster,
                        onClick = { onMetaClick(item.type, item.id) },
                        subtitle = item.releaseInfo,
                        rating = item.imdbRating.ifEmpty { item.rating }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Popular
        if (uiState.popular.isNotEmpty()) {
            SectionHeader(title = "Popular")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.popular) { item ->
                    LiquidPosterCard(
                        title = item.name,
                        posterUrl = item.poster,
                        onClick = { onMetaClick(item.type, item.id) },
                        subtitle = item.releaseInfo,
                        rating = item.imdbRating.ifEmpty { item.rating }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Top Rated
        if (uiState.topRated.isNotEmpty()) {
            SectionHeader(title = "Top Rated")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.topRated) { item ->
                    LiquidPosterCard(
                        title = item.name,
                        posterUrl = item.poster,
                        onClick = { onMetaClick(item.type, item.id) },
                        subtitle = item.releaseInfo,
                        rating = item.imdbRating.ifEmpty { item.rating }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recently Added
        if (uiState.recentlyAdded.isNotEmpty()) {
            SectionHeader(title = "Recently Added")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentlyAdded) { item ->
                    LiquidPosterCard(
                        title = item.name,
                        posterUrl = item.poster,
                        onClick = { onMetaClick(item.type, item.id) },
                        subtitle = item.releaseInfo,
                        rating = item.imdbRating.ifEmpty { item.rating }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun ContinueWatchingCard(item: LibraryItemEntity, onClick: () -> Unit) {
    LiquidGlassSurface(
        modifier = Modifier
            .width(160.dp)
            .height(90.dp),
        cornerRadius = 12.dp,
        blurRadius = 4.dp,
        tintColor = GlassTint.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1
            )
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.watchProgress)
                        .fillMaxHeight()
                        .background(AccentPrimary, MaterialTheme.shapes.extraSmall)
                )
            }
        }
    }
}
