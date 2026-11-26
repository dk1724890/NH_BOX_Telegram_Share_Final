package com.nhbox.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.components.EmptyStateView
import com.nhbox.app.ui.components.LoadingStateView
import com.nhbox.app.ui.components.PosterCard
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.UiState

@Composable
fun CategoryViewAllScreen(
    sectionType: String,
    onBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val title = when (sectionType) {
        "trending_movies" -> "Trending Movies"
        "popular_series" -> "Popular Web Series"
        "recently_added" -> "Recently Added"
        else -> "All Content"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NhBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = NhWhite
                )
            }
            Text(
                text = title,
                color = NhWhite,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when (val state = uiState) {
            is UiState.Loading -> LoadingStateView()
            is UiState.Success -> {
                val feed = state.data
                if (sectionType == "popular_series") {
                    if (feed.popularSeries.isEmpty()) {
                        EmptyStateView("No Web Series", "Check back soon.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(feed.popularSeries, key = { it.id }) { series ->
                                PosterCard(
                                    title = series.title,
                                    posterSource = series.resolvedPoster(),
                                    releaseYear = series.releaseYear,
                                    rating = series.rating,
                                    onClick = { onSeriesClick(series.id) }
                                )
                            }
                        }
                    }
                } else {
                    val list = if (sectionType == "recently_added") {
                        feed.recentlyAddedMovies
                    } else {
                        feed.trendingMovies
                    }
                    if (list.isEmpty()) {
                        EmptyStateView("No Movies", "Check back soon.")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(list, key = { it.id }) { movie ->
                                PosterCard(
                                    title = movie.title,
                                    posterSource = movie.resolvedPoster(),
                                    releaseYear = movie.releaseYear,
                                    rating = movie.rating,
                                    onClick = { onMovieClick(movie.id) }
                                )
                            }
                        }
                    }
                }
            }
            else -> EmptyStateView("Unavailable", "Unable to load this category right now.")
        }
    }
}
