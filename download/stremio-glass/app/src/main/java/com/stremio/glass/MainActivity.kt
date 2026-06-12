package com.stremio.glass

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stremio.glass.data.model.Stream
import com.stremio.glass.ui.navigation.Screen
import com.stremio.glass.ui.navigation.bottomNavScreens
import com.stremio.glass.ui.screens.*
import com.stremio.glass.ui.theme.StremioGlassTheme
import com.stremio.glass.ui.components.liquidglass.NavItem
import com.stremio.glass.ui.components.liquidglass.LiquidBottomTabs
import com.stremio.glass.ui.components.liquidglass.NavItemType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StremioGlassTheme {
                StremioGlassApp()
            }
        }
    }
}

@Composable
fun StremioGlassApp() {
    val navController = rememberNavController()
    var currentTab by remember { mutableStateOf("home") }

    // Shared playback state for navigating to the player
    var pendingStream by remember { mutableStateOf<Stream?>(null) }
    var pendingMetaType by remember { mutableStateOf("") }
    var pendingMetaId by remember { mutableStateOf("") }
    var pendingVideoId by remember { mutableStateOf("") }
    var pendingVideoTitle by remember { mutableStateOf("") }

    val navItems = listOf(
        NavItem(id = "home", label = "Home", icon = androidx.compose.material.icons.Icons.Default.Home),
        NavItem(id = "discover", label = "Discover", icon = androidx.compose.material.icons.Icons.Default.Explore),
        NavItem(
            id = "search",
            label = "Search",
            icon = androidx.compose.material.icons.Icons.Default.Search,
            type = NavItemType.Search
        ),
        NavItem(id = "addons", label = "Addons", icon = androidx.compose.material.icons.Icons.Default.AddCircle),
        NavItem(id = "library", label = "Library", icon = androidx.compose.material.icons.Icons.Default.VideoLibrary)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("home") {
                    HomeScreen(
                        onMetaClick = { type, id ->
                            navController.navigate(Screen.Detail.createRoute(type, id))
                        }
                    )
                }
                composable("discover") {
                    DiscoverScreen(
                        onMetaClick = { type, id ->
                            navController.navigate(Screen.Detail.createRoute(type, id))
                        }
                    )
                }
                composable("search") {
                    SearchScreen(
                        onMetaClick = { type, id ->
                            navController.navigate(Screen.Detail.createRoute(type, id))
                        }
                    )
                }
                composable("addons") {
                    AddonsScreen()
                }
                composable("library") {
                    LibraryScreen(
                        onMetaClick = { type, id ->
                            navController.navigate(Screen.Detail.createRoute(type, id))
                        }
                    )
                }
                composable(
                    route = "detail/{metaType}/{metaId}",
                    arguments = listOf(
                        navArgument("metaType") { type = NavType.StringType },
                        navArgument("metaId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val metaType = backStackEntry.arguments?.getString("metaType") ?: "movie"
                    val metaId = backStackEntry.arguments?.getString("metaId") ?: ""

                    DetailScreen(
                        onBack = { navController.popBackStack() },
                        onPlayStream = { stream ->
                            // Stream chip clicked - pass the stream for playback
                            pendingStream = stream
                            pendingMetaType = metaType
                            pendingMetaId = metaId
                            pendingVideoId = ""
                            pendingVideoTitle = ""
                            navController.navigate("player")
                        },
                        onPlayEpisode = { epMetaType, epMetaId, videoId, videoTitle ->
                            // Episode play button clicked - player will fetch streams in background
                            pendingStream = null
                            pendingMetaType = epMetaType
                            pendingMetaId = epMetaId
                            pendingVideoId = videoId
                            pendingVideoTitle = videoTitle
                            navController.navigate("player")
                        }
                    )
                }
                composable("player") {
                    PlayerScreen(
                        pendingStream = pendingStream,
                        pendingMetaType = pendingMetaType,
                        pendingMetaId = pendingMetaId,
                        pendingVideoId = pendingVideoId,
                        pendingVideoTitle = pendingVideoTitle,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("settings") {
                    SettingsScreen()
                }
            }

            // Bottom navigation bar
            if (navController.currentDestination?.route in listOf("home", "discover", "search", "addons", "library")) {
                LiquidBottomTabs(
                    items = navItems,
                    selectedId = currentTab,
                    onItemSelected = { item ->
                        currentTab = item.id
                        navController.navigate(item.id) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onQueryChange = { query ->
                        // Real-time search query updates
                    },
                    onSearch = { query ->
                        currentTab = "search"
                        navController.navigate("search") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                )
            }
        }
    }
}
