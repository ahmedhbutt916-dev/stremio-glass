package com.stremio.glass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stremio.glass.data.local.LibraryItemEntity
import com.stremio.glass.data.repository.StremioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val items: List<LibraryItemEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: StremioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            repository.getLibrary().collectLatest { items ->
                _uiState.update {
                    it.copy(items = items, isLoading = false)
                }
            }
        }
    }

    fun removeFromLibrary(id: String) {
        viewModelScope.launch {
            repository.removeFromLibrary(id)
        }
    }
}
