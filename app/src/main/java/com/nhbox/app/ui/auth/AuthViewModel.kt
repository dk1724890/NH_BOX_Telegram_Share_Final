package com.nhbox.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.UserProfile
import com.nhbox.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = authRepository.observeCurrentUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isSignedIn: Boolean get() = authRepository.currentUser != null

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.signIn(email, password)
                .onSuccess {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Sign in failed")
                }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.value = AuthUiState(error = "Enter username, valid email, and 6+ char password")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.register(username, email, password)
                .onSuccess {
                    _uiState.value = AuthUiState()
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Registration failed")
                }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(error = "Enter your email address first")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = AuthUiState(message = "Password reset link sent to $email")
                }
                .onFailure { e ->
                    _uiState.value = AuthUiState(error = e.message ?: "Failed to send reset link")
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, message = null)
    }
}
