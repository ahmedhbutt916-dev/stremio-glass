package com.stremio.glass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.model.Stream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamUrl: String = "",
    val streamTitle: String = "",
    val streamName: String = "",
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadStream(stream: Stream) {
        val url = when {
            stream.url.isNotEmpty() -> stream.url
            stream.ytId.isNotEmpty() -> "https://www.youtube.com/watch?v=${stream.ytId}"
            stream.externalUrl.isNotEmpty() -> stream.externalUrl
            stream.infoHash.isNotEmpty() -> {
                // Build magnet link from info hash
                val magnet = "magnet:?xt=urn:btih:${stream.infoHash}"
                if (stream.sources.isNotEmpty()) {
                    magnet + "&" + stream.sources.joinToString("&") { "tr=$it" }
                } else magnet
            }
            else -> ""
        }

        _uiState.update {
            it.copy(
                streamUrl = url,
                streamTitle = stream.title,
                streamName = stream.name,
                isPlaying = url.isNotEmpty(),
                error = if (url.isEmpty()) "No playable URL found" else null
            )
        }
    }

    fun updateProgress(position: Long, duration: Long) {
        _uiState.update {
            it.copy(currentPosition = position, duration = duration)
        }
    }

    fun setError(error: String) {
        _uiState.update { it.copy(error = error, isPlaying = false) }
    }
}
