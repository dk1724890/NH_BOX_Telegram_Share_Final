package com.nhbox.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.nhbox.app.R
import com.nhbox.app.data.model.CatalogItem
import com.nhbox.app.data.model.toCatalogItem
import com.nhbox.app.data.repository.ContentRepository
import com.nhbox.app.ui.admin.AdminPanelScreen
import com.nhbox.app.ui.auth.AuthViewModel
import com.nhbox.app.ui.auth.LoginScreen
import com.nhbox.app.ui.auth.RegisterScreen
import com.nhbox.app.ui.components.LoadingStateView
import com.nhbox.app.ui.components.NhBoxBottomBar
import com.nhbox.app.ui.details.MovieDetailsScreen
import com.nhbox.app.ui.downloads.OfflineDownloadsScreen
import com.nhbox.app.ui.home.CategoryViewAllScreen
import com.nhbox.app.ui.home.HomeScreen
import com.nhbox.app.ui.player.VideoPlayerScreen
import com.nhbox.app.ui.search.SearchScreen
import com.nhbox.app.ui.settings.SettingsScreen
import com.nhbox.app.ui.theme.NhBackground
import com.nhbox.app.ui.theme.NhLightGray
import com.nhbox.app.ui.theme.NhRed
import com.nhbox.app.ui.theme.NhSecondaryBg
import com.nhbox.app.ui.theme.NhWhite
import com.nhbox.app.ui.watchlist.WatchlistScreen
import com.nhbox.app.util.OfflineDownloadManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ContentRepoEntryPoint {
    fun contentRepository(): ContentRepository
}

