package com.stremio.glass.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.model.MetaItem
import com.stremio.glass.data.model.Stream
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val meta: MetaItem? = null,
    val streams: List<Stream> = emptyList(),
    val isInLibrary: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingStreams: Boolean = false,
    val error: String? = null,
    val selectedSeason: Int? = null,
    val selectedVideoId: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: StremioRepository
) : ViewModel() {

    private val metaId: String = savedStateHandle["metaId"] ?: ""
    private val metaType: String = savedStateHandle["metaType"] ?: "movie"

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMeta()
    }

    fun loadMeta() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Use the more resilient meta fetching method
            val result = repository.getMetaFromAnyAddon(metaType, metaId)
            if (result.isSuccess) {
                val metaItem = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        meta = metaItem,
                        isLoading = false,
                        selectedSeason = metaItem.videos.firstOrNull()?.season,
                        error = null
                    )
                }
                loadStreams()
                checkLibrary()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Could not load details for this item"
                Log.w("DetailVM", "Meta load failed: $errorMsg")
                _uiState.update {
                    it.copy(isLoading = false, error = errorMsg)
                }
            }
        }
    }

    fun loadStreams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStreams = true) }

            val result = repository.getStreams(metaType, metaId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(streams = result.getOrThrow(), isLoadingStreams = false)
                }
            } else {
                Log.w("DetailVM", "Stream load failed: ${result.exceptionOrNull()?.message}")
                _uiState.update { it.copy(isLoadingStreams = false) }
            }
        }
    }

    private fun checkLibrary() {
        viewModelScope.launch {
            val inLibrary = repository.isInLibrary(metaId)
            _uiState.update { it.copy(isInLibrary = inLibrary) }
        }
    }

    fun toggleLibrary() {
        viewModelScope.launch {
            val current = _uiState.value
            val meta = current.meta ?: return@launch

            if (current.isInLibrary) {
                repository.removeFromLibrary(metaId)
                _uiState.update { it.copy(isInLibrary = false) }
            } else {
                repository.addToLibrary(meta)
                _uiState.update { it.copy(isInLibrary = true) }
            }
        }
    }

    fun selectSeason(season: Int?) {
        _uiState.update { it.copy(selectedSeason = season) }
    }

    fun selectVideo(videoId: String) {
        _uiState.update { it.copy(selectedVideoId = videoId) }
    }
}
