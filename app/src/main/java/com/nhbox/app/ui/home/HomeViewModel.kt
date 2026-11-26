package com.nhbox.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.Banner
import com.nhbox.app.data.model.Category
import com.nhbox.app.data.model.Movie
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.data.repository.ContentRepository
import com.nhbox.app.utils.UiState
import com.nhbox.app.utils.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeFeedData(
    val banners: List<Banner> = emptyList(),
    val trendingMovies: List<Movie> = emptyList(),
    val popularSeries: List<TvSeries> = emptyList(),
    val recentlyAddedMovies: List<Movie> = emptyList(),
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _uiState = MutableStateFlow<UiState<HomeFeedData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeFeedData>> = _uiState.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            combine(
                contentRepository.observeActiveBanners(),
                contentRepository.observePublishedMovies(),
                contentRepository.observePublishedSeries(),
                contentRepository.observeEnabledCategories()
            ) { bannersRes, moviesRes, seriesRes, catsRes ->
                val error = bannersRes.exceptionOrNull()
                    ?: moviesRes.exceptionOrNull()
                    ?: seriesRes.exceptionOrNull()
                    ?: catsRes.exceptionOrNull()

                if (error != null) {
                    if (!isOnline.value) {
                        UiState.NetworkError(error.message ?: "No internet connection")
                    } else {
                        UiState.DatabaseError(error.message ?: "Failed to load content")
                    }
                } else {
                    val movies = moviesRes.getOrNull().orEmpty()
                    val series = seriesRes.getOrNull().orEmpty()
                    val banners = bannersRes.getOrNull().orEmpty().ifEmpty {
                        movies.filter { it.isFeatured }.ifEmpty { movies }.take(5).mapIndexed { idx, m ->
                            Banner(
                                id = m.id,
                                title = m.title,
                                imageUrl = m.backdropUrl.ifBlank { m.posterUrl },
                                imageId = m.backdropImageId.ifBlank { m.posterImageId },
                                targetId = m.id,
                                targetType = "movie",
                                order = idx,
                                isEnabled = true
                            )
                        }
                    }

                    val trending = movies.sortedByDescending { it.rating * 10 + it.viewCount }.take(15)
                    val popularSeries = series.sortedByDescending { it.rating * 10 + it.viewCount }.take(15)
                    val recent = movies.sortedByDescending { it.createdAt }.take(15)
                    val categories = catsRes.getOrNull().orEmpty()

                    if (movies.isEmpty() && series.isEmpty() && banners.isEmpty()) {
                        UiState.Empty("No movies or series published yet. Add content from the Admin Panel.")
                    } else {
                        UiState.Success(
                            HomeFeedData(
                                banners = banners,
                                trendingMovies = trending,
                                popularSeries = popularSeries,
                                recentlyAddedMovies = recent,
                                categories = categories
                            )
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
