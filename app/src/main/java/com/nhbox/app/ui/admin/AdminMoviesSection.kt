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
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import com.nhbox.app.data.model.Movie
import com.nhbox.app.ui.auth.nhFieldColors
import com.nhbox.app.ui.components.RtdbAsyncImage
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSecondaryBg
import com.nhbox.app.ui.theme.NhSuccess
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.image.compression.ImageProfile

@Composable
fun AdminMoviesSection(
    state: AdminState,
    viewModel: AdminViewModel
) {
    var search by remember { mutableStateOf("") }
    var editingMovie by remember { mutableStateOf<Movie?>(null) }

    val filtered = state.movies.filter {
        search.isBlank() || it.title.contains(search, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Movies (${filtered.size})", color = NhWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { editingMovie = Movie() },
                colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = NhWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Movie", color = NhWhite, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search movies...") },
            singleLine = true,
            colors = nhFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { movie ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NhCardBg)
                        .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RtdbAsyncImage(
                        source = movie.resolvedPoster(),
                        contentDescription = movie.title,
                        modifier = Modifier
                            .width(56.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(movie.title, color = NhWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${movie.releaseYear} • ${movie.quality} • ★ ${movie.rating}",
                            color = NhLightGray,
                            fontSize = 12.sp
                        )
                        Text(
                            if (movie.isPublished) "PUBLISHED" else "DRAFT",
                            color = if (movie.isPublished) NhSuccess else NhRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.toggleMoviePublish(movie) }) {
                        Icon(
                            if (movie.isPublished) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Publish Toggle",
                            tint = NhWhite
                        )
                    }
                    IconButton(onClick = { editingMovie = movie }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = NhWhite)
                    }
                    IconButton(onClick = { viewModel.deleteMovie(movie) }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                    }
                }
            }
        }
    }

    editingMovie?.let { initial ->
        MovieEditorDialog(
            initial = initial,
            viewModel = viewModel,
            onDismiss = { editingMovie = null },
            onSave = { updated ->
                viewModel.saveMovie(updated)
                editingMovie = null
            }
        )
    }
}