@Composable
fun NhBoxNavGraph(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val telegramUrl = stringResource(R.string.developer_telegram)
    val navController = rememberNavController()
    val userProfile by authViewModel.userProfile.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = if (authViewModel.isSignedIn) "home" else "login"
    val bottomBarRoutes = setOf("home", "search", "watchlist", "downloads", "settings")

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val openDetails: (String, String) -> Unit = { id, type ->
        navController.navigate("details/$type/$id")
    }

    val openPlayer: (String, String, String, String) -> Unit = { id, type, epId, offlineUri ->
        val encodedPath = Uri.encode(offlineUri)
        val safeEp = epId.ifBlank { "none" }
        navController.navigate("player/$type/$id?episodeId=$safeEp&localPath=$encodedPath")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute == "home",
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = NhSecondaryBg) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = NhWhite, fontWeight = FontWeight.ExtraBold)) {
                                append("NH ")
                            }
                            withStyle(SpanStyle(color = NhRed, fontWeight = FontWeight.ExtraBold)) {
                                append("BOX")
                            }
                        },
                        fontSize = 24.sp
                    )
                    Text(
                        text = userProfile?.email ?: "Streaming Platform",
                        color = NhLightGray,
                        fontSize = 12.sp
                    )
                }
                DrawerMenuItem(Icons.Outlined.Home, "Home") {
                    scope.launch { drawerState.close() }
                    navController.navigate("home") { launchSingleTop = true }
                }
                DrawerMenuItem(Icons.Outlined.Movie, "Trending Movies") {
                    scope.launch { drawerState.close() }
                    navController.navigate("view_all/trending_movies")
                }
                DrawerMenuItem(Icons.Outlined.Tv, "Popular Web Series") {
                    scope.launch { drawerState.close() }
                    navController.navigate("view_all/popular_series")
                }
                DrawerMenuItem(Icons.Outlined.Search, "Search") {
                    scope.launch { drawerState.close() }
                    navController.navigate("search") { launchSingleTop = true }
                }
                DrawerMenuItem(Icons.Outlined.BookmarkBorder, "Watchlist") {
                    scope.launch { drawerState.close() }
                    navController.navigate("watchlist") { launchSingleTop = true }
                }
                DrawerMenuItem(Icons.Outlined.Download, "Downloads") {
                    scope.launch { drawerState.close() }
                    navController.navigate("downloads") { launchSingleTop = true }
                }
                DrawerMenuItem(Icons.Outlined.Settings, "Settings") {
                    scope.launch { drawerState.close() }
                    navController.navigate("settings") { launchSingleTop = true }
                }
                if (userProfile?.isAdmin == true) {
                    DrawerMenuItem(Icons.Outlined.AdminPanelSettings, "Admin Panel") {
                        scope.launch { drawerState.close() }
                        navController.navigate("admin")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text(
                        text = stringResource(R.string.developer_credit),
                        color = NhLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = telegramUrl,
                        color = NhRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { runCatching { uriHandler.openUri(telegramUrl) } }
                            .padding(top = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = NhBackground,
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    NhBoxBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onNavigateToLogin = { navController.popBackStack() },
                            onRegisterSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onOpenSearch = {
                                navController.navigate("search") { launchSingleTop = true }
                            },
                            onMovieClick = { id -> openDetails(id, "movie") },
                            onSeriesClick = { id -> openDetails(id, "series") },
                            onViewAllClick = { section -> navController.navigate("view_all/$section") }
                        )
                    }

                    composable("search") {
                        SearchScreen(
                            onMovieClick = { id -> openDetails(id, "movie") },
                            onSeriesClick = { id -> openDetails(id, "series") }
                        )
                    }

                    composable("watchlist") {
                        WatchlistScreen(
                            onMovieClick = { id -> openDetails(id, "movie") },
                            onSeriesClick = { id -> openDetails(id, "series") }
                        )
                    }

                    composable("downloads") {
                        val downloadsMap by OfflineDownloadManager.downloadsMap.collectAsState()
                        val isOnline by OfflineDownloadManager.observeNetworkOnline(context)
                            .collectAsState(initial = OfflineDownloadManager.isNetworkAvailable(context))
                        OfflineDownloadsScreen(
                            downloads = downloadsMap.values.toList(),
                            isDeviceOnline = isOnline,
                            onPlayOfflineItem = { entry ->
                                openPlayer(
                                    entry.itemId,
                                    if (entry.isSeries) "series" else "movie",
                                    "",
                                    entry.offlineStreamUri
                                )
                            },
                            onDeleteOfflineItem = { downloadId ->
                                OfflineDownloadManager.cancelOrDeleteDownload(context, downloadId)
                            },
                            onBrowseOnlineCatalog = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenWatchlist = { navController.navigate("watchlist") },
                            onOpenDownloads = { navController.navigate("downloads") },
                            onOpenAdminPanel = {
                                if (userProfile?.isAdmin == true) {
                                    navController.navigate("admin")
                                }
                            },
                            onLoggedOut = {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "view_all/{section}",
                        arguments = listOf(navArgument("section") { type = NavType.StringType })
                    ) { backStack ->
                        val section = backStack.arguments?.getString("section").orEmpty()
                        CategoryViewAllScreen(
                            sectionType = section,
                            onBack = { navController.popBackStack() },
                            onMovieClick = { id -> openDetails(id, "movie") },
                            onSeriesClick = { id -> openDetails(id, "series") }
                        )
                    }

                    composable(
                        route = "details/{contentType}/{contentId}",
                        arguments = listOf(
                            navArgument("contentType") { type = NavType.StringType },
                            navArgument("contentId") { type = NavType.StringType }
                        )
                    ) {
                        MovieDetailsScreen(
                            onBack = { navController.popBackStack() },
                            onPlayVideo = { id, type, epId -> openPlayer(id, type, epId, "") },
                            onOpenMovie = { id -> openDetails(id, "movie") },
                            onOpenSeries = { id -> openDetails(id, "series") }
                        )
                    }

                    composable(
                        route = "player/{contentType}/{contentId}?episodeId={episodeId}&localPath={localPath}",
                        arguments = listOf(
                            navArgument("contentType") { type = NavType.StringType },
                            navArgument("contentId") { type = NavType.StringType },
                            navArgument("episodeId") { type = NavType.StringType; defaultValue = "" },
                            navArgument("localPath") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStack ->
                        val contentId = backStack.arguments?.getString("contentId").orEmpty()
                        val contentType = backStack.arguments?.getString("contentType") ?: "movie"
                        val episodeId = backStack.arguments?.getString("episodeId").orEmpty()
                        val localPath = backStack.arguments?.getString("localPath").orEmpty()

                        var ready by remember(contentId, episodeId, localPath) { mutableStateOf(false) }
                        var playerTitle by remember { mutableStateOf("NH BOX Player") }
                        var streamUrl by remember { mutableStateOf(localPath) }
                        var currentMovieItem by remember { mutableStateOf<CatalogItem?>(null) }
                        var seriesItem by remember { mutableStateOf<CatalogItem?>(null) }
                        var similarItems by remember { mutableStateOf<List<CatalogItem>>(emptyList()) }
                        var seasonNum by remember { mutableStateOf<Int?>(null) }
                        var episodeNum by remember { mutableStateOf<Int?>(null) }

                        LaunchedEffect(contentId, episodeId, localPath) {
                            if (localPath.isNotBlank()) {
                                val entry = OfflineDownloadManager.downloadsMap.value.values
                                    .firstOrNull { it.offlineStreamUri == localPath || it.itemId == contentId }
                                playerTitle = entry?.title ?: "Offline Playback"
                                streamUrl = localPath
                                ready = true
                                return@LaunchedEffect
                            }

                            val repo = EntryPointAccessors.fromApplication(
                                context.applicationContext,
                                ContentRepoEntryPoint::class.java
                            ).contentRepository()

                            if (contentType == "series") {
                                val series = repo.getSeriesById(contentId).getOrNull()
                                val rawSeasons = repo.getSeasonsForSeries(contentId).getOrNull().orEmpty()
                                val populatedSeasons = rawSeasons.map { s ->
                                    val eps = repo.getEpisodesForSeason(contentId, s.id).getOrNull().orEmpty()
                                        .map { it.copy(seasonNumber = s.seasonNumber) }
                                    s.copy(episodes = eps)
                                }
                                val allEps = populatedSeasons.flatMap { it.episodes }
                                val targetEp = allEps.firstOrNull { it.id == episodeId } ?: allEps.firstOrNull()
                                seriesItem = series?.toCatalogItem(populatedSeasons)
                                seasonNum = targetEp?.seasonNumber
                                episodeNum = targetEp?.episodeNumber
                                playerTitle = "${series?.title.orEmpty()} • S${seasonNum ?: 1} E${episodeNum ?: 1}"
                                streamUrl = targetEp?.primaryStreamUrl.orEmpty()
                                ready = true
                            } else {
                                val movie = repo.getMovieById(contentId).getOrNull()
                                currentMovieItem = movie?.toCatalogItem()
                                similarItems = movie?.let { repo.getSimilarMovies(it).map { m -> m.toCatalogItem() } }.orEmpty()
                                playerTitle = movie?.title ?: "NH BOX Player"
                                streamUrl = movie?.videoUrl.orEmpty()
                                ready = true
                            }
                        }

                        if (!ready) {
                            LoadingStateView()
                        } else {
                            VideoPlayerScreen(
                                title = playerTitle,
                                streamUrl = streamUrl,
                                currentMovieItem = currentMovieItem,
                                similarMovies = similarItems,
                                seriesItem = seriesItem,
                                currentSeasonNumber = seasonNum,
                                currentEpisodeNumber = episodeNum,
                                onBack = { navController.popBackStack() },
                                onSwitchEpisode = { ep ->
                                    openPlayer(contentId, "series", ep.id, "")
                                },
                                onSelectSimilarMovie = { sim ->
                                    openDetails(sim.id, "movie")
                                }
                            )
                        }
                    }

                    composable("admin") {
                        if (userProfile?.isAdmin == true) {
                            AdminPanelScreen(
                                onExitToUserApp = {
                                    navController.navigate("home") {
                                        popUpTo("admin") { inclusive = true }
                                    }
                                },
                                onLoggedOut = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        } else {
                            HomeScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenSearch = { navController.navigate("search") },
                                onMovieClick = { id -> openDetails(id, "movie") },
                                onSeriesClick = { id -> openDetails(id, "series") },
                                onViewAllClick = { section -> navController.navigate("view_all/$section") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedIconColor = NhLightGray,
            unselectedTextColor = NhWhite
        ),
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
    )
}
