package com.nhbox.app.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Category(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val isEnabled: Boolean = true,
    val imageId: String = "",
    val imageUrl: String = ""
)

@IgnoreExtraProperties
data class Genre(
    val id: String = "",
    val name: String = "",
    val isEnabled: Boolean = true
)

@IgnoreExtraProperties
data class Banner(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val imageId: String = "",
    val targetId: String = "",
    val targetType: String = "movie", // "movie" or "series"
    val order: Int = 0,
    val isEnabled: Boolean = true
) {
    fun resolvedImage(): String = when {
        imageId.isNotBlank() -> "rtdb://$imageId"
        else -> imageUrl
    }
}

@IgnoreExtraProperties
data class WatchlistItem(
    val contentId: String = "",
    val contentType: String = "movie", // "movie" or "series"
    val title: String = "",
    val posterUrl: String = "",
    val posterImageId: String = "",
    val releaseYear: String = "",
    val rating: Double = 0.0,
    val addedAt: Long = 0L
) {
    fun resolvedPoster(): String = if (posterImageId.isNotBlank()) "rtdb://$posterImageId" else posterUrl
}

@IgnoreExtraProperties
data class WatchHistoryItem(
    val contentId: String = "",
    val contentType: String = "movie",
    val title: String = "",
    val posterUrl: String = "",
    val posterImageId: String = "",
    val episodeId: String = "",
    val seasonId: String = "",
    val episodeTitle: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun resolvedPoster(): String = if (posterImageId.isNotBlank()) "rtdb://$posterImageId" else posterUrl
}

@IgnoreExtraProperties
data class DownloadRecord(
    val downloadId: String = "",
    val systemDownloadId: Long = -1L,
    val contentId: String = "",
    val contentType: String = "movie",
    val title: String = "",
    val posterUrl: String = "",
    val posterImageId: String = "",
    val videoUrl: String = "",
    val localFilePath: String = "",
    val quality: String = "1080p",
    val status: String = "PENDING", // PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
    val progress: Int = 0,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val createdAt: Long = 0L
) {
    fun resolvedPoster(): String = if (posterImageId.isNotBlank()) "rtdb://$posterImageId" else posterUrl
}

@IgnoreExtraProperties
data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val imageId: String = "",
    val targetScreen: String = "home",
    val targetContentId: String = "",
    val targetContentType: String = "movie",
    val createdAt: Long = 0L,
    val isRead: Boolean = false
)

@IgnoreExtraProperties
data class RtdbImage(
    val id: String = "",
    val category: String = "POSTER", // POSTER, BACKDROP, THUMBNAIL, LOGO, BANNER, PROFILE, CATEGORY
    val base64Data: String = "",
    val mimeType: String = "image/webp",
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Int = 0,
    val sha256Hash: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L
)

@IgnoreExtraProperties
data class AdminActivityLog(
    val id: String = "",
    val adminUid: String = "",
    val adminName: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = 0L
)

@IgnoreExtraProperties
data class AppSettings(
    val appName: String = "NH BOX",
    val maintenanceMode: Boolean = false,
    val maxImageSizeKb: Int = 180,
    val tmdbEnabled: Boolean = true,
    val supportEmail: String = "support@nhbox.app",
    val privacyPolicyUrl: String = "https://nhbox.app/privacy",
    val termsUrl: String = "https://nhbox.app/terms"
)

data class AdminDashboardStats(
    val totalMovies: Int = 0,
    val totalSeries: Int = 0,
    val totalCategories: Int = 0,
    val totalUsers: Int = 0,
    val onlineUsers: Int = 0,
    val totalViews: Long = 0L
)
