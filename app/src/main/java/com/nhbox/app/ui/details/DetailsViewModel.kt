package com.nhbox.app.ui.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.Episode
import com.nhbox.app.data.model.Movie
import com.nhbox.app.data.model.Season
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.data.model.WatchlistItem
import com.nhbox.app.data.model.toCatalogItem
import com.nhbox.app.data.repository.ContentRepository
import com.nhbox.app.data.repository.UserLibraryRepository
import com.nhbox.app.util.OfflineDownloadManager
import com.nhbox.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsContent(
    val isSeries: Boolean,
    val movie: Movie? = null,
    val series: TvSeries? = null,
    val seasons: List<Season> = emptyList(),
    val selectedSeasonId: String = "",
    val episodes: List<Episode> = emptyList(),
    val similarMovies: List<Movie> = emptyList(),
    val similarSeries: List<TvSeries> = emptyList(),
    val isInWatchlist: Boolean = false
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
    private val contentRepository: ContentRepository,
    private val userLibraryRepository: UserLibraryRepository
) : ViewModel() {

    private val contentId: String = savedStateHandle.get<String>("contentId").orEmpty()
    private val contentType: String = savedStateHandle.get<String>("contentType") ?: "movie"

    private val _uiState = MutableStateFlow<UiState<DetailsContent>>(UiState.Loading)
    val uiState: StateFlow<UiState<DetailsContent>> = _uiState.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            if (contentType == "series") {
                val res = contentRepository.getSeriesById(contentId)
                val series = res.getOrNull()
                if (res.isFailure) {
                    _uiState.value = UiState.DatabaseError(res.exceptionOrNull()?.message ?: "Failed to load series")
                    return@launch
                }
                if (series == null) {
                    _uiState.value = UiState.Empty("Series not found")
                    return@launch
                }
                val seasons = contentRepository.getSeasonsForSeries(contentId).getOrNull().orEmpty()
                val firstSeasonId = seasons.firstOrNull()?.id.orEmpty()
                val episodes = if (firstSeasonId.isNotBlank()) {
                    contentRepository.getEpisodesForSeason(contentId, firstSeasonId).getOrNull().orEmpty()
                } else emptyList()
                val similar = contentRepository.getSimilarSeries(series)
                val inWatchlist = userLibraryRepository.isInWatchlist(contentId)
                contentRepository.incrementViewCount(contentId, isSeries = true)

                _uiState.value = UiState.Success(
                    DetailsContent(
                        isSeries = true,
                        series = series,
                        seasons = seasons,
                        selectedSeasonId = firstSeasonId,
                        episodes = episodes,
                        similarSeries = similar,
                        isInWatchlist = inWatchlist
                    )
                )
            } else {
                val res = contentRepository.getMovieById(contentId)
                val movie = res.getOrNull()
                if (res.isFailure) {
                    _uiState.value = UiState.DatabaseError(res.exceptionOrNull()?.message ?: "Failed to load movie")
                    return@launch
                }
                if (movie == null) {
                    _uiState.value = UiState.Empty("Movie not found")
                    return@launch
                }
                val similar = contentRepository.getSimilarMovies(movie)
                val inWatchlist = userLibraryRepository.isInWatchlist(contentId)
                contentRepository.incrementViewCount(contentId, isSeries = false)

                _uiState.value = UiState.Success(
                    DetailsContent(
                        isSeries = false,
                        movie = movie,
                        similarMovies = similar,
                        isInWatchlist = inWatchlist
                    )
                )
            }
        }
    }

    fun selectSeason(seasonId: String) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val eps = contentRepository.getEpisodesForSeason(contentId, seasonId).getOrNull().orEmpty()
            _uiState.value = UiState.Success(
                current.copy(selectedSeasonId = seasonId, episodes = eps)
            )
        }
    }

    fun toggleWatchlist() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            if (current.isInWatchlist) {
                userLibraryRepository.removeFromWatchlist(contentId)
                    .onSuccess {
                        _uiState.value = UiState.Success(current.copy(isInWatchlist = false))
                        _feedbackMessage.value = "Removed from Watchlist"
                    }
            } else {
                val item = if (current.isSeries && current.series != null) {
                    WatchlistItem(
                        contentId = current.series.id,
                        contentType = "series",
                        title = current.series.title,
                        posterUrl = current.series.posterUrl,
                        posterImageId = current.series.posterImageId,
                        releaseYear = current.series.releaseYear,
                        rating = current.series.rating,
                        addedAt = System.currentTimeMillis()
                    )
                } else if (current.movie != null) {
                    WatchlistItem(
                        contentId = current.movie.id,
                        contentType = "movie",
                        title = current.movie.title,
                        posterUrl = current.movie.posterUrl,
                        posterImageId = current.movie.posterImageId,
                        releaseYear = current.movie.releaseYear,
                        rating = current.movie.rating,
                        addedAt = System.currentTimeMillis()
                    )
                } else return@launch

                userLibraryRepository.addToWatchlist(item)
                    .onSuccess {
                        _uiState.value = UiState.Success(current.copy(isInWatchlist = true))
                        _feedbackMessage.value = "Added to Watchlist"
                    }
            }
        }
    }

    fun startDownload() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        if (current.isSeries && current.series != null) {
            val firstEp = current.episodes.firstOrNull()
            val seasonNum = current.seasons.firstOrNull { it.id == current.selectedSeasonId }?.seasonNumber ?: 1
            val epWithSeason = firstEp?.copy(seasonNumber = seasonNum)
            val catalogItem = current.series.toCatalogItem(current.seasons)
            OfflineDownloadManager.startDownload(appContext, catalogItem, epWithSeason)
            _feedbackMessage.value = "Download started: ${current.series.title}"
        } else if (current.movie != null) {
            val catalogItem = current.movie.toCatalogItem()
            OfflineDownloadManager.startDownload(appContext, catalogItem)
            _feedbackMessage.value = "Download started: ${current.movie.title}"
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }
}
