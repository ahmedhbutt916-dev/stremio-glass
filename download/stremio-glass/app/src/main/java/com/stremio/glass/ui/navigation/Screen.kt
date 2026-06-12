package com.stremio.glass.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Discover : Screen("discover", "Discover", Icons.Default.Explore)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Addons : Screen("addons", "Addons", Icons.Default.AddCircle)
    data object Library : Screen("library", "Library", Icons.Default.VideoLibrary)

    data object Detail : Screen("detail/{metaType}/{metaId}", "Detail", Icons.Default.Info) {
        fun createRoute(type: String, id: String) = "detail/$type/$id"
    }
    data object Player : Screen("player", "Player", Icons.Default.PlayArrow) {
        fun createRoute() = "player"
    }
    data object Settings : Screen("settings", "Settings", Icons.Default.Home)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Discover,
    Screen.Search,
    Screen.Addons,
    Screen.Library
)
