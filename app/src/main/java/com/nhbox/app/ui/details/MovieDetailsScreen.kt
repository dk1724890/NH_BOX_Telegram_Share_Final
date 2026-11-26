package com.nhbox.app.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.components.EmptyStateView
import com.nhbox.app.ui.components.ErrorStateView
import com.nhbox.app.ui.components.LoadingStateView
import com.nhbox.app.ui.components.PosterCard
import com.nhbox.app.ui.components.RtdbAsyncImage
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhGoldStar
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSecondaryBg
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailsScreen(
    onBack: () -> Unit,
    onPlayVideo: (contentId: String, contentType: String, episodeId: String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onOpenSeries: (String) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedback) {
        feedback?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        containerColor = NhBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingStateView(Modifier.padding(innerPadding))
            is UiState.Empty -> EmptyStateView("Not Found", state.message, Modifier.padding(innerPadding))
            is UiState.NetworkError -> ErrorStateView(state.message, true, { viewModel.loadDetails() }, Modifier.padding(innerPadding))
            is UiState.DatabaseError -> ErrorStateView(state.message, false, { viewModel.loadDetails() }, Modifier.padding(innerPadding))
            is UiState.Success -> {
                val data = state.data
                val movie = data.movie
                val series = data.series

                val title = movie?.title ?: series?.title.orEmpty()
                val backdrop = movie?.resolvedBackdrop() ?: series?.resolvedBackdrop().orEmpty()
                val poster = movie?.resolvedPoster() ?: series?.resolvedPoster().orEmpty()
                val rating = movie?.rating ?: series?.rating ?: 0.0
                val quality = movie?.quality ?: series?.quality ?: "4K UHD"
                val year = movie?.releaseYear ?: series?.releaseYear.orEmpty()
                val language = movie?.language ?: series?.language ?: "Hindi"
                val ageRating = movie?.ageRating ?: series?.ageRating ?: "16+"
                val overview = movie?.overview ?: series?.overview.orEmpty()
                val castImages = (movie?.castImages ?: series?.castImages).orEmpty()
                var storyExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NhBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp)
                ) {
                    // DESIGN-LOCKED BACKDROP SECTION
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(235.dp)
                    ) {
                        RtdbAsyncImage(
                            source = backdrop.ifBlank { poster },
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.45f),
                                            Color.Transparent,
                                            NhBackground
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = NhWhite
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleWatchlist() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (data.isInWatchlist) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (data.isInWatchlist) NhRed else NhWhite
                                )
                            }
                        }
                    }

                    // DESIGN-LOCKED OVERLAPPING POSTER + TITLE & METADATA PILLS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-54).dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(114.dp)
                                .height(168.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.2.dp, Color(0xFF3A3E4A), RoundedCornerShape(12.dp))
                        ) {
                            RtdbAsyncImage(
                                source = poster,
                                contentDescription = title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 56.dp)
                        ) {
                            Text(
                                text = title.uppercase(),
                                color = NhWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetadataPill(
                                    text = String.format("%.1f", rating),
                                    showStar = true
                                )
                                MetadataPill(text = quality)
                                MetadataPill(text = year.ifBlank { "—" })
                                MetadataPill(text = language)
                                MetadataPill(text = ageRating)
                            }
                        }
                    }

                    // DESIGN-LOCKED ACTION BUTTONS (PLAY NOW + DOWNLOAD)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-36).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val firstEpId = data.episodes.firstOrNull()?.id.orEmpty()
                                onPlayVideo(
                                    movie?.id ?: series?.id.orEmpty(),
                                    if (data.isSeries) "series" else "movie",
                                    firstEpId
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = NhWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PLAY NOW",
                                color = NhWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.startDownload() },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E4250)),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = NhSecondaryBg),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = NhWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DOWNLOAD",
                                color = NhWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // DESIGN-LOCKED STORY & CAST IMAGES CARD (Story + Circular Cast Photos Only)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-20).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NhCardBg)
                            .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(NhRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STORY & DETAILS",
                                color = NhWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val storyText = overview.ifBlank { "No story available." }
                        Text(
                            text = storyText,
                            color = Color(0xFFD2D5DC),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = if (storyExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (storyText.length > 110) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF3A2714))
                                        .clickable { storyExpanded = !storyExpanded }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (storyExpanded) "LESS" else "MORE",
                                        color = Color(0xFFF5A623),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (castImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Cast & Crew",
                                color = NhWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(castImages) { imgSource ->
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, NhCardBorder, CircleShape)
                                    ) {
                                        RtdbAsyncImage(
                                            source = imgSource,
                                            contentDescription = "Cast",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SEASONS & EPISODES (when Web Series)
                    if (data.isSeries && data.seasons.isNotEmpty()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(NhRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Seasons & Episodes",
                                    color = NhWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(data.seasons, key = { it.id }) { season ->
                                    val selected = season.id == data.selectedSeasonId
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.selectSeason(season.id) },
                                        label = {
                                            Text(
                                                text = season.name.ifBlank { "Season ${season.seasonNumber}" },
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NhRed,
                                            selectedLabelColor = NhWhite,
                                            containerColor = NhCardBg,
                                            labelColor = NhLightGray
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            data.episodes.forEach { ep ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NhCardBg)
                                        .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            onPlayVideo(series?.id.orEmpty(), "series", ep.id)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RtdbAsyncImage(
                                        source = ep.resolvedPoster().ifBlank { poster },
                                        contentDescription = ep.name,
                                        modifier = Modifier
                                            .width(105.dp)
                                            .height(62.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "E${ep.episodeNumber}. ${ep.name}",
                                            color = NhWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${ep.runtime} min • ${ep.quality}",
                                            color = NhLightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = "Play",
                                        tint = NhRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // DESIGN-LOCKED SIMILAR MOVIES / SERIES SECTION
                    val hasSimilar = if (data.isSeries) data.similarSeries.isNotEmpty() else data.similarMovies.isNotEmpty()
                    if (hasSimilar) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(NhRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (data.isSeries) "Similar Web Series" else "Similar Movies",
                                color = NhWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (data.isSeries) {
                                items(data.similarSeries, key = { it.id }) { s ->
                                    PosterCard(
                                        title = s.title,
                                        posterSource = s.resolvedPoster(),
                                        releaseYear = s.releaseYear,
                                        rating = s.rating,
                                        onClick = { onOpenSeries(s.id) },
                                        showRating = false
                                    )
                                }
                            } else {
                                items(data.similarMovies, key = { it.id }) { m ->
                                    PosterCard(
                                        title = m.title,
                                        posterSource = m.resolvedPoster(),
                                        releaseYear = m.releaseYear,
                                        rating = m.rating,
                                        onClick = { onOpenMovie(m.id) },
                                        showRating = false
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
private fun MetadataPill(
    text: String,
    showStar: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NhCardBg)
            .border(0.8.dp, Color(0xFF343846), RoundedCornerShape(6.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showStar) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = NhGoldStar,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = NhWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
