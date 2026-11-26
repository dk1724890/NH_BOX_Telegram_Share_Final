package com.nhbox.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.nhbox.app.data.model.AppNotification
import com.nhbox.app.data.model.WatchHistoryItem
import com.nhbox.app.data.model.WatchlistItem
import com.nhbox.app.data.remote.FirebaseRtdbService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLibraryRepository @Inject constructor(
    private val rtdb: FirebaseRtdbService,
    private val auth: FirebaseAuth
) {
    private val uid: String? get() = auth.currentUser?.uid

    fun observeWatchlist(): Flow<Result<List<WatchlistItem>>> {
        val userId = uid ?: return flowOf(Result.success(emptyList()))
        val query = rtdb.ref("watchlist/$userId").orderByChild("addedAt")
        return rtdb.observeList(query, WatchlistItem::class.java).map { res ->
            res.map { it.sortedByDescending { item -> item.addedAt } }
        }
    }

    suspend fun isInWatchlist(contentId: String): Boolean {
        val userId = uid ?: return false
        return rtdb.getValueOnce("watchlist/$userId/$contentId", WatchlistItem::class.java)
            .getOrNull() != null
    }

    suspend fun addToWatchlist(item: WatchlistItem): Result<Unit> {
        val userId = uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return rtdb.setValue("watchlist/$userId/${item.contentId}", item)
    }

    suspend fun removeFromWatchlist(contentId: String): Result<Unit> {
        val userId = uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return rtdb.removeValue("watchlist/$userId/$contentId")
    }

    fun observeWatchHistory(): Flow<Result<List<WatchHistoryItem>>> {
        val userId = uid ?: return flowOf(Result.success(emptyList()))
        val query = rtdb.ref("watchHistory/$userId").orderByChild("updatedAt")
        return rtdb.observeList(query, WatchHistoryItem::class.java).map { res ->
            res.map { it.sortedByDescending { item -> item.updatedAt } }
        }
    }

    suspend fun getWatchProgress(contentId: String): WatchHistoryItem? {
        val userId = uid ?: return null
        return rtdb.getValueOnce("watchHistory/$userId/$contentId", WatchHistoryItem::class.java)
            .getOrNull()
    }

    suspend fun saveWatchProgress(item: WatchHistoryItem): Result<Unit> {
        val userId = uid ?: return Result.failure(IllegalStateException("Not signed in"))
        return rtdb.setValue("watchHistory/$userId/${item.contentId}", item)
    }

    fun observeNotifications(): Flow<Result<List<AppNotification>>> {
        val query = rtdb.ref("notifications/broadcast")
        return rtdb.observeList(query, AppNotification::class.java).map { res ->
            res.map { it.sortedByDescending { n -> n.createdAt } }
        }
    }
}
