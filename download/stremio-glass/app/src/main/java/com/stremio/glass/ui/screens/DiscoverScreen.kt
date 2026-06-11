package com.stremio.glass.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stremio.glass.data.model.MetaItem
import com.stremio.glass.ui.components.liquidglass.*
import com.stremio.glass.ui.theme.*
import com.stremio.glass.viewmodel.HomeViewModel

@Composable
fun DiscoverScreen(
    onMetaClick: (type: String, id: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedGenre by remember { mutableStateOf<String?>(null) }
    val genres = remember {
        listOf("Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Thriller", "Romance", "Animation", "Documentary", "Crime")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Genre chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                LiquidChip(
                    text = genre,
                    selected = selectedGenre == genre,
                    onClick = { selectedGenre = if (selectedGenre == genre) null else genre }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Content filtered by genre
        val filteredItems = if (selectedGenre != null) {
            uiState.trending.filter { it.genre.any { g -> g.equals(selectedGenre, ignoreCase = true) } }
        } else {
            uiState.trending
        }

        if (filteredItems.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No content found for this genre",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Grid layout using rows
        val chunked = filteredItems.chunked(3)
        for (row in chunked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (item in row) {
                    Box(modifier = Modifier.weight(1f)) {
                        LiquidPosterCard(
                            title = item.name,
                            posterUrl = item.poster,
                            onClick = { onMetaClick(item.type, item.id) },
                            subtitle = item.releaseInfo,
                            rating = item.imdbRating.ifEmpty { item.rating },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                // Fill remaining space if row isn't full
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        }
    }
}
