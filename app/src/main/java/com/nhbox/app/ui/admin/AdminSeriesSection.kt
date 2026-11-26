package com.nhbox.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nhbox.app.data.model.Episode
import com.nhbox.app.data.model.Season
import com.nhbox.app.data.model.TvSeries
import com.nhbox.app.ui.auth.nhFieldColors
import com.nhbox.app.ui.components.RtdbAsyncImage
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSecondaryBg
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.image.compression.ImageProfile

@Composable
fun AdminSeriesSection(
    state: AdminState,
    viewModel: AdminViewModel
) {
    var editingSeries by remember { mutableStateOf<TvSeries?>(null) }
    var managingSeries by remember { mutableStateOf<TvSeries?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Web Series (${state.series.size})", color = NhWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { editingSeries = TvSeries() },
                colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = NhWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Series", color = NhWhite, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.series, key = { it.id }) { series ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NhCardBg)
                        .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.loadSeasons(series.id)
                            managingSeries = series
                        }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RtdbAsyncImage(
                        source = series.resolvedPoster(),
                        contentDescription = series.title,
                        modifier = Modifier
                            .width(56.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(series.title, color = NhWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${series.releaseYear} • ★ ${series.rating} • Tap for Seasons", color = NhLightGray, fontSize = 12.sp)
                    }
                    IconButton(onClick = { editingSeries = series }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = NhWhite)
                    }
                    IconButton(onClick = { viewModel.deleteSeries(series) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                    }
                }
            }
        }
    }

    editingSeries?.let { initial ->
        SeriesEditorDialog(
            initial = initial,
            viewModel = viewModel,
            onDismiss = { editingSeries = null },
            onSave = { updated ->
                viewModel.saveSeries(updated)
                editingSeries = null
            }
        )
    }

    managingSeries?.let { series ->
        SeasonsAndEpisodesDialog(
            series = series,
            viewModel = viewModel,
            onDismiss = { managingSeries = null }
        )
    }
}

