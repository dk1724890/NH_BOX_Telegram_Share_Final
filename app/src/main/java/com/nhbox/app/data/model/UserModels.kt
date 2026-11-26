package com.nhbox.app.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserPreferences(
    val darkTheme: Boolean = true,
    val language: String = "English",
    val autoPlay: Boolean = true,
    val videoQuality: String = "Auto",
    val defaultStreamingQuality: String = "1080p",
    val autoDownloadWifiOnly: Boolean = true,
    val preferredAudioLanguage: String = "Hindi",
    val subtitlesEnabled: Boolean = true,
    val subtitleLanguage: String = "English",
    val autoNextEpisode: Boolean = true
)

@IgnoreExtraProperties
data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "user", // "user" or "admin"
    val accountStatus: String = "active", // "active", "suspended", "banned"
    val isOnline: Boolean = false,
    val profileImageId: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = 0L,
    val lastActive: Long = 0L,
    val preferences: UserPreferences = UserPreferences()
) {
    @get:Exclude
    val isAdmin: Boolean get() = role == "admin"

    @get:Exclude
    val isActive: Boolean get() = accountStatus == "active"
}
