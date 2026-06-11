package com.stremio.glass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.model.Addon
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddonsUiState(
    val installedAddons: List<Addon> = emptyList(),
    val communityAddons: List<Addon> = emptyList(),
    val searchQuery: String = "",
    val isInstalling: Boolean = false,
    val installError: String? = null,
    val installSuccess: Boolean = false,
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class AddonsViewModel @Inject constructor(
    private val repository: StremioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddonsUiState())
    val uiState: StateFlow<AddonsUiState> = _uiState.asStateFlow()

    // Community addons catalog (well-known Stremio addons)
    private val communityAddonUrls = listOf(
        "https://v3-cinemeta.strem.io/manifest.json",
        "https://watchhub.strem.io/manifest.json",
        "https://opensubtitles.strem.io/manifest.json",
        "https://stremio-torrentio.strem.fun/manifest.json",
        "https://stremio-addon.debridlink.com/manifest.json",
        "https://addon.embedrise.com/manifest.json",
        "https://stremio-kitsu.strem.fun/manifest.json",
        "https://stremio-filmon.strem.fun/manifest.json",
        "https://stremio-openvideos.strem.fun/manifest.json",
        "https://stremio-youtube.strem.fun/manifest.json",
        "https://stremio-twitch.strem.fun/manifest.json",
        "https://stremio-reddit.strem.fun/manifest.json",
        "https://stremio-novelas.strem.fun/manifest.json",
        "https://stremio-channels.strem.fun/manifest.json",
        "https://juanma-tv.strem.fun/manifest.json"
    )

    init {
        loadInstalledAddons()
        loadCommunityAddons()
    }

    private fun loadInstalledAddons() {
        viewModelScope.launch {
            repository.getInstalledAddons().collectLatest { addons ->
                _uiState.update {
                    it.copy(installedAddons = addons, isLoading = false)
                }
            }
        }
    }

    private fun loadCommunityAddons() {
        viewModelScope.launch {
            val community = mutableListOf<Addon>()
            for (url in communityAddonUrls) {
                try {
                    val result = repository.installAddon(url)
                    if (result.isSuccess) {
                        community.add(result.getOrThrow().copy(installed = false))
                    }
                } catch (e: Exception) { /* skip */ }
            }
            _uiState.update { it.copy(communityAddons = community) }
        }
    }

    fun installAddon(manifestUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInstalling = true, installError = null) }

            val result = repository.installAddon(manifestUrl)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isInstalling = false, installSuccess = true, showAddDialog = false)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isInstalling = false,
                        installError = result.exceptionOrNull()?.message ?: "Installation failed"
                    )
                }
            }
        }
    }

    fun uninstallAddon(manifestUrl: String) {
        viewModelScope.launch {
            repository.uninstallAddon(manifestUrl)
        }
    }

    fun toggleAddon(manifestUrl: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAddon(manifestUrl, enabled)
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, installError = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false, installError = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