@Composable
private fun SeriesEditorDialog(
    initial: TvSeries,
    viewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSave: (TvSeries) -> Unit
) {
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    var tmdbIdText by remember { mutableStateOf(if (initial.tmdbId > 0) initial.tmdbId.toString() else "") }
    var title by remember { mutableStateOf(initial.title) }
    var posterUrl by remember { mutableStateOf(initial.posterUrl) }
    var posterImageId by remember { mutableStateOf(initial.posterImageId) }
    var backdropUrl by remember { mutableStateOf(initial.backdropUrl) }
    var backdropImageId by remember { mutableStateOf(initial.backdropImageId) }
    var castImages by remember { mutableStateOf(initial.castImages) }
    var newCastUrl by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf(initial.overview) }
    var releaseYear by remember { mutableStateOf(initial.releaseYear) }
    var ratingText by remember { mutableStateOf(initial.rating.toString()) }
    var language by remember { mutableStateOf(initial.language) }
    var genresText by remember { mutableStateOf(initial.genres.joinToString(", ")) }
    var director by remember { mutableStateOf(initial.director) }
    var status by remember { mutableStateOf(initial.status) }
    var isPublished by remember { mutableStateOf(initial.isPublished) }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.POSTER) { posterImageId = it }
    }
    val backdropPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.BACKDROP) { backdropImageId = it }
    }
    val castPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.uploadImage(uri, ImageProfile.PROFILE) { imageId ->
                castImages = castImages + "rtdb://$imageId"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(NhBackground)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NhSecondaryBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = NhWhite)
                    }
                    Text(
                        text = if (initial.id.isBlank()) "Add Web Series" else "Edit Web Series",
                        color = NhWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        onSave(
                            initial.copy(
                                tmdbId = tmdbIdText.toIntOrNull() ?: 0,
                                title = title.trim(),
                                posterUrl = posterUrl.trim(),
                                posterImageId = posterImageId,
                                backdropUrl = backdropUrl.trim(),
                                backdropImageId = backdropImageId,
                                castImages = castImages,
                                overview = overview.trim(),
                                releaseYear = releaseYear.trim(),
                                rating = ratingText.toDoubleOrNull() ?: 0.0,
                                language = language.trim(),
                                genres = genresText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                director = director.trim(),
                                status = status.trim(),
                                isPublished = isPublished
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("SAVE", color = NhWhite, fontWeight = FontWeight.ExtraBold) }
            }

            uploadError?.let { err ->
                Text(text = "Upload Error: $err", color = NhRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().background(NhCardBg).padding(horizontal = 16.dp, vertical = 6.dp))
            }
            uploadProgress?.let { prog ->
                Column(modifier = Modifier.fillMaxWidth().background(NhCardBg).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(prog.statusText, color = NhWhite, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (prog.compressionProgress * 0.5f + prog.uploadProgress * 0.5f).coerceIn(0f, 1f) },
                        color = NhRed,
                        trackColor = NhCardBorder,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EditorCard("TMDB Quick Import (Optional)") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = tmdbIdText,
                            onValueChange = { tmdbIdText = it },
                            label = { Text("TMDB TV ID") },
                            colors = nhFieldColors(),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val id = tmdbIdText.toIntOrNull() ?: return@Button
                                viewModel.fetchSeriesFromTmdb(id) { fetched, _ ->
                                    title = fetched.title
                                    posterUrl = fetched.posterUrl
                                    backdropUrl = fetched.backdropUrl
                                    overview = fetched.overview
                                    releaseYear = fetched.releaseYear
                                    ratingText = fetched.rating.toString()
                                    language = fetched.language
                                    genresText = fetched.genres.joinToString(", ")
                                    director = fetched.director
                                    status = fetched.status
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                        ) { Text("Fetch", color = NhWhite) }
                    }
                }

                EditorCard("Media Assets (Compressed WebP → RTDB)") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MediaUploadPreviewBox(
                            label = "Poster",
                            source = if (posterImageId.isNotBlank()) "rtdb://$posterImageId" else posterUrl,
                            onClick = { posterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        )
                        MediaUploadPreviewBox(
                            label = "Backdrop",
                            source = if (backdropImageId.isNotBlank()) "rtdb://$backdropImageId" else backdropUrl,
                            onClick = { backdropPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(value = posterUrl, onValueChange = { posterUrl = it }, label = { Text("Or Poster URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = backdropUrl, onValueChange = { backdropUrl = it }, label = { Text("Or Backdrop URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                EditorCard("Cast Photos (Compressed)") {
                    Button(
                        onClick = { castPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = NhWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Cast Photo", color = NhWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = newCastUrl, onValueChange = { newCastUrl = it }, label = { Text("Or Cast Photo URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            if (newCastUrl.isNotBlank()) {
                                castImages = castImages + newCastUrl.trim()
                                newCastUrl = ""
                            }
                        }) { Text("Add", color = NhWhite) }
                    }
                    if (castImages.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(castImages) { idx, img ->
                                Box(modifier = Modifier.size(64.dp)) {
                                    RtdbAsyncImage(
                                        source = img,
                                        contentDescription = "Cast",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.dp, NhCardBorder, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd).clip(CircleShape).background(NhRed).clickable {
                                            castImages = castImages.filterIndexed { i, _ -> i != idx }
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = NhWhite, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                EditorCard("Series Info & Publishing") {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Series Title*") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = overview, onValueChange = { overview = it }, label = { Text("Story / Overview") }, colors = nhFieldColors(), minLines = 3, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = releaseYear, onValueChange = { releaseYear = it }, label = { Text("Year") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ratingText, onValueChange = { ratingText = it }, label = { Text("Rating") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = genresText, onValueChange = { genresText = it }, label = { Text("Genres") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Publish Immediately", color = NhWhite, modifier = Modifier.weight(1f))
                        Switch(checked = isPublished, onCheckedChange = { isPublished = it }, colors = SwitchDefaults.colors(checkedTrackColor = NhRed))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SeasonsAndEpisodesDialog(
    series: TvSeries,
    viewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    val seasons by viewModel.seasons.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    var selectedSeasonId by remember { mutableStateOf("") }
    var newSeasonName by remember { mutableStateOf("") }
    var newEpName by remember { mutableStateOf("") }
    var newEpUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NhCardBg,
        title = { Text("${series.title} — Seasons & Episodes", color = NhWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSeasonName,
                        onValueChange = { newSeasonName = it },
                        label = { Text("New Season Name") },
                        colors = nhFieldColors(),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newSeasonName.isNotBlank()) {
                                viewModel.saveSeason(
                                    Season(
                                        seriesId = series.id,
                                        seasonNumber = seasons.size + 1,
                                        name = newSeasonName.trim()
                                    )
                                )
                                newSeasonName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                    ) { Text("Add", color = NhWhite) }
                }

                seasons.forEach { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSeasonId == s.id) NhRed.copy(alpha = 0.25f) else NhCardBorder.copy(alpha = 0.3f))
                            .clickable {
                                selectedSeasonId = s.id
                                viewModel.loadEpisodes(series.id, s.id)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("S${s.seasonNumber}: ${s.name}", color = NhWhite, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.deleteSeason(series.id, s.id) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                        }
                    }
                }

                if (selectedSeasonId.isNotBlank()) {
                    Text("Episodes", color = NhWhite, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = newEpName, onValueChange = { newEpName = it }, label = { Text("Episode Title") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newEpUrl, onValueChange = { newEpUrl = it }, label = { Text("Episode Video URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (newEpName.isNotBlank()) {
                                viewModel.saveEpisode(
                                    Episode(
                                        seriesId = series.id,
                                        seasonId = selectedSeasonId,
                                        episodeNumber = episodes.size + 1,
                                        name = newEpName.trim(),
                                        videoUrl = newEpUrl.trim()
                                    )
                                )
                                newEpName = ""; newEpUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                    ) { Text("Add Episode", color = NhWhite) }

                    episodes.forEach { ep ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("E${ep.episodeNumber}. ${ep.name}", color = NhLightGray, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteEpisode(series.id, selectedSeasonId, ep) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = NhRed) } }
    )
}
