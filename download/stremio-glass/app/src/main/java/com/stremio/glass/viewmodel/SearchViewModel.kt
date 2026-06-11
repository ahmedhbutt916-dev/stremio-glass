package com.stremio.glass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.model.MetaItem
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<MetaItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: StremioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadRecentSearches()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun performSearch(query: String = _uiState.value.query) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }

            val result = repository.search(query.trim())
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        results = result.getOrThrow(),
                        isSearching = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = result.exceptionOrNull()?.message ?: "Search failed",
                        isSearching = false
                    )
                }
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            repository.getSearchHistory().collectLatest { history ->
                _uiState.update { it.copy(recentSearches = history.map { h -> h.query }) }
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
