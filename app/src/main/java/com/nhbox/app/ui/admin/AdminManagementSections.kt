package com.nhbox.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhbox.app.data.model.AppNotification
import com.nhbox.app.data.model.Banner
import com.nhbox.app.data.model.Category
import com.nhbox.app.data.model.Genre
import com.nhbox.app.ui.auth.nhFieldColors
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSuccess
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.utils.image.compression.ImageProfile

@Composable
fun AdminCategoriesGenresSection(state: AdminState, viewModel: AdminViewModel) {
    var catName by remember { mutableStateOf("") }
    var catOrder by remember { mutableStateOf("1") }
    var genreName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Categories", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Category Name") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = catOrder, onValueChange = { catOrder = it }, label = { Text("Order") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.width(72.dp))
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.saveCategory(Category(name = catName.trim(), order = catOrder.toIntOrNull() ?: 0))
                            catName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                ) { Text("Add", color = NhWhite) }
            }
        }

        items(state.categories, key = { it.id }) { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${cat.order} ${cat.name}", color = NhWhite, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = cat.isEnabled,
                    onCheckedChange = { viewModel.saveCategory(cat.copy(isEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedTrackColor = NhRed)
                )
                IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Genres", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = genreName, onValueChange = { genreName = it }, label = { Text("Genre Name") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (genreName.isNotBlank()) {
                            viewModel.saveGenre(Genre(name = genreName.trim()))
                            genreName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                ) { Text("Add", color = NhWhite) }
            }
        }

        items(state.genres, key = { it.id }) { genre ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(genre.name, color = NhWhite, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.deleteGenre(genre) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                }
            }
        }
    }
}

@Composable
fun AdminBannersSection(state: AdminState, viewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageId by remember { mutableStateOf("") }
    var targetId by remember { mutableStateOf("") }
    var orderText by remember { mutableStateOf("1") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.uploadImage(uri, ImageProfile.BANNER) { imageId = it }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Home Banners", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Banner Title") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (imageId.isNotBlank()) "Banner Uploaded ✓" else "Pick Compressed Banner", color = NhWhite)
                }
                OutlinedTextField(value = orderText, onValueChange = { orderText = it }, label = { Text("Order") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.width(80.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Or Banner Image URL") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = targetId, onValueChange = { targetId = it }, label = { Text("Target Movie/Series ID") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveBanner(
                            Banner(
                                title = title.trim(),
                                imageUrl = imageUrl.trim(),
                                imageId = imageId,
                                targetId = targetId.trim(),
                                order = orderText.toIntOrNull() ?: 1
                            )
                        )
                        title = ""; imageUrl = ""; imageId = ""; targetId = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NhRed)
            ) { Text("Save Banner", color = NhWhite) }
        }

        items(state.banners, key = { it.id }) { b ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("#${b.order} ${b.title}", color = NhWhite, fontWeight = FontWeight.Bold)
                    Text("Target: ${b.targetId.ifBlank { "None" }}", color = NhLightGray, fontSize = 12.sp)
                }
                Switch(
                    checked = b.isEnabled,
                    onCheckedChange = { viewModel.saveBanner(b.copy(isEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedTrackColor = NhRed)
                )
                IconButton(onClick = { viewModel.deleteBanner(b) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                }
            }
        }
    }
}

@Composable
fun AdminUsersSection(state: AdminState, viewModel: AdminViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Registered Users (${state.users.size})", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        items(state.users, key = { it.uid }) { user ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.username.ifBlank { "User" }, color = NhWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(user.email, color = NhLightGray, fontSize = 12.sp)
                        Text("UID: ${user.uid}", color = NhLightGray, fontSize = 10.sp)
                    }
                    Text(
                        text = if (user.isOnline) "ONLINE" else "OFFLINE",
                        color = if (user.isOnline) NhSuccess else NhLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Role: ${user.role.uppercase()} • Status: ${user.accountStatus.uppercase()}",
                        color = NhWhite,
                        fontSize = 12.sp
                    )
                    Row {
                        IconButton(onClick = {
                            val nextStatus = if (user.accountStatus == "active") "suspended" else "active"
                            viewModel.updateUserStatus(user.uid, nextStatus, user.role)
                        }) {
                            Icon(
                                if (user.accountStatus == "active") Icons.Rounded.Block else Icons.Rounded.CheckCircle,
                                contentDescription = "Toggle Status",
                                tint = if (user.accountStatus == "active") NhRed else NhSuccess
                            )
                        }
                        IconButton(onClick = { viewModel.deleteUserRecord(user.uid) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = NhRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVideoLinksSection(state: AdminState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Configured Video Links", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        items(state.movies, key = { it.id }) { m ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(m.title, color = NhWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(m.videoUrl.ifBlank { "No video URL set" }, color = NhLightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AdminNotificationsSection(viewModel: AdminViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var targetId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Publish Notification", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Notification Title") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, colors = nhFieldColors(), minLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = targetId, onValueChange = { targetId = it }, label = { Text("Target Movie/Series ID (optional)") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                if (title.isNotBlank() && message.isNotBlank()) {
                    viewModel.publishNotification(
                        AppNotification(
                            title = title.trim(),
                            message = message.trim(),
                            targetContentId = targetId.trim()
                        )
                    )
                    title = ""; message = ""; targetId = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NhRed),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Publish Notification", color = NhWhite, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun AdminSettingsSection(state: AdminState, viewModel: AdminViewModel) {
    var maxKb by remember(state.appSettings) { mutableStateOf(state.appSettings.maxImageSizeKb.toString()) }
    var maintenance by remember(state.appSettings) { mutableStateOf(state.appSettings.maintenanceMode) }
    var tmdbEnabled by remember(state.appSettings) { mutableStateOf(state.appSettings.tmdbEnabled) }
    var supportEmail by remember(state.appSettings) { mutableStateOf(state.appSettings.supportEmail) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Platform Settings", color = NhWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = maxKb, onValueChange = { maxKb = it }, label = { Text("Max Compressed Image Size (KB)") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = supportEmail, onValueChange = { supportEmail = it }, label = { Text("Support Email") }, colors = nhFieldColors(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Maintenance Mode", color = NhWhite, modifier = Modifier.weight(1f))
            Switch(checked = maintenance, onCheckedChange = { maintenance = it }, colors = SwitchDefaults.colors(checkedTrackColor = NhRed))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TMDB API Integration Enabled", color = NhWhite, modifier = Modifier.weight(1f))
            Switch(checked = tmdbEnabled, onCheckedChange = { tmdbEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = NhRed))
        }
        Button(
            onClick = {
                viewModel.saveAppSettings(
                    state.appSettings.copy(
                        maxImageSizeKb = maxKb.toIntOrNull() ?: 180,
                        maintenanceMode = maintenance,
                        tmdbEnabled = tmdbEnabled,
                        supportEmail = supportEmail.trim()
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = NhRed),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Platform Settings", color = NhWhite, fontWeight = FontWeight.Bold) }
    }
}
