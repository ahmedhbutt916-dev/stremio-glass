package com.stremio.glass.ui.components.liquidglass

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stremio.glass.ui.theme.*

/**
 * Navigation item types for the bottom bar.
 */
enum class NavItemType {
    Standard,  // Regular tab
    Search     // Expandable search tab
}

/**
 * Data class representing a bottom navigation tab.
 */
data class NavItem(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val type: NavItemType = NavItemType.Standard
)

/**
 * Liquid Glass Bottom Navigation Bar with expandable search.
 * Follows the Kyant0 LiquidBottomTabs design:
 * - Full-width capsule bar with glass effect
 * - Floating pill indicator for selected tab
 * - Expandable search bar on the search tab
 * - Spring physics animations for tab transitions
 */
@Composable
fun LiquidBottomTabs(
    items: List<NavItem>,
    selectedId: String,
    onItemSelected: (NavItem) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Animate selected indicator position
    val indicatorOffset by animateDpAsState(
        targetValue = if (isSearchExpanded) {
            // When search is expanded, indicator goes to search tab
            val searchIdx = items.indexOfFirst { it.type == NavItemType.Search }.coerceAtLeast(0)
            (searchIdx * 72).dp
        } else {
            (selectedIndex * 72).dp
        },
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val searchExpandedWidth by animateDpAsState(
        targetValue = if (isSearchExpanded) 220.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        )
    )

    val searchBarAlpha by animateFloatAsState(
        targetValue = if (isSearchExpanded) 1f else 0f,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Background bar - full glass capsule
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            cornerRadius = 32.dp,
            blurRadius = 10.dp,
            tintColor = GlassTint.copy(alpha = 0.6f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Selected tab indicator pill
                LiquidGlassSurface(
                    modifier = Modifier
                        .offset(x = indicatorOffset + 8.dp)
                        .size(56.dp, 48.dp)
                        .align(Alignment.CenterStart),
                    cornerRadius = 24.dp,
                    blurRadius = 4.dp,
                    tintColor = AccentPrimary.copy(alpha = 0.25f),
                    borderColor = AccentPrimary.copy(alpha = 0.3f)
                ) {}

                // Tab items row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = item.id == selectedId && !isSearchExpanded ||
                                (isSearchExpanded && item.type == NavItemType.Search)

                        when (item.type) {
                            NavItemType.Search -> {
                                // Search tab with expandable bar
                                SearchTabItem(
                                    item = item,
                                    isSelected = isSelected || isSearchExpanded,
                                    isExpanded = isSearchExpanded,
                                    searchQuery = searchQuery,
                                    searchBarAlpha = searchBarAlpha,
                                    searchExpandedWidth = searchExpandedWidth,
                                    focusRequester = focusRequester,
                                    onTabClick = {
                                        if (isSearchExpanded) {
                                            isSearchExpanded = false
                                            searchQuery = ""
                                            onQueryChange("")
                                        } else {
                                            isSearchExpanded = true
                                            onItemSelected(item)
                                        }
                                    },
                                    onQueryChange = { query ->
                                        searchQuery = query
                                        onQueryChange(query)
                                    },
                                    onSearch = { query ->
                                        onSearch(query)
                                    }
                                )
                            }
                            NavItemType.Standard -> {
                                StandardTabItem(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = {
                                        isSearchExpanded = false
                                        searchQuery = ""
                                        onItemSelected(item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto-focus search when expanded
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            kotlinx.coroutines.delay(200)
            focusRequester.requestFocus()
        }
    }
}

@Composable
private fun RowScope.StandardTabItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AccentPrimary else TextTertiary,
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
        AnimatedVisibility(visible = isSelected) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = iconColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RowScope.SearchTabItem(
    item: NavItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    searchQuery: String,
    searchBarAlpha: Float,
    searchExpandedWidth: Dp,
    focusRequester: FocusRequester,
    onTabClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AccentPrimary else TextTertiary,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Search icon (always visible, becomes close when expanded)
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTabClick
                )
        )

        // Expandable search input field
        if (searchBarAlpha > 0.01f) {
            LiquidGlassSurface(
                modifier = Modifier
                    .width(searchExpandedWidth)
                    .height(40.dp)
                    .offset(x = 24.dp)
                    .focusRequester(focusRequester),
                cornerRadius = 20.dp,
                blurRadius = 6.dp,
                tintColor = GlassTint.copy(alpha = 0.8f)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    placeholder = {
                        Text(
                            "Search...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary.copy(alpha = searchBarAlpha)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch(searchQuery) }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary.copy(alpha = searchBarAlpha),
                        unfocusedTextColor = TextPrimary.copy(alpha = searchBarAlpha),
                        cursorColor = AccentPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = TextPrimary.copy(alpha = searchBarAlpha)
                    )
                )
            }
        }
    }
}
