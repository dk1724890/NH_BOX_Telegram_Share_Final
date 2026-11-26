package com.nhbox.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.UserPreferences
import com.nhbox.app.data.model.UserProfile
import com.nhbox.app.data.model.WatchHistoryItem
import com.nhbox.app.data.repository.AuthRepository
import com.nhbox.app.data.repository.ImageRtdbRepository
import com.nhbox.app.data.repository.UserLibraryRepository
import com.nhbox.app.utils.image.compression.ImageProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageRtdbRepository: ImageRtdbRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = authRepository.observeCurrentUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val watchHistory: StateFlow<List<WatchHistoryItem>> = MutableStateFlow<List<WatchHistoryItem>>(emptyList()).also { flow ->
        viewModelScope.launch {
            userLibraryRepository.observeWatchHistory().collect { res ->
                flow.value = res.getOrNull().orEmpty()
            }
        }
    }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val current = userProfile.value?.preferences ?: UserPreferences()
        val updated = transform(current)
        viewModelScope.launch {
            authRepository.updatePreferences(updated)
                .onSuccess { _message.value = "Settings saved" }
                .onFailure { _message.value = it.message ?: "Failed to save settings" }
        }
    }

    fun updateProfileName(newUsername: String) {
        if (newUsername.isBlank()) return
        viewModelScope.launch {
            authRepository.updateProfile(newUsername)
                .onSuccess { _message.value = "Profile updated" }
                .onFailure { _message.value = it.message ?: "Failed to update profile" }
        }
    }

    fun uploadProfileAvatar(uri: Uri) {
        val currentName = userProfile.value?.username ?: "User"
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            imageRtdbRepository.uploadCompressedImage(uri, ImageProfile.PROFILE)
                .onSuccess { imageId ->
                    authRepository.updateProfile(currentName, imageId)
                    _message.value = "Profile photo updated"
                }
                .onFailure { e ->
                    _message.value = e.message ?: "Photo upload failed"
                }
            _isUploadingAvatar.value = false
        }
    }

    fun changePassword(currentPwd: String, newPwd: String) {
        if (newPwd.length < 6) {
            _message.value = "New password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            authRepository.changePassword(currentPwd, newPwd)
                .onSuccess { _message.value = "Password updated successfully" }
                .onFailure { _message.value = it.message ?: "Password update failed" }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onLoggedOut()
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
