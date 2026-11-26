package com.nhbox.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.nhbox.app.BuildConfig
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
import com.nhbox.app.data.remote.FirebaseRtdbService
import com.nhbox.app.data.remote.TmdbApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val rtdb: FirebaseRtdbService,
    private val auth: FirebaseAuth,
    private val tmdbApi: TmdbApiService,
    private val imageRepo: ImageRtdbRepository
) {
    fun observeAllMovies(): Flow<Result<List<Movie>>> =
        rtdb.observeList(rtdb.ref("movies"), Movie::class.java).map { res ->
            res.map { it.sortedByDescending { m -> m.createdAt } }
        }

    fun observeAllSeries(): Flow<Result<List<TvSeries>>> =
        rtdb.observeList(rtdb.ref("tvSeries"), TvSeries::class.java).map { res ->
            res.map { it.sortedByDescending { s -> s.createdAt } }
        }

    fun observeAllCategories(): Flow<Result<List<Category>>> =
        rtdb.observeList(rtdb.ref("categories"), Category::class.java).map { res ->
            res.map { it.sortedBy { c -> c.order } }
        }

    fun observeAllGenres(): Flow<Result<List<Genre>>> =
        rtdb.observeList(rtdb.ref("genres"), Genre::class.java).map { res ->
            res.map { it.sortedBy { g -> g.name } }
        }

    fun observeAllBanners(): Flow<Result<List<Banner>>> =
        rtdb.observeList(rtdb.ref("banners"), Banner::class.java).map { res ->
            res.map { it.sortedBy { b -> b.order } }
        }

    fun observeAllUsers(): Flow<Result<List<UserProfile>>> =
        rtdb.observeList(rtdb.ref("users"), UserProfile::class.java).map { res ->
            res.map { it.sortedByDescending { u -> u.lastActive } }
        }

    fun observeRecentActivity(): Flow<Result<List<AdminActivityLog>>> =
        rtdb.observeList(rtdb.ref("analytics/adminActivity").limitToLast(25), AdminActivityLog::class.java)
            .map { res -> res.map { it.sortedByDescending { a -> a.timestamp } } }

    fun observeAppSettings(): Flow<Result<AppSettings?>> =
        rtdb.observeValue("appSettings", AppSettings::class.java)

    fun computeStats(
        movies: List<Movie>,
        series: List<TvSeries>,
        categories: List<Category>,
        users: List<UserProfile>
    ): AdminDashboardStats {
        val totalViews = movies.sumOf { it.viewCount } + series.sumOf { it.viewCount }
        return AdminDashboardStats(
            totalMovies = movies.size,
            totalSeries = series.size,
            totalCategories = categories.size,
            totalUsers = users.size,
            onlineUsers = users.count { it.isOnline },
            totalViews = totalViews
        )
    }

    suspend fun saveMovie(movie: Movie): Result<Movie> = runCatching {
        val now = System.currentTimeMillis()
        val id = movie.id.ifBlank { rtdb.generateKey("movies") }
        val finalMovie = movie.copy(
            id = id,
            createdAt = if (movie.createdAt == 0L) now else movie.createdAt,
            updatedAt = now
        )
        rtdb.setValue("movies/$id", finalMovie).getOrThrow()
        logActivity("Save Movie", "Saved '${finalMovie.title}'")
        finalMovie
    }

    suspend fun deleteMovie(movie: Movie): Result<Unit> = runCatching {
        rtdb.removeValue("movies/${movie.id}").getOrThrow()
        imageRepo.deleteImageIfExists(movie.posterImageId)
        imageRepo.deleteImageIfExists(movie.backdropImageId)
        imageRepo.deleteImageIfExists(movie.logoImageId)
        movie.castImages.filter { it.startsWith("rtdb://") }.forEach { imageRepo.deleteImageIfExists(it) }
        logActivity("Delete Movie", "Deleted '${movie.title}'")
    }

    suspend fun toggleMoviePublish(movie: Movie): Result<Unit> = runCatching {
        val newState = !movie.isPublished
        rtdb.updateChildren(
            "movies/${movie.id}",
            mapOf("published" to newState, "updatedAt" to System.currentTimeMillis())
        ).getOrThrow()
        logActivity(if (newState) "Publish Movie" else "Unpublish Movie", movie.title)
    }

    suspend fun saveSeries(series: TvSeries): Result<TvSeries> = runCatching {
        val now = System.currentTimeMillis()
        val id = series.id.ifBlank { rtdb.generateKey("tvSeries") }
        val finalSeries = series.copy(
            id = id,
            createdAt = if (series.createdAt == 0L) now else series.createdAt,
            updatedAt = now
        )
        rtdb.setValue("tvSeries/$id", finalSeries).getOrThrow()
        logActivity("Save Series", "Saved '${finalSeries.title}'")
        finalSeries
    }

    suspend fun deleteSeries(series: TvSeries): Result<Unit> = runCatching {
        rtdb.removeValue("tvSeries/${series.id}").getOrThrow()
        rtdb.removeValue("seasons/${series.id}")
        rtdb.removeValue("episodes/${series.id}")
        imageRepo.deleteImageIfExists(series.posterImageId)
        imageRepo.deleteImageIfExists(series.backdropImageId)
        imageRepo.deleteImageIfExists(series.logoImageId)
        series.castImages.filter { it.startsWith("rtdb://") }.forEach { imageRepo.deleteImageIfExists(it) }
        logActivity("Delete Series", "Deleted '${series.title}'")
    }

    suspend fun saveSeason(season: Season): Result<Season> = runCatching {
        val id = season.id.ifBlank { rtdb.generateKey("seasons/${season.seriesId}") }
        val finalSeason = season.copy(id = id)
        rtdb.setValue("seasons/${season.seriesId}/$id", finalSeason).getOrThrow()
        logActivity("Save Season", "Season ${finalSeason.seasonNumber}: ${finalSeason.name}")
        finalSeason
    }

    suspend fun deleteSeason(seriesId: String, seasonId: String): Result<Unit> = runCatching {
        rtdb.removeValue("seasons/$seriesId/$seasonId").getOrThrow()
        rtdb.removeValue("episodes/$seriesId/$seasonId")
        logActivity("Delete Season", "Removed season $seasonId")
    }

    suspend fun saveEpisode(episode: Episode): Result<Episode> = runCatching {
        val id = episode.id.ifBlank {
            rtdb.generateKey("episodes/${episode.seriesId}/${episode.seasonId}")
        }
        val finalEp = episode.copy(id = id)
        rtdb.setValue(
            "episodes/${episode.seriesId}/${episode.seasonId}/$id",
            finalEp
        ).getOrThrow()
        logActivity("Save Episode", "Ep ${finalEp.episodeNumber}: ${finalEp.name}")
        finalEp
    }

    suspend fun deleteEpisode(seriesId: String, seasonId: String, episode: Episode): Result<Unit> =
        runCatching {
            rtdb.removeValue("episodes/$seriesId/$seasonId/${episode.id}").getOrThrow()
            imageRepo.deleteImageIfExists(episode.posterImageId)
            logActivity("Delete Episode", episode.name)
        }

    suspend fun saveCategory(category: Category): Result<Unit> = runCatching {
        val id = category.id.ifBlank { rtdb.generateKey("categories") }
        rtdb.setValue("categories/$id", category.copy(id = id)).getOrThrow()
        logActivity("Save Category", category.name)
    }

    suspend fun deleteCategory(category: Category): Result<Unit> = runCatching {
        rtdb.removeValue("categories/${category.id}").getOrThrow()
        imageRepo.deleteImageIfExists(category.imageId)
        logActivity("Delete Category", category.name)
    }

    suspend fun saveGenre(genre: Genre): Result<Unit> = runCatching {
        val id = genre.id.ifBlank { rtdb.generateKey("genres") }
        rtdb.setValue("genres/$id", genre.copy(id = id)).getOrThrow()
        logActivity("Save Genre", genre.name)
    }

    suspend fun deleteGenre(genre: Genre): Result<Unit> = runCatching {
        rtdb.removeValue("genres/${genre.id}").getOrThrow()
        logActivity("Delete Genre", genre.name)
    }

    suspend fun saveBanner(banner: Banner): Result<Unit> = runCatching {
        val id = banner.id.ifBlank { rtdb.generateKey("banners") }
        rtdb.setValue("banners/$id", banner.copy(id = id)).getOrThrow()
        logActivity("Save Banner", banner.title)
    }

    suspend fun deleteBanner(banner: Banner): Result<Unit> = runCatching {
        rtdb.removeValue("banners/${banner.id}").getOrThrow()
        imageRepo.deleteImageIfExists(banner.imageId)
        logActivity("Delete Banner", banner.title)
    }

    suspend fun updateUserStatus(uid: String, status: String, role: String): Result<Unit> =
        runCatching {
            rtdb.updateChildren(
                "users/$uid",
                mapOf("accountStatus" to status, "role" to role)
            ).getOrThrow()
            logActivity("Update User", "UID $uid -> status=$status, role=$role")
        }

    suspend fun deleteUserRecord(uid: String): Result<Unit> = runCatching {
        rtdb.removeValue("users/$uid").getOrThrow()
        rtdb.removeValue("watchlist/$uid")
        rtdb.removeValue("watchHistory/$uid")
        rtdb.removeValue("downloads/$uid")
        logActivity("Delete User", "Removed user data for UID $uid")
    }

    suspend fun publishNotification(notification: AppNotification): Result<Unit> = runCatching {
        val id = notification.id.ifBlank { rtdb.generateKey("notifications/broadcast") }
        val finalNotif = notification.copy(id = id, createdAt = System.currentTimeMillis())
        rtdb.setValue("notifications/broadcast/$id", finalNotif).getOrThrow()
        logActivity("Publish Notification", finalNotif.title)
    }

    suspend fun saveAppSettings(settings: AppSettings): Result<Unit> = runCatching {
        rtdb.setValue("appSettings", settings).getOrThrow()
        logActivity("Update App Settings", "Max image KB: ${settings.maxImageSizeKb}")
    }

    suspend fun fetchMovieFromTmdb(tmdbId: Int): Result<Movie> = runCatching {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) error("TMDB_API_KEY is not configured in local.properties")
        val dto = tmdbApi.getMovieDetails(movieId = tmdbId, apiKey = apiKey)
        val imgBase = BuildConfig.TMDB_IMAGE_BASE_URL
        val director = dto.credits?.crew?.firstOrNull { it.job == "Director" }?.name.orEmpty()
        val cast = dto.credits?.cast?.sortedBy { it.order }?.take(8)?.map { it.name }.orEmpty()
        val trailerKey = dto.videos?.results?.firstOrNull {
            it.site == "YouTube" && (it.type == "Trailer" || it.type == "Teaser")
        }?.key.orEmpty()
        val logoPath = dto.images?.logos?.firstOrNull { it.iso6391 == "en" }?.filePath
            ?: dto.images?.logos?.firstOrNull()?.filePath

        Movie(
            tmdbId = dto.id,
            title = dto.title,
            originalTitle = dto.originalTitle,
            posterUrl = dto.posterPath?.let { "${imgBase}w500$it" }.orEmpty(),
            backdropUrl = dto.backdropPath?.let { "${imgBase}w1280$it" }.orEmpty(),
            logoUrl = logoPath?.let { "${imgBase}w500$it" }.orEmpty(),
            overview = dto.overview,
            releaseDate = dto.releaseDate.orEmpty(),
            releaseYear = dto.releaseDate?.take(4).orEmpty(),
            rating = (dto.voteAverage * 10).toInt() / 10.0,
            voteCount = dto.voteCount,
            language = dto.originalLanguage.uppercase(),
            runtime = dto.runtime ?: 0,
            genres = dto.genres.map { it.name },
            cast = cast,
            director = director,
            trailerUrl = if (trailerKey.isNotBlank()) "https://www.youtube.com/watch?v=$trailerKey" else ""
        )
    }

    suspend fun fetchSeriesFromTmdb(tmdbId: Int): Result<Pair<TvSeries, List<Season>>> = runCatching {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) error("TMDB_API_KEY is not configured in local.properties")
        val dto = tmdbApi.getTvDetails(tvId = tmdbId, apiKey = apiKey)
        val imgBase = BuildConfig.TMDB_IMAGE_BASE_URL
        val director = dto.credits?.crew?.firstOrNull {
            it.job == "Director" || it.job == "Executive Producer"
        }?.name.orEmpty()
        val cast = dto.credits?.cast?.sortedBy { it.order }?.take(8)?.map { it.name }.orEmpty()

        val series = TvSeries(
            tmdbId = dto.id,
            title = dto.name,
            posterUrl = dto.posterPath?.let { "${imgBase}w500$it" }.orEmpty(),
            backdropUrl = dto.backdropPath?.let { "${imgBase}w1280$it" }.orEmpty(),
            overview = dto.overview,
            releaseYear = dto.firstAirDate?.take(4).orEmpty(),
            rating = (dto.voteAverage * 10).toInt() / 10.0,
            language = dto.originalLanguage.uppercase(),
            genres = dto.genres.map { it.name },
            cast = cast,
            director = director,
            status = dto.status ?: "Ongoing"
        )
        val seasons = dto.seasons.filter { it.seasonNumber > 0 }.map { s ->
            Season(
                seasonNumber = s.seasonNumber,
                name = s.name,
                posterUrl = s.posterPath?.let { "${imgBase}w500$it" }.orEmpty(),
                overview = s.overview.orEmpty(),
                releaseDate = s.airDate.orEmpty()
            )
        }
        series to seasons
    }

    suspend fun fetchSeasonEpisodesFromTmdb(
        tmdbId: Int,
        seriesId: String,
        seasonId: String,
        seasonNumber: Int
    ): Result<List<Episode>> = runCatching {
        val apiKey = BuildConfig.TMDB_API_KEY
        if (apiKey.isBlank()) error("TMDB_API_KEY is not configured in local.properties")
        val dto = tmdbApi.getTvSeasonDetails(tmdbId, seasonNumber, apiKey)
        val imgBase = BuildConfig.TMDB_IMAGE_BASE_URL
        dto.episodes.map { ep ->
            Episode(
                seriesId = seriesId,
                seasonId = seasonId,
                episodeNumber = ep.episodeNumber,
                name = ep.name,
                posterUrl = ep.stillPath?.let { "${imgBase}w500$it" }.orEmpty(),
                overview = ep.overview.orEmpty(),
                runtime = ep.runtime ?: 45
            )
        }
    }

    private suspend fun logActivity(action: String, details: String) {
        runCatching {
            val id = rtdb.generateKey("analytics/adminActivity")
            val user = auth.currentUser
            val log = AdminActivityLog(
                id = id,
                adminUid = user?.uid.orEmpty(),
                adminName = user?.email ?: "Admin",
                action = action,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            rtdb.setValue("analytics/adminActivity/$id", log)
        }
    }
}
