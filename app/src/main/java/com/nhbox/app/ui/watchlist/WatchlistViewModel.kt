package com.nhbox.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.WatchlistItem
import com.nhbox.app.data.repository.UserLibraryRepository
import com.nhbox.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<WatchlistItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<WatchlistItem>>> = _uiState.asStateFlow()

    init {
        loadWatchlist()
    }

    fun loadWatchlist() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            userLibraryRepository.observeWatchlist().collect { res ->
                res.onSuccess { list ->
                    _uiState.value = if (list.isEmpty()) {
                        UiState.Empty("Your watchlist is empty. Bookmark movies or series to save them here.")
                    } else {
                        UiState.Success(list)
                    }
                }.onFailure { e ->
                    _uiState.value = UiState.DatabaseError(e.message ?: "Failed to load watchlist")
                }
            }
        }
    }

    fun remove(contentId: String) {
        viewModelScope.launch {
            userLibraryRepository.removeFromWatchlist(contentId)
        }
    }
}
