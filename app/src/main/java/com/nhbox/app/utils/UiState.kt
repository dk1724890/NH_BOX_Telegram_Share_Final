package com.nhbox.app.utils

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String = "No content available") : UiState<Nothing>()
    data class NetworkError(val message: String = "No internet connection") : UiState<Nothing>()
    data class DatabaseError(val message: String = "Failed to load data from server") : UiState<Nothing>()
}
