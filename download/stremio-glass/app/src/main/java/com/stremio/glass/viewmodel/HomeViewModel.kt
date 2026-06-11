package com.stremio.glass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.local.LibraryItemEntity
import com.stremio.glass.data.model.*
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val featured: List<MetaItem> = emptyList(),
    val trending: List<MetaItem> = emptyList(),
    val popular: List<MetaItem> = emptyList(),
    val topRated: List<MetaItem> = emptyList(),
    val recentlyAdded: List<MetaItem> = emptyList(),
    val continueWatching: List<LibraryItemEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StremioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
        loadContinueWatching()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Get installed addons and fetch catalogs
            repository.getInstalledAddons().collectLatest { addons ->
                val enabledAddons = addons.filter { it.enabled }
                if (enabledAddons.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No addons installed. Install addons to see content.") }
                    return@collectLatest
                }

                val allItems = mutableListOf<MetaItem>()
                val seenIds = mutableSetOf<String>()

                for (addon in enabledAddons) {
                    for (catalog in addon.manifest.catalogs) {
                        try {
                            val result = repository.getCatalog(
                                addon.manifestUrl, catalog.type, catalog.id
                            )
                            if (result.isSuccess) {
                                result.getOrThrow().forEach { item ->
                                    if (item.id !in seenIds) {
                                        seenIds.add(item.id)
                                        allItems.add(item)
                                    }
                                }
                            }
                        } catch (e: Exception) { /* skip */ }
                    }
                }

                // Shuffle and distribute into categories
                val shuffled = allItems.shuffled()
                val featured = shuffled.take(5)
                val trending = shuffled.take(20)
                val popular = shuffled.drop(20).take(20)
                val topRated = allItems
                    .sortedByDescending { it.imdbRating.ifEmpty { it.rating } }
                    .take(20)
                val recentlyAdded = shuffled.drop(40).take(20)

                _uiState.update {
                    it.copy(
                        featured = featured,
                        trending = trending,
                        popular = popular,
                        topRated = topRated,
                        recentlyAdded = recentlyAdded,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadContinueWatching() {
        viewModelScope.launch {
            repository.getLibrary().collectLatest { items ->
                val watching = items.filter { it.watchProgress > 0f && it.watchProgress < 0.95f }
                    .sortedByDescending { it.lastWatchedAt }
                _uiState.update { it.copy(continueWatching = watching) }
            }
        }
    }

    fun refresh() = loadHomeContent()
}
