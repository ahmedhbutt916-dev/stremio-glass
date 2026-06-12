package com.stremio.glass.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.model.Stream
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamUrl: String = "",
    val streamTitle: String = "",
    val streamName: String = "",
    val isResolving: Boolean = false,
    val resolvingMessage: String = "",
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null,
    val subtitles: List<SubtitleTrack> = emptyList()
)

data class SubtitleTrack(
    val url: String,
    val label: String,
    val language: String
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: StremioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /**
     * Load a direct stream (from stream chip click).
     * If the stream has an HTTP URL, play immediately.
     * Otherwise, try to resolve by fetching alternative streams.
     */
    fun loadStream(stream: Stream, metaType: String = "", metaId: String = "") {
        val title = stream.title.ifEmpty { stream.name }

        if (stream.url.isNotEmpty()) {
            // Direct HTTP URL - play immediately with ExoPlayer
            Log.d("PlayerVM", "Loading direct URL stream: ${stream.url.take(80)}")
            _uiState.update {
                it.copy(
                    streamUrl = stream.url,
                    streamTitle = title,
                    streamName = stream.name,
                    isResolving = false,
                    isPlaying = true,
                    error = null
                )
            }
        } else if (metaType.isNotEmpty() && metaId.isNotEmpty()) {
            // No direct URL but we know the content - try fetching alternative streams
            Log.d("PlayerVM", "Stream has no direct URL, resolving via addon search for $metaType/$metaId")
            _uiState.update {
                it.copy(
                    streamTitle = title,
                    streamName = stream.name,
                    isResolving = true,
                    resolvingMessage = "Looking for direct streams...",
                    error = null
                )
            }
            viewModelScope.launch {
                resolveAndPlay(metaType, stream.infoHash.ifEmpty { metaId })
            }
        } else {
            // No way to resolve - show appropriate error
            val errorMsg = getStreamErrorMessage(stream)
            Log.w("PlayerVM", "Cannot play stream: $errorMsg")
            _uiState.update {
                it.copy(isResolving = false, error = errorMsg)
            }
        }
    }

    /**
     * Load an episode by fetching streams in the background.
     * Player shows "Finding streams..." while resolving.
     */
    fun loadEpisode(metaType: String, metaId: String, videoId: String, title: String) {
        Log.d("PlayerVM", "Loading episode: $metaType/$videoId - $title")
        _uiState.update {
            it.copy(
                streamTitle = title,
                isResolving = true,
                resolvingMessage = "Finding streams...",
                error = null,
                streamUrl = "",
                isPlaying = false
            )
        }
        viewModelScope.launch {
            resolveAndPlay(metaType, videoId)
        }
    }

    /**
     * Load a direct URL for immediate playback (e.g., from a stream chip with a URL).
     */
    fun loadDirectUrl(url: String, title: String) {
        Log.d("PlayerVM", "Loading direct URL: ${url.take(80)}")
        _uiState.update {
            it.copy(
                streamUrl = url,
                streamTitle = title,
                isResolving = false,
                isPlaying = true,
                error = null
            )
        }
    }

    /**
     * Resolve streams for the given content and pick the best one to play.
     * Prioritizes HTTP URL streams that ExoPlayer can handle directly.
     */
    private suspend fun resolveAndPlay(type: String, id: String) {
        try {
            val result = repository.getStreamsParallel(type, id)
            if (result.isSuccess) {
                val streams = result.getOrThrow()
                Log.d("PlayerVM", "Found ${streams.size} streams for $type/$id")

                // Prioritize HTTP URL streams (ExoPlayer can play these directly)
                val httpStream = streams.firstOrNull { it.url.isNotEmpty() }

                if (httpStream != null) {
                    Log.d("PlayerVM", "Playing HTTP stream: ${httpStream.url.take(80)}")
                    _uiState.update {
                        it.copy(
                            streamUrl = httpStream.url,
                            streamName = httpStream.name,
                            isResolving = false,
                            isPlaying = true
                        )
                    }
                } else if (streams.any { it.infoHash.isNotEmpty() }) {
                    // Only torrent streams available
                    Log.w("PlayerVM", "Only torrent streams available for $type/$id")
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            error = "Only torrent streams found. Install a direct streaming addon (like Embedrise) for instant playback."
                        )
                    }
                } else if (streams.any { it.ytId.isNotEmpty() }) {
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            error = "Only YouTube streams available. YouTube playback is not supported in-app yet."
                        )
                    }
                } else {
                    Log.w("PlayerVM", "No streams found for $type/$id")
                    _uiState.update {
                        it.copy(
                            isResolving = false,
                            error = "No streams found for this content. Try installing more streaming addons."
                        )
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e("PlayerVM", "Stream fetch failed: $errorMsg")
                _uiState.update {
                    it.copy(
                        isResolving = false,
                        error = "Failed to fetch streams: $errorMsg"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PlayerVM", "Error resolving streams", e)
            _uiState.update {
                it.copy(
                    isResolving = false,
                    error = "Error finding streams: ${e.message}"
                )
            }
        }
    }

    fun updateProgress(position: Long, duration: Long) {
        _uiState.update {
            it.copy(currentPosition = position, duration = duration)
        }
    }

    fun setError(error: String) {
        _uiState.update { it.copy(error = error, isPlaying = false, isResolving = false) }
    }

    private fun getStreamErrorMessage(stream: Stream): String {
        return when {
            stream.infoHash.isNotEmpty() -> "This is a torrent stream. Install a direct streaming addon for instant playback, or try a different source."
            stream.ytId.isNotEmpty() -> "YouTube playback is not supported in-app yet"
            stream.externalUrl.isNotEmpty() -> "External links cannot be played in the built-in player"
            else -> "No playable URL found in this stream"
        }
    }
}
