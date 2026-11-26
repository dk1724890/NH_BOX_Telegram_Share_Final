package com.nhbox.app.data.repository

import android.content.Context
import com.nhbox.app.BuildConfig
import com.nhbox.app.security.NhCreditVault
import dagger.hilt.android.qualifiers.ApplicationContext
import com.nhbox.app.data.model.Banner
import com.nhbox.app.data.model.Category
import com.nhbox.app.data.model.Episode
import com.nhbox.app.data.model.Genre
import com.nhbox.app.data.model.Movie
import com.nhbox.app.data.model.Season
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.data.remote.FirebaseRtdbService
import com.nhbox.app.data.remote.TmdbApiService
import com.nhbox.app.data.remote.TmdbMultiSearchItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rtdb: FirebaseRtdbService,
    private val tmdbApi: TmdbApiService
) {
    fun observePublishedMovies(): Flow<Result<List<Movie>>> {
        return rtdb.observeList(rtdb.ref(NhCreditVault.node(context, "movies")), Movie::class.java).map { res ->
            res.map { list ->
                list.filter { it.isPublished }.sortedByDescending { it.createdAt }
            }
        }
    }

    fun observePublishedSeries(): Flow<Result<List<TvSeries>>> {
        return rtdb.observeList(rtdb.ref(NhCreditVault.node(context, "tvSeries")), TvSeries::class.java).map { res ->
            res.map { list ->
                list.filter { it.isPublished }.sortedByDescending { it.createdAt }
            }
        }
    }

    fun observeActiveBanners(): Flow<Result<List<Banner>>> {
        return rtdb.observeList(rtdb.ref(NhCreditVault.node(context, "banners")), Banner::class.java).map { res ->
            res.map { list ->
                list.filter { it.isEnabled }.sortedBy { it.order }
            }
        }
    }

    fun observeEnabledCategories(): Flow<Result<List<Category>>> {
        return rtdb.observeList(rtdb.ref(NhCreditVault.node(context, "categories")), Category::class.java).map { res ->
            res.map { list ->
                list.filter { it.isEnabled }.sortedBy { it.order }
            }
        }
    }

    fun observeGenres(): Flow<Result<List<Genre>>> {
        return rtdb.observeList(rtdb.ref(NhCreditVault.node(context, "genres")), Genre::class.java).map { res ->
            res.map { list -> list.filter { it.isEnabled }.sortedBy { it.name } }
        }
    }

    suspend fun getMovieById(movieId: String): Result<Movie?> {
        return rtdb.getValueOnce("movies/$movieId", Movie::class.java)
    }

    suspend fun getSeriesById(seriesId: String): Result<TvSeries?> {
        return rtdb.getValueOnce("tvSeries/$seriesId", TvSeries::class.java)
    }

    suspend fun getSeasonsForSeries(seriesId: String): Result<List<Season>> {
        val query = rtdb.ref("seasons/$seriesId").orderByChild("seasonNumber")
        return rtdb.getListOnce(query, Season::class.java).map { it.sortedBy { s -> s.seasonNumber } }
    }

    suspend fun getEpisodesForSeason(seriesId: String, seasonId: String): Result<List<Episode>> {
        val query = rtdb.ref("episodes/$seriesId/$seasonId").orderByChild("episodeNumber")
        return rtdb.getListOnce(query, Episode::class.java).map { it.sortedBy { e -> e.episodeNumber } }
    }

    suspend fun getSimilarMovies(movie: Movie): List<Movie> {
        val all = rtdb.getListOnce(rtdb.ref(NhCreditVault.node(context, "movies")), Movie::class.java)
            .getOrNull().orEmpty().filter { it.isPublished }

        return all.filter { it.id != movie.id }
            .sortedByDescending { candidate ->
                candidate.genres.count { it in movie.genres } * 10 + candidate.rating.toInt()
            }
            .take(10)
    }

    suspend fun getSimilarSeries(series: TvSeries): List<TvSeries> {
        val all = rtdb.getListOnce(rtdb.ref(NhCreditVault.node(context, "tvSeries")), TvSeries::class.java)
            .getOrNull().orEmpty().filter { it.isPublished }

        return all.filter { it.id != series.id }
            .sortedByDescending { candidate ->
                candidate.genres.count { it in series.genres } * 10 + candidate.rating.toInt()
            }
            .take(10)
    }

    suspend fun incrementViewCount(contentId: String, isSeries: Boolean) {
        runCatching {
            val path = if (isSeries) "tvSeries/$contentId/viewCount" else "movies/$contentId/viewCount"
            val current = rtdb.getValueOnce(path, Long::class.java).getOrNull() ?: 0L
            rtdb.setValue(path, current + 1L)
            rtdb.setValue("analytics/contentViews/$contentId", current + 1L)
        }
    }

    suspend fun searchTmdbIfConfigured(query: String): List<TmdbMultiSearchItemDto> {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank() || query.isBlank()) return emptyList()
        return runCatching {
            tmdbApi.searchMulti(apiKey = apiKey, query = query).results
                .filter { it.mediaType == "movie" || it.mediaType == "tv" }
        }.getOrDefault(emptyList())
    }
}
