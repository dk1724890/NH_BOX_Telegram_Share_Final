package com.nhbox.app.data.remote

import com.google.gson.annotations.SerializedName

data class TmdbGenreDto(
    val id: Int = 0,
    val name: String = ""
)

data class TmdbCastDto(
    val id: Int = 0,
    val name: String = "",
    val character: String? = null,
    val order: Int = 0
)

data class TmdbCrewDto(
    val id: Int = 0,
    val name: String = "",
    val job: String = "",
    val department: String = ""
)

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbVideoDto(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val type: String = ""
)

data class TmdbVideosResponse(
    val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbImageDto(
    @SerializedName("file_path") val filePath: String = "",
    @SerializedName("iso_639_1") val iso6391: String? = null
)

data class TmdbImagesResponse(
    val logos: List<TmdbImageDto> = emptyList(),
    val backdrops: List<TmdbImageDto> = emptyList(),
    val posters: List<TmdbImageDto> = emptyList()
)

data class TmdbMovieDetailsDto(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("original_title") val originalTitle: String = "",
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("original_language") val originalLanguage: String = "hi",
    val runtime: Int? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosResponse? = null,
    val images: TmdbImagesResponse? = null
)

data class TmdbSeasonSummaryDto(
    val id: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    val name: String = "",
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("air_date") val airDate: String? = null
)

data class TmdbEpisodeDto(
    val id: Int = 0,
    @SerializedName("episode_number") val episodeNumber: Int = 0,
    val name: String = "",
    val overview: String? = null,
    @SerializedName("still_path") val stillPath: String? = null,
    val runtime: Int? = null
)

data class TmdbSeasonDetailsDto(
    val id: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 1,
    val name: String = "",
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("air_date") val airDate: String? = null,
    val episodes: List<TmdbEpisodeDto> = emptyList()
)

data class TmdbTvDetailsDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = "hi",
    val status: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val seasons: List<TmdbSeasonSummaryDto> = emptyList(),
    val credits: TmdbCreditsDto? = null,
    val images: TmdbImagesResponse? = null
)

data class TmdbMultiSearchItemDto(
    val id: Int = 0,
    @SerializedName("media_type") val mediaType: String = "",
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0
)

data class TmdbSearchResponse(
    val results: List<TmdbMultiSearchItemDto> = emptyList()
)
