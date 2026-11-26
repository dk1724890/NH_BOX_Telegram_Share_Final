package com.nhbox.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.BuildConfig
import com.nhbox.app.R
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.nhbox.app.data.model.UserPreferences
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

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val history by viewModel.watchHistory.collectAsState()
    val message by viewModel.message.collectAsState()
    val isUploading by viewModel.isUploadingAvatar.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs = profile?.preferences ?: UserPreferences()
    val creditText = stringResource(R.string.developer_credit)
    val telegramUrl = stringResource(R.string.developer_telegram)
    val uriHandler = LocalUriHandler.current

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAccountInfo by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var optionDialog by remember {
        mutableStateOf<Triple<String, List<String>, (String) -> Unit>?>(null)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.uploadProfileAvatar(uri)
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = NhBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NhBackground)
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = NhWhite
                        )
                    }
                    Text(
                        text = "Settings",
                        color = NhWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (profile?.isAdmin == true) {
                    OutlinedButton(
                        onClick = onOpenAdminPanel,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NhRed),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            tint = NhRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ADMIN", color = NhWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // PROFILE SECTION
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NhCardBg)
                    .border(1.dp, NhCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(NhSecondaryBg)
                        .border(1.5.dp, NhRed, CircleShape)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = NhRed, modifier = Modifier.size(24.dp))
                    } else {
                        val avatar = profile?.profileImageId?.takeIf { it.isNotBlank() }?.let { "rtdb://$it" }
                            ?: profile?.profileImageUrl.orEmpty()
                        RtdbAsyncImage(
                            source = avatar,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile?.username?.ifBlank { "NH BOX User" } ?: "NH BOX User",
                        color = NhWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile?.email.orEmpty(),
                        color = NhLightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${(profile?.accountStatus ?: "active").uppercase()}",
                        color = NhSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { showEditProfile = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit", color = NhWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ACCOUNT
            SettingsSectionHeader("ACCOUNT")
            SettingsCard {
                SettingsActionRow(Icons.Outlined.Person, "My Profile", profile?.username ?: "View") {
                    showAccountInfo = true
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Person, "Edit Profile", "Name & Photo") {
                    showEditProfile = true
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Lock, "Change Password", "Update password") {
                    showChangePassword = true
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Info, "Account Information", profile?.role?.uppercase() ?: "USER") {
                    showAccountInfo = true
                }
                SettingsDivider()
                SettingsActionRow(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = "Logout",
                    subtitle = "Sign out securely",
                    isDestructive = true
                ) {
                    showLogoutConfirm = true
                }
            }

            // APP SETTINGS
            SettingsSectionHeader("APP SETTINGS")
            SettingsCard {
                SettingsSwitchRow(Icons.Outlined.DarkMode, "Dark Theme", prefs.darkTheme) { checked ->
                    viewModel.updatePreferences { it.copy(darkTheme = checked) }
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Language, "Language", prefs.language) {
                    optionDialog = Triple(
                        "App Language",
                        listOf("English", "Hindi", "Bengali", "Tamil", "Telugu")
                    ) { choice ->
                        viewModel.updatePreferences { it.copy(language = choice) }
                    }
                }
                SettingsDivider()
                SettingsSwitchRow(Icons.Outlined.PlayCircle, "Auto Play", prefs.autoPlay) { checked ->
                    viewModel.updatePreferences { it.copy(autoPlay = checked) }
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.HighQuality, "Video Quality", prefs.videoQuality) {
                    optionDialog = Triple(
                        "Video Quality",
                        listOf("Auto", "4K UHD", "1080p", "720p", "480p")
                    ) { choice ->
                        viewModel.updatePreferences { it.copy(videoQuality = choice) }
                    }
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.HighQuality, "Default Streaming Quality", prefs.defaultStreamingQuality) {
                    optionDialog = Triple(
                        "Default Streaming Quality",
                        listOf("4K UHD", "1080p", "720p", "480p")
                    ) { choice ->
                        viewModel.updatePreferences { it.copy(defaultStreamingQuality = choice) }
                    }
                }
                SettingsDivider()
                SettingsSwitchRow(Icons.Outlined.Download, "Auto Download (Wi-Fi Only)", prefs.autoDownloadWifiOnly) { checked ->
                    viewModel.updatePreferences { it.copy(autoDownloadWifiOnly = checked) }
                }
            }

            // PLAYBACK
            SettingsSectionHeader("PLAYBACK")
            SettingsCard {
                SettingsActionRow(Icons.Outlined.GraphicEq, "Preferred Audio Language", prefs.preferredAudioLanguage) {
                    optionDialog = Triple(
                        "Preferred Audio Language",
                        listOf("Hindi", "English", "Bengali", "Tamil", "Original")
                    ) { choice ->
                        viewModel.updatePreferences { it.copy(preferredAudioLanguage = choice) }
                    }
                }
                SettingsDivider()
                SettingsSwitchRow(Icons.Outlined.Subtitles, "Subtitle Settings", prefs.subtitlesEnabled) { checked ->
                    viewModel.updatePreferences { it.copy(subtitlesEnabled = checked) }
                }
                SettingsDivider()
                SettingsSwitchRow(Icons.Outlined.PlayCircle, "Auto Next Episode", prefs.autoNextEpisode) { checked ->
                    viewModel.updatePreferences { it.copy(autoNextEpisode = checked) }
                }
            }

            // MY LIBRARY
            SettingsSectionHeader("MY LIBRARY")
            SettingsCard {
                SettingsActionRow(Icons.Outlined.BookmarkBorder, "My Watchlist", "Saved movies & series") {
                    onOpenWatchlist()
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.History, "Watch History & Continue Watching", "${history.size} watched") {
                    showHistoryDialog = true
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Download, "Downloads", "Manage offline videos") {
                    onOpenDownloads()
                }
            }

            // SUPPORT
            SettingsSectionHeader("SUPPORT")
            SettingsCard {
                SettingsActionRow(Icons.Outlined.Info, "About NH BOX", "Premium Streaming App") {
                    showInfoDialog = "About NH BOX" to "NH BOX is a premium movie and web-series streaming platform.\n\n$creditText\nTelegram: $telegramUrl"
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Policy, "Privacy Policy", "View policy") {
                    showInfoDialog = "Privacy Policy" to "Your profile, watchlist, and playback progress are stored securely under your authenticated Firebase UID and never shared with unauthorized third parties."
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Policy, "Terms & Conditions", "View terms") {
                    showInfoDialog = "Terms & Conditions" to "Streaming and downloads are permitted only for authorized media links configured by platform administrators."
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.SupportAgent, "Contact Support", "support@nhbox.app") {
                    showInfoDialog = "Contact Support" to "Need help? Email us at support@nhbox.app with your registered account email."
                }
                SettingsDivider()
                SettingsActionRow(Icons.Outlined.Info, "App Version", "v${BuildConfig.VERSION_NAME}") {}
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = creditText,
                color = NhLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = telegramUrl,
                color = NhRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { runCatching { uriHandler.openUri(telegramUrl) } }
                    .padding(top = 4.dp, bottom = 24.dp)
            )
        }
    }

    if (showEditProfile) {
        var newName by remember { mutableStateOf(profile?.username.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            containerColor = NhCardBg,
            title = { Text("Edit Profile", color = NhWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Username") },
                    colors = nhFieldColors(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfileName(newName)
                        showEditProfile = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                ) { Text("Save", color = NhWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel", color = NhLightGray) }
            }
        )
    }

    if (showChangePassword) {
        var currentPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePassword = false },
            containerColor = NhCardBg,
            title = { Text("Change Password", color = NhWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = nhFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("New Password (6+ chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = nhFieldColors(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changePassword(currentPwd, newPwd)
                        showChangePassword = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                ) { Text("Update", color = NhWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassword = false }) { Text("Cancel", color = NhLightGray) }
            }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = NhCardBg,
            title = { Text("Confirm Logout", color = NhWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of NH BOX?", color = NhLightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout(onLoggedOut)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NhRed)
                ) { Text("Logout", color = NhWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel", color = NhLightGray) }
            }
        )
    }

    if (showAccountInfo) {
        AlertDialog(
            onDismissRequest = { showAccountInfo = false },
            containerColor = NhCardBg,
            title = { Text("Account Information", color = NhWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Username: ${profile?.username ?: "—"}", color = NhWhite, fontSize = 13.sp)
                    Text("Email: ${profile?.email ?: "—"}", color = NhWhite, fontSize = 13.sp)
                    Text("UID: ${profile?.uid ?: "—"}", color = NhLightGray, fontSize = 12.sp)
                    Text("Role: ${profile?.role?.uppercase() ?: "USER"}", color = NhRed, fontSize = 13.sp)
                    Text("Status: ${profile?.accountStatus?.uppercase() ?: "ACTIVE"}", color = NhSuccess, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountInfo = false }) { Text("Close", color = NhRed) }
            }
        )
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            containerColor = NhCardBg,
            title = { Text("Watch History", color = NhWhite, fontWeight = FontWeight.Bold) },
            text = {
                if (history.isEmpty()) {
                    Text("No watch history yet.", color = NhLightGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.take(8).forEach { item ->
                            val mins = (item.positionMs / 60000L).coerceAtLeast(0)
                            Text("• ${item.title} (${mins}m watched)", color = NhWhite, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Close", color = NhRed) }
            }
        )
    }

    showInfoDialog?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            containerColor = NhCardBg,
            title = { Text(title, color = NhWhite, fontWeight = FontWeight.Bold) },
            text = { Text(body, color = NhLightGray, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = null }) { Text("OK", color = NhRed) }
            }
        )
    }

    optionDialog?.let { (title, options, onSelect) ->
        AlertDialog(
            onDismissRequest = { optionDialog = null },
            containerColor = NhCardBg,
            title = { Text(title, color = NhWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    options.forEach { opt ->
                        Text(
                            text = opt,
                            color = NhWhite,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(opt)
                                    optionDialog = null
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { optionDialog = null }) { Text("Cancel", color = NhLightGray) }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(NhRed)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = NhLightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NhCardBg)
            .border(0.8.dp, NhCardBorder, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = NhCardBorder, thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 14.dp))
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) NhRed else NhLightGray,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = if (isDestructive) NhRed else NhWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = subtitle,
            color = NhLightGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = NhLightGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NhLightGray,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = NhWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NhWhite,
                checkedTrackColor = NhRed,
                uncheckedThumbColor = NhLightGray,
                uncheckedTrackColor = NhSecondaryBg
            )
        )
    }
}
