package com.nhbox.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.data.model.Banner
import com.nhbox.app.ui.components.EmptyStateView
import com.nhbox.app.ui.components.ErrorStateView
import com.nhbox.app.ui.components.LoadingStateView
import com.nhbox.app.ui.components.OfflineBanner
import com.nhbox.app.ui.components.PosterCard
import com.nhbox.app.ui.components.RtdbAsyncImage
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhMutedGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.UiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NhBackground)
            .statusBarsPadding()
    ) {
        // DESIGN-LOCKED HEADER
        HomeHeader(
            onMenuClick = onOpenDrawer,
            onSearchClick = onOpenSearch
        )

        if (!isOnline) {
            OfflineBanner()
        }

        when (val state = uiState) {
            is UiState.Loading -> LoadingStateView()
            is UiState.Empty -> EmptyStateView(
                title = "No Content Yet",
                subtitle = state.message
            )
            is UiState.NetworkError -> ErrorStateView(
                message = state.message,
                isNetworkError = true,
                onRetry = { viewModel.loadHomeFeed() }
            )
            is UiState.DatabaseError -> ErrorStateView(
                message = state.message,
                isNetworkError = false,
                onRetry = { viewModel.loadHomeFeed() }
            )
            is UiState.Success -> {
                val feed = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (feed.banners.isNotEmpty()) {
                        item(key = "banner_slider") {
                            FeaturedBannerSlider(
                                banners = feed.banners,
                                onBannerClick = { banner ->
                                    if (banner.targetType == "series") {
                                        onSeriesClick(banner.targetId)
                                    } else {
                                        onMovieClick(banner.targetId)
                                    }
                                }
                            )
                        }
                    }

                    if (feed.trendingMovies.isNotEmpty()) {
                        item(key = "trending_movies") {
                            SectionHeaderRow(
                                title = "Trending Movies",
                                onViewAll = { onViewAllClick("trending_movies") }
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(feed.trendingMovies, key = { it.id }) { movie ->
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

                    if (feed.popularSeries.isNotEmpty()) {
                        item(key = "popular_series") {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeaderRow(
                                title = "Popular Web Series",
                                onViewAll = { onViewAllClick("popular_series") }
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    }

                    if (feed.recentlyAddedMovies.isNotEmpty()) {
                        item(key = "recently_added") {
                            Spacer(modifier = Modifier.height(14.dp))
                            SectionHeaderRow(
                                title = "Recently Added",
                                onViewAll = { onViewAllClick("recently_added") }
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(feed.recentlyAddedMovies, key = { it.id }) { movie ->
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
            }
        }
    }
}

@Composable
private fun HomeHeader(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = NhWhite,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = NhWhite, fontWeight = FontWeight.ExtraBold)) {
                    append("NH ")
                }
                withStyle(SpanStyle(color = NhRed, fontWeight = FontWeight.ExtraBold)) {
                    append("BOX")
                }
            },
            fontSize = 22.sp,
            letterSpacing = 1.2.sp
        )

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = NhWhite,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedBannerSlider(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(4500)
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(14.dp))
        ) { page ->
            val banner = banners[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onBannerClick(banner) }
            ) {
                RtdbAsyncImage(
                    source = banner.resolvedImage(),
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Text(
                    text = banner.title.uppercase(),
                    color = NhWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NhRed else NhMutedGray)
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(
    title: String,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NhRed)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = NhWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(NhRed)
                .clickable(onClick = onViewAll)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "VIEW ALL",
                color = NhWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
