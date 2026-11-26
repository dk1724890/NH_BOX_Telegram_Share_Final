package com.nhbox.app.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.auth.nhFieldColors
import com.nhbox.app.ui.components.EmptyStateView
import com.nhbox.app.ui.components.PosterCard
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhWhite

@Composable
fun SearchScreen(
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NhBackground)
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Search",
            color = NhWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("Search movies, web series, cast...", color = NhLightGray) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
            shape = RoundedCornerShape(14.dp),
            colors = nhFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Genre Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.availableGenres) { genre ->
                FilterChip(
                    selected = state.selectedGenre == genre,
                    onClick = { viewModel.selectGenre(genre) },
                    label = { Text(genre, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NhRed,
                        selectedLabelColor = NhWhite,
                        containerColor = NhCardBg,
                        labelColor = NhLightGray
                    )
                )
            }
        }

        // Language & Year Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.availableLanguages) { lang ->
                FilterChip(
                    selected = state.selectedLanguage == lang,
                    onClick = { viewModel.selectLanguage(lang) },
                    label = { Text("Lang: $lang", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NhRed,
                        selectedLabelColor = NhWhite,
                        containerColor = NhCardBg,
                        labelColor = NhLightGray
                    )
                )
            }
            items(state.availableYears) { yr ->
                FilterChip(
                    selected = state.selectedYear == yr,
                    onClick = { viewModel.selectYear(yr) },
                    label = { Text("Year: $yr", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NhRed,
                        selectedLabelColor = NhWhite,
                        containerColor = NhCardBg,
                        labelColor = NhLightGray
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.query.isBlank() && state.recentSearches.isNotEmpty()) {
                item {
                    Text("Recent Searches", color = NhWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    state.recentSearches.forEach { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onQueryChange(term) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null, tint = NhLightGray)
                            Text(
                                text = term,
                                color = NhLightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }

            if (state.movies.isEmpty() && state.series.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Matches Found",
                        subtitle = "Try adjusting your search query or filters.",
                        modifier = Modifier.height(300.dp)
                    )
                }
            }

            if (state.movies.isNotEmpty()) {
                item {
                    Text("Movies (${state.movies.size})", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.movies, key = { it.id }) { m ->
                            PosterCard(
                                title = m.title,
                                posterSource = m.resolvedPoster(),
                                releaseYear = m.releaseYear,
                                rating = m.rating,
                                onClick = {
                                    viewModel.submitSearch()
                                    onMovieClick(m.id)
                                }
                            )
                        }
                    }
                }
            }

            if (state.series.isNotEmpty()) {
                item {
                    Text("Web Series (${state.series.size})", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.series, key = { it.id }) { s ->
                            PosterCard(
                                title = s.title,
                                posterSource = s.resolvedPoster(),
                                releaseYear = s.releaseYear,
                                rating = s.rating,
                                onClick = {
                                    viewModel.submitSearch()
                                    onSeriesClick(s.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
