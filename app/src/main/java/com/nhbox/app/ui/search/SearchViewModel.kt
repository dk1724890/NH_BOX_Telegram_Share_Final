package com.nhbox.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhbox.app.data.model.Movie
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.data.remote.TmdbMultiSearchItemDto
import com.nhbox.app.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val selectedGenre: String = "All",
    val selectedLanguage: String = "All",
    val selectedYear: String = "All",
    val recentSearches: List<String> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<TvSeries> = emptyList(),
    val tmdbSuggestions: List<TmdbMultiSearchItemDto> = emptyList(),
    val availableGenres: List<String> = listOf("All"),
    val availableLanguages: List<String> = listOf("All"),
    val availableYears: List<String> = listOf("All"),
    val isLoading: Boolean = true
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _allMovies = MutableStateFlow<List<Movie>>(emptyList())
    private val _allSeries = MutableStateFlow<List<TvSeries>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _genre = MutableStateFlow("All")
    private val _language = MutableStateFlow("All")
    private val _year = MutableStateFlow("All")
    private val _recent = MutableStateFlow<List<String>>(emptyList())
    private val _tmdb = MutableStateFlow<List<TmdbMultiSearchItemDto>>(emptyList())

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            contentRepository.observePublishedMovies().collect { res ->
                _allMovies.value = res.getOrNull().orEmpty()
            }
        }
        viewModelScope.launch {
            contentRepository.observePublishedSeries().collect { res ->
                _allSeries.value = res.getOrNull().orEmpty()
            }
        }
        viewModelScope.launch {
            combine(
                _allMovies,
                _allSeries,
                _query,
                _genre,
                _language,
                _year,
                _recent,
                _tmdb
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val movies = values[0] as List<Movie>
                @Suppress("UNCHECKED_CAST")
                val series = values[1] as List<TvSeries>
                val q = values[2] as String
                val genre = values[3] as String
                val lang = values[4] as String
                val year = values[5] as String
                @Suppress("UNCHECKED_CAST")
                val recent = values[6] as List<String>
                @Suppress("UNCHECKED_CAST")
                val tmdb = values[7] as List<TmdbMultiSearchItemDto>

                val genres = listOf("All") + (movies.flatMap { it.genres } + series.flatMap { it.genres })
                    .filter { it.isNotBlank() }.distinct().sorted()
                val langs = listOf("All") + (movies.map { it.language } + series.map { it.language })
                    .filter { it.isNotBlank() }.distinct().sorted()
                val years = listOf("All") + (movies.map { it.releaseYear } + series.map { it.releaseYear })
                    .filter { it.isNotBlank() }.distinct().sortedDescending()

                val filteredMovies = movies.filter { m ->
                    (q.isBlank() || m.title.contains(q, ignoreCase = true) || m.cast.any { it.contains(q, true) }) &&
                        (genre == "All" || m.genres.contains(genre)) &&
                        (lang == "All" || m.language.equals(lang, ignoreCase = true)) &&
                        (year == "All" || m.releaseYear == year)
                }
                val filteredSeries = series.filter { s ->
                    (q.isBlank() || s.title.contains(q, ignoreCase = true) || s.cast.any { it.contains(q, true) }) &&
                        (genre == "All" || s.genres.contains(genre)) &&
                        (lang == "All" || s.language.equals(lang, ignoreCase = true)) &&
                        (year == "All" || s.releaseYear == year)
                }

                SearchUiState(
                    query = q,
                    selectedGenre = genre,
                    selectedLanguage = lang,
                    selectedYear = year,
                    recentSearches = recent,
                    movies = filteredMovies,
                    series = filteredSeries,
                    tmdbSuggestions = tmdb,
                    availableGenres = genres,
                    availableLanguages = langs,
                    availableYears = years,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.length >= 3) {
            viewModelScope.launch {
                _tmdb.value = contentRepository.searchTmdbIfConfigured(newQuery).take(6)
            }
        } else {
            _tmdb.value = emptyList()
        }
    }

    fun submitSearch() {
        val q = _query.value.trim()
        if (q.isNotBlank()) {
            _recent.value = (listOf(q) + _recent.value.filter { it != q }).take(8)
        }
    }

    fun selectGenre(g: String) { _genre.value = g }
    fun selectLanguage(l: String) { _language.value = l }
    fun selectYear(y: String) { _year.value = y }
    fun clearSearch() { _query.value = ""; _tmdb.value = emptyList() }
}