@Composable
private fun MovieEditorDialog(
    initial: Movie,
    viewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSave: (Movie) -> Unit
) {
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    var tmdbIdText by remember { mutableStateOf(if (initial.tmdbId > 0) initial.tmdbId.toString() else "") }
    var title by remember { mutableStateOf(initial.title) }
    var originalTitle by remember { mutableStateOf(initial.originalTitle) }
    var posterUrl by remember { mutableStateOf(initial.posterUrl) }
    var posterImageId by remember { mutableStateOf(initial.posterImageId) }
    var backdropUrl by remember { mutableStateOf(initial.backdropUrl) }
    var backdropImageId by remember { mutableStateOf(initial.backdropImageId) }
    var logoUrl by remember { mutableStateOf(initial.logoUrl) }
    var logoImageId by remember { mutableStateOf(initial.logoImageId) }
    var castImages by remember { mutableStateOf(initial.castImages) }
    var newCastUrl by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf(initial.overview) }
    var releaseDate by remember { mutableStateOf(initial.releaseDate) }
    var releaseYear by remember { mutableStateOf(initial.releaseYear) }
    var ratingText by remember { mutableStateOf(initial.rating.toString()) }
    var language by remember { mutableStateOf(initial.language) }
    var runtimeText by remember { mutableStateOf(initial.runtime.toString()) }
    var genresText by remember { mutableStateOf(initial.genres.joinToString(", ")) }
    var director by remember { mutableStateOf(initial.director) }
    var trailerUrl by remember { mutableStateOf(initial.trailerUrl) }
    var videoUrl by remember { mutableStateOf(initial.videoUrl) }
    var quality by remember { mutableStateOf(initial.quality) }
    var ageRating by remember { mutableStateOf(initial.ageRating) }
    var isFeatured by remember { mutableStateOf(initial.isFeatured) }
    var isPublished by remember { mutableStateOf(initial.isPublished) }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.POSTER) { posterImageId = it }
    }
    val backdropPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.BACKDROP) { backdropImageId = it }
    }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.LOGO) { logoImageId = it }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NhBackground)
        ) {
            // Sticky Top Bar
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
                        text = if (initial.id.isBlank()) "Add New Movie" else "Edit Movie",
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
                                originalTitle = originalTitle.trim(),
                                posterUrl = posterUrl.trim(),
                                posterImageId = posterImageId,
                                backdropUrl = backdropUrl.trim(),
                                backdropImageId = backdropImageId,
                                logoUrl = logoUrl.trim(),
                                logoImageId = logoImageId,
                                castImages = castImages,
                                overview = overview.trim(),
                                releaseDate = releaseDate.trim(),
                                releaseYear = releaseYear.trim(),
                                rating = ratingText.toDoubleOrNull() ?: 0.0,
                                language = language.trim(),
                                runtime = runtimeText.toIntOrNull() ?: 0,
                                genres = genresText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                director = director.trim(),
                                trailerUrl = trailerUrl.trim(),
                                videoUrl = videoUrl.trim(),
                                quality = quality.trim(),
                                ageRating = ageRating.trim(),
                                isFeatured = isFeatured,
                                isPublished = isPublished
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SAVE", color = NhWhite, fontWeight = FontWeight.ExtraBold)
                }
            }

            uploadError?.let { err ->
                Text(text = "Upload Error: $err", color = NhRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().background(NhCardBg).padding(horizontal = 16.dp, vertical = 6.dp))
            }
            uploadProgress?.let { prog ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NhCardBg)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(prog.statusText, color = NhWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Optional TMDB Import
                EditorCard("TMDB Quick Import (Optional)") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = tmdbIdText,
                            onValueChange = { tmdbIdText = it },
                            label = { Text("TMDB ID") },
                            colors = nhFieldColors(),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val id = tmdbIdText.toIntOrNull() ?: return@Button
                                viewModel.fetchMovieFromTmdb(id) { fetched ->
                                    title = fetched.title
                                    originalTitle = fetched.originalTitle
                                    posterUrl = fetched.posterUrl
                                    backdropUrl = fetched.backdropUrl
                                    logoUrl = fetched.logoUrl
                                    overview = fetched.overview
                                    releaseDate = fetched.releaseDate
                                    releaseYear = fetched.releaseYear
                                    ratingText = fetched.rating.toString()
                                    language = fetched.language
                                    runtimeText = fetched.runtime.toString()
                                    genresText = fetched.genres.joinToString(", ")
                                    director = fetched.director
                                    trailerUrl = fetched.trailerUrl
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                        ) { Text("Fetch", color = NhWhite) }
                    }
                }

                // Section 2: Media Assets (Poster, Backdrop, Logo) with Live Preview & Compressor
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
                        MediaUploadPreviewBox(
                            label = "Logo",
                            source = if (logoImageId.isNotBlank()) "rtdb://$logoImageId" else logoUrl,
                            onClick = { logoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(value = posterUrl, onValueChange = { posterUrl = it }, label = { Text("Or Poster URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = backdropUrl, onValueChange = { backdropUrl = it }, label = { Text("Or Backdrop URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                // Section 3: Cast Photos (Compressed)
                EditorCard("Cast Photos (Compressed)") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { castPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = NhWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Cast Photo", color = NhWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${castImages.size} added", color = NhLightGray, fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCastUrl,
                            onValueChange = { newCastUrl = it },
                            label = { Text("Or Cast Photo URL") },
                            colors = nhFieldColors(),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = {
                                if (newCastUrl.isNotBlank()) {
                                    castImages = castImages + newCastUrl.trim()
                                    newCastUrl = ""
                                }
                            }
                        ) { Text("Add", color = NhWhite) }
                    }

                    if (castImages.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(castImages) { idx, img ->
                                Box(modifier = Modifier.size(64.dp)) {
                                    RtdbAsyncImage(
                                        source = img,
                                        contentDescription = "Cast",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(1.dp, NhCardBorder, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(NhRed)
                                            .clickable {
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

                // Section 4: Basic Info & Story
                EditorCard("Basic Info & Story") {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Movie Title*") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = overview, onValueChange = { overview = it }, label = { Text("Story / Overview") }, colors = nhFieldColors(), minLines = 3, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = releaseYear, onValueChange = { releaseYear = it }, label = { Text("Year") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ratingText, onValueChange = { ratingText = it }, label = { Text("Rating") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = runtimeText, onValueChange = { runtimeText = it }, label = { Text("Min") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quality, onValueChange = { quality = it }, label = { Text("Quality") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Language") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ageRating, onValueChange = { ageRating = it }, label = { Text("Age") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = genresText, onValueChange = { genresText = it }, label = { Text("Genres (comma separated)") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                // Section 5: Streaming & Publishing
                EditorCard("Streaming & Publishing") {
                    OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("Video Stream URL (MP4 / HLS / DASH)") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = trailerUrl, onValueChange = { trailerUrl = it }, label = { Text("Trailer URL (optional)") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Featured on Banner", color = NhWhite, modifier = Modifier.weight(1f))
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = SwitchDefaults.colors(checkedTrackColor = NhRed))
                    }
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
fun EditorCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NhCardBg)
            .border(0.8.dp, NhCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
            Text(title, color = NhWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
fun MediaUploadPreviewBox(
    label: String,
    source: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NhSecondaryBg)
            .border(1.dp, if (source.isNotBlank()) NhSuccess else NhCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (source.isNotBlank()) {
                RtdbAsyncImage(source = source, contentDescription = label, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = label, tint = NhLightGray, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (source.isNotBlank()) "$label ✓" else "Pick $label",
            color = if (source.isNotBlank()) NhSuccess else NhWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
