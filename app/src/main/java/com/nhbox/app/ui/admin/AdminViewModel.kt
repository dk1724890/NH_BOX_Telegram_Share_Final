package com.nhbox.app.ui.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.AdminActivityLog
import com.nhbox.app.data.model.AdminDashboardStats
import com.nhbox.app.data.model.AppNotification
import com.nhbox.app.data.model.AppSettings
import com.nhbox.app.data.model.Banner
import com.nhbox.app.data.model.Category
import com.nhbox.app.data.model.Episode
import com.nhbox.app.data.model.Genre
import com.nhbox.app.data.model.Movie
import com.nhbox.app.data.model.Season
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.data.model.UserProfile
import com.nhbox.app.data.repository.AdminRepository
import com.nhbox.app.data.repository.AuthRepository
import com.nhbox.app.data.repository.ContentRepository
import com.nhbox.app.data.repository.ImageRtdbRepository
import com.nhbox.app.data.repository.ImageUploadProgress
import com.nhbox.app.utils.image.compression.ImageProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val stats: AdminDashboardStats = AdminDashboardStats(),
    val movies: List<Movie> = emptyList(),
    val series: List<TvSeries> = emptyList(),
    val categories: List<Category> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val users: List<UserProfile> = emptyList(),
    val activityLogs: List<AdminActivityLog> = emptyList(),
    val appSettings: AppSettings = AppSettings()
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepo: AdminRepository,
    private val contentRepo: ContentRepository,
    private val imageRepo: ImageRtdbRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    val adminState: StateFlow<AdminState> = combine(
        adminRepo.observeAllMovies(),
        adminRepo.observeAllSeries(),
        adminRepo.observeAllCategories(),
        adminRepo.observeAllGenres(),
        adminRepo.observeAllBanners(),
        adminRepo.observeAllUsers(),
        adminRepo.observeRecentActivity(),
        adminRepo.observeAppSettings()
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val movies = (values[0] as Result<List<Movie>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val series = (values[1] as Result<List<TvSeries>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val categories = (values[2] as Result<List<Category>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val genres = (values[3] as Result<List<Genre>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val banners = (values[4] as Result<List<Banner>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val users = (values[5] as Result<List<UserProfile>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val logs = (values[6] as Result<List<AdminActivityLog>>).getOrNull().orEmpty()
        @Suppress("UNCHECKED_CAST")
        val settings = (values[7] as Result<AppSettings?>).getOrNull() ?: AppSettings()

        AdminState(
            stats = adminRepo.computeStats(movies, series, categories, users),
            movies = movies,
            series = series,
            categories = categories,
            genres = genres,
            banners = banners,
            users = users,
            activityLogs = logs,
            appSettings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminState())

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _uploadProgress = MutableStateFlow<ImageUploadProgress?>(null)
    val uploadProgress: StateFlow<ImageUploadProgress?> = _uploadProgress.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _seasons = MutableStateFlow<List<Season>>(emptyList())
    val seasons: StateFlow<List<Season>> = _seasons.asStateFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    fun clearMessage() { _statusMessage.value = null }

    fun uploadImage(uri: Uri, profile: ImageProfile, onUploaded: (String) -> Unit) {
        _uploadError.value = null
        viewModelScope.launch {
            val maxKb = adminState.value.appSettings.maxImageSizeKb
            imageRepo.uploadCompressedImage(uri, profile, maxKb) { p ->
                _uploadProgress.value = p
            }.onSuccess { imageId ->
                _uploadProgress.value = null
                onUploaded(imageId)
                _statusMessage.value = "Compressed & saved image to RTDB"
            }.onFailure { e ->
                _uploadProgress.value = null
                val msg = e.message ?: "Image upload failed"
                _uploadError.value = msg
                _statusMessage.value = msg
            }
        }
    }

    fun fetchMovieFromTmdb(tmdbId: Int, onResult: (Movie) -> Unit) {
        viewModelScope.launch {
            adminRepo.fetchMovieFromTmdb(tmdbId)
                .onSuccess {
                    onResult(it)
                    _statusMessage.value = "Fetched '${it.title}' from TMDB"
                }
                .onFailure { _statusMessage.value = it.message ?: "TMDB fetch failed" }
        }
    }

    fun saveMovie(movie: Movie) {
        if (movie.title.isBlank()) {
            _statusMessage.value = "Movie title is required"
            return
        }
        viewModelScope.launch {
            adminRepo.saveMovie(movie)
                .onSuccess { _statusMessage.value = "Movie '${it.title}' saved" }
                .onFailure { _statusMessage.value = it.message ?: "Save failed" }
        }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            adminRepo.deleteMovie(movie)
                .onSuccess { _statusMessage.value = "Deleted '${movie.title}'" }
                .onFailure { _statusMessage.value = it.message }
        }
    }

    fun toggleMoviePublish(movie: Movie) {
        viewModelScope.launch { adminRepo.toggleMoviePublish(movie) }
    }

    fun fetchSeriesFromTmdb(tmdbId: Int, onResult: (TvSeries, List<Season>) -> Unit) {
        viewModelScope.launch {
            adminRepo.fetchSeriesFromTmdb(tmdbId)
                .onSuccess { (series, seasons) ->
                    onResult(series, seasons)
                    _statusMessage.value = "Fetched '${series.title}' (${seasons.size} seasons) from TMDB"
                }
                .onFailure { _statusMessage.value = it.message ?: "TMDB fetch failed" }
        }
    }

    fun saveSeries(series: TvSeries) {
        if (series.title.isBlank()) {
            _statusMessage.value = "Series title is required"
            return
        }
        viewModelScope.launch {
            adminRepo.saveSeries(series)
                .onSuccess { _statusMessage.value = "Series '${it.title}' saved" }
                .onFailure { _statusMessage.value = it.message }
        }
    }

    fun deleteSeries(series: TvSeries) {
        viewModelScope.launch {
            adminRepo.deleteSeries(series)
                .onSuccess { _statusMessage.value = "Deleted '${series.title}'" }
        }
    }

    fun loadSeasons(seriesId: String) {
        viewModelScope.launch {
            _seasons.value = contentRepo.getSeasonsForSeries(seriesId).getOrNull().orEmpty()
            _episodes.value = emptyList()
        }
    }

    fun saveSeason(season: Season) {
        viewModelScope.launch {
            adminRepo.saveSeason(season).onSuccess {
                loadSeasons(season.seriesId)
                _statusMessage.value = "Season saved"
            }
        }
    }

    fun deleteSeason(seriesId: String, seasonId: String) {
        viewModelScope.launch {
            adminRepo.deleteSeason(seriesId, seasonId).onSuccess { loadSeasons(seriesId) }
        }
    }

    fun loadEpisodes(seriesId: String, seasonId: String) {
        viewModelScope.launch {
            _episodes.value = contentRepo.getEpisodesForSeason(seriesId, seasonId).getOrNull().orEmpty()
        }
    }

    fun saveEpisode(episode: Episode) {
        viewModelScope.launch {
            adminRepo.saveEpisode(episode).onSuccess {
                loadEpisodes(episode.seriesId, episode.seasonId)
                _statusMessage.value = "Episode saved"
            }
        }
    }

    fun deleteEpisode(seriesId: String, seasonId: String, episode: Episode) {
        viewModelScope.launch {
            adminRepo.deleteEpisode(seriesId, seasonId, episode).onSuccess {
                loadEpisodes(seriesId, seasonId)
            }
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            adminRepo.saveCategory(category).onSuccess { _statusMessage.value = "Category saved" }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { adminRepo.deleteCategory(category) }
    }

    fun saveGenre(genre: Genre) {
        viewModelScope.launch {
            adminRepo.saveGenre(genre).onSuccess { _statusMessage.value = "Genre saved" }
        }
    }

    fun deleteGenre(genre: Genre) {
        viewModelScope.launch { adminRepo.deleteGenre(genre) }
    }

    fun saveBanner(banner: Banner) {
        viewModelScope.launch {
            adminRepo.saveBanner(banner).onSuccess { _statusMessage.value = "Banner saved" }
        }
    }

    fun deleteBanner(banner: Banner) {
        viewModelScope.launch { adminRepo.deleteBanner(banner) }
    }

    fun updateUserStatus(uid: String, status: String, role: String) {
        viewModelScope.launch {
            adminRepo.updateUserStatus(uid, status, role)
                .onSuccess { _statusMessage.value = "User updated ($status / $role)" }
        }
    }

    fun deleteUserRecord(uid: String) {
        viewModelScope.launch {
            adminRepo.deleteUserRecord(uid).onSuccess { _statusMessage.value = "User record removed" }
        }
    }

    fun publishNotification(notification: AppNotification) {
        viewModelScope.launch {
            adminRepo.publishNotification(notification)
                .onSuccess { _statusMessage.value = "Notification published" }
        }
    }

    fun saveAppSettings(settings: AppSettings) {
        viewModelScope.launch {
            adminRepo.saveAppSettings(settings)
                .onSuccess { _statusMessage.value = "App settings updated" }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepo.signOut()
            onDone()
        }
    }
}
