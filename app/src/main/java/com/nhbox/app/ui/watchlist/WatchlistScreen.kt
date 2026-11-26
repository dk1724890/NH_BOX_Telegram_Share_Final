package com.nhbox.app.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.components.EmptyStateView
import com.nhbox.app.ui.components.ErrorStateView
import com.nhbox.app.ui.components.LoadingStateView
import com.nhbox.app.ui.components.RtdbAsyncImage
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.UiState

@Composable
fun WatchlistScreen(
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NhBackground)
            .statusBarsPadding()
    ) {
        Text(
            text = "My Watchlist",
            color = NhWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        when (val state = uiState) {
            is UiState.Loading -> LoadingStateView()
            is UiState.Empty -> EmptyStateView("Watchlist Empty", state.message)
            is UiState.NetworkError -> ErrorStateView(state.message, true, { viewModel.loadWatchlist() })
            is UiState.DatabaseError -> ErrorStateView(state.message, false, { viewModel.loadWatchlist() })
            is UiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data, key = { it.contentId }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NhCardBg)
                                .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (item.contentType == "series") onSeriesClick(item.contentId)
                                    else onMovieClick(item.contentId)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RtdbAsyncImage(
                                source = item.resolvedPoster(),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = NhWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${item.contentType.uppercase()} • ${item.releaseYear} • ★ ${String.format("%.1f", item.rating)}",
                                    color = NhLightGray,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { viewModel.remove(item.contentId) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remove",
                                    tint = NhRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
