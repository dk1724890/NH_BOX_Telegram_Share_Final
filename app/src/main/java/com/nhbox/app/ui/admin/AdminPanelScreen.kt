package com.nhbox.app.ui.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhCardBg
import com.nhbox.app.ui.theme.NhCardBorder
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSecondaryBg
import com.nhbox.app.ui.theme.NhSuccess
import com.nhbox.app.ui.theme.NhWhite
import kotlinx.coroutines.launch

enum class AdminSection(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Outlined.Dashboard),
    MOVIES("Movies", Icons.Outlined.Movie),
    SERIES("Web Series", Icons.Outlined.Tv),
    CATEGORIES("Categories & Genres", Icons.Outlined.Category),
    BANNERS("Banners", Icons.Outlined.Slideshow),
    USERS("Users", Icons.Outlined.Group),
    VIDEO_LINKS("Video Links", Icons.Outlined.Link),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications),
    APP_SETTINGS("App Settings", Icons.Outlined.Settings)
}

@Composable
fun AdminPanelScreen(
    onExitToUserApp: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.adminState.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    var currentSection by remember { mutableStateOf(AdminSection.DASHBOARD) }
    var menuExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = NhSecondaryBg) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = NhWhite, fontWeight = FontWeight.ExtraBold)) {
                                append("NH BOX ")
                            }
                            withStyle(SpanStyle(color = NhRed, fontWeight = FontWeight.ExtraBold)) {
                                append("ADMIN")
                            }
                        },
                        fontSize = 20.sp
                    )
                    Text("Platform Control Center", color = NhLightGray, fontSize = 12.sp)
                }
                AdminSection.entries.forEach { section ->
                    NavigationDrawerItem(
                        icon = { Icon(section.icon, contentDescription = null) },
                        label = { Text(section.label, fontWeight = FontWeight.SemiBold) },
                        selected = currentSection == section,
                        onClick = {
                            currentSection = section
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = NhRed.copy(alpha = 0.2f),
                            selectedIconColor = NhRed,
                            selectedTextColor = NhWhite,
                            unselectedIconColor = NhLightGray,
                            unselectedTextColor = NhLightGray
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) },
                    label = { Text("Back to User App") },
                    selected = false,
                    onClick = onExitToUserApp,
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = NhWhite,
                        unselectedTextColor = NhWhite
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { viewModel.logout(onLoggedOut) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = NhRed,
                        unselectedTextColor = NhRed
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    ) {
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
            ) {
                // ADMIN HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NhSecondaryBg)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Admin Menu", tint = NhWhite)
                        }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = NhWhite, fontWeight = FontWeight.ExtraBold)) {
                                    append("NH BOX ")
                                }
                                withStyle(SpanStyle(color = NhRed, fontWeight = FontWeight.ExtraBold)) {
                                    append("ADMIN")
                                }
                            },
                            fontSize = 18.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentSection = AdminSection.NOTIFICATIONS }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = NhWhite)
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = NhWhite)
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                containerColor = NhCardBg
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Switch to User App", color = NhWhite) },
                                    onClick = { menuExpanded = false; onExitToUserApp() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout", color = NhRed) },
                                    onClick = { menuExpanded = false; viewModel.logout(onLoggedOut) }
                                )
                            }
                        }
                    }
                }

                uploadProgress?.let { prog ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NhCardBg)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
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

                when (currentSection) {
                    AdminSection.DASHBOARD -> AdminDashboardContent(state)
                    AdminSection.MOVIES -> AdminMoviesSection(state, viewModel)
                    AdminSection.SERIES -> AdminSeriesSection(state, viewModel)
                    AdminSection.CATEGORIES -> AdminCategoriesGenresSection(state, viewModel)
                    AdminSection.BANNERS -> AdminBannersSection(state, viewModel)
                    AdminSection.USERS -> AdminUsersSection(state, viewModel)
                    AdminSection.VIDEO_LINKS -> AdminVideoLinksSection(state)
                    AdminSection.NOTIFICATIONS -> AdminNotificationsSection(viewModel)
                    AdminSection.APP_SETTINGS -> AdminSettingsSection(state, viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminDashboardContent(state: AdminState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Dashboard Overview", color = NhWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                StatCard("Total Movies", state.stats.totalMovies.toString(), Modifier.weight(1f))
                StatCard("Total Web Series", state.stats.totalSeries.toString(), Modifier.weight(1f))
                StatCard("Categories", state.stats.totalCategories.toString(), Modifier.weight(1f))
                StatCard("Total Users", state.stats.totalUsers.toString(), Modifier.weight(1f))
                StatCard("Online Users", state.stats.onlineUsers.toString(), Modifier.weight(1f), highlight = NhSuccess)
                StatCard("Total Views", state.stats.totalViews.toString(), Modifier.weight(1f), highlight = NhRed)
            }
        }

        item {
            Text("Recently Added Movies", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            state.movies.take(4).forEach { m ->
                Text("• ${m.title} (${m.releaseYear}) — ${if (m.isPublished) "Published" else "Draft"}", color = NhLightGray, fontSize = 13.sp)
            }
        }

        item {
            Text("Recently Added Web Series", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            state.series.take(4).forEach { s ->
                Text("• ${s.title} (${s.releaseYear}) — ★ ${s.rating}", color = NhLightGray, fontSize = 13.sp)
            }
        }

        item {
            Text("Popular Content", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            (state.movies.sortedByDescending { it.viewCount }.take(3)).forEach { m ->
                Text("• ${m.title} — ${m.viewCount} views", color = NhWhite, fontSize = 13.sp)
            }
        }

        item {
            Text("Recent Admin Activity", color = NhWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(state.activityLogs.take(10), key = { it.id }) { log ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NhCardBg)
                    .border(0.8.dp, NhCardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text("${log.action}: ${log.details}", color = NhWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("By ${log.adminName}", color = NhLightGray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Color = NhWhite
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NhCardBg)
            .border(0.8.dp, NhCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text = label, color = NhLightGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = highlight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    }
}
