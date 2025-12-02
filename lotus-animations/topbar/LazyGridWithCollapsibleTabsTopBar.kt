package com.yourapp.animations.topbar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import com.yourapp.animations.animatable.rememberAnimatable
import com.yourapp.animations.utils.isSystemInLandscapeOrientation
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * LOTUS SCROLL EFFECT - LazyGrid with Collapsible Tabs Top Bar
 * ============================================================================
 * 
 * This is the main scroll effect from the Lotus music player app.
 * 
 * Features:
 * - Collapsible top bar that shrinks/expands on scroll
 * - Animated tab switching with shared element transitions
 * - Tap on title to show tab row for switching
 * - Smooth spring animations
 * - Underline indicator that animates between tabs
 * 
 * How it works:
 * 1. Scroll down → Top bar collapses smoothly
 * 2. Scroll up (at top) → Top bar expands
 * 3. Tap title → Shows horizontal tab row
 * 4. Select tab → Content switches with animation
 */

/**
 * Define your tabs here. Each tab needs a title.
 * Customize this enum for your app's needs.
 */
enum class Tab(val title: String) {
    // Example tabs - customize for your app
    Home("Home"),
    Library("Library"),
    Tracks("Tracks"),
    Albums("Albums"),
    Artists("Artists"),
    Playlists("Playlists"),
    Folders("Folders"),
    Settings("Settings")
}

/**
 * Main composable - LazyVerticalGrid with collapsible tabs top bar.
 * This creates the scroll effect like in the Lotus app screenshots.
 *
 * @param topBarTabs List of tabs to display
 * @param defaultSelectedTab The initially selected tab
 * @param onTabChange Callback when tab changes
 * @param tabTitleTextStyle Style for the main title (when collapsed to single title)
 * @param tabRowTitleTextStyle Style for titles in the tab row
 * @param topBarButtons Additional buttons to show in the top bar (like settings icon)
 * @param minTopBarHeight Minimum height when fully collapsed
 * @param maxTopBarHeight Maximum height when fully expanded
 * @param maxTopBarHeightLandscape Maximum height in landscape orientation
 * @param collapsedByDefault Whether to start collapsed
 * @param collapseFraction Callback with collapse progress (0 = expanded, 1 = collapsed)
 * @param gridState State for the LazyVerticalGrid
 * @param contentPadding Padding for the content
 * @param gridCells Grid cells configuration per tab
 * @param tabContent Content builder for each tab
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LazyGridWithCollapsibleTabsTopBar(
    topBarTabs: List<Tab>,
    defaultSelectedTab: Tab = Tab.Tracks,
    onTabChange: (tab: Tab) -> Unit = {},
    tabTitleTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    tabRowTitleTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    topBarButtons: @Composable BoxScope.(tab: Tab) -> Unit = {},
    minTopBarHeight: Dp = 60.dp,
    maxTopBarHeight: Dp = 250.dp,
    maxTopBarHeightLandscape: Dp = 150.dp,
    collapsedByDefault: Boolean = false,
    collapseFraction: (Float) -> Unit = {},
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHorizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier = Modifier,
    gridCells: (tab: Tab) -> GridCells = { GridCells.Fixed(1) },
    tabContent: LazyGridScope.(tab: Tab) -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val isInLandscapeOrientation = isSystemInLandscapeOrientation()
    val minTopBarHeightPx = remember { with(density) { minTopBarHeight.toPx() } }
    val maxTopBarHeightPx = remember {
        with(density) {
            if (isInLandscapeOrientation) {
                maxTopBarHeightLandscape.toPx()
            } else maxTopBarHeight.toPx()
        }
    }
    
    // Animatable height that smoothly transitions
    val topBarHeight = rememberAnimatable(
        initialValue = if (collapsedByDefault || isInLandscapeOrientation) {
            minTopBarHeightPx
        } else maxTopBarHeightPx
    )

    // Reset to max when orientation changes
    LaunchedEffect(isInLandscapeOrientation) {
        topBarHeight.snapTo(maxTopBarHeightPx)
    }

    // Report collapse fraction
    LaunchedEffect(topBarHeight.value) {
        collapseFraction(
            (topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)
        )
    }

    // The magic scroll connection that handles expand/collapse
    val topBarScrollConnection = remember {
        return@remember object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val previousHeight = topBarHeight.value
                val newHeight = if (gridState.firstVisibleItemIndex >= 0 && available.y < 0) {
                    // Scrolling down - collapse
                    (previousHeight + available.y).coerceIn(
                        minTopBarHeightPx,
                        maxTopBarHeightPx
                    )
                } else if (
                    gridState.firstVisibleItemIndex == 0 &&
                    gridState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset?.y == 0
                ) {
                    // At top and scrolling up - expand
                    (previousHeight + available.y).coerceIn(
                        minTopBarHeightPx,
                        maxTopBarHeightPx
                    )
                } else previousHeight

                coroutineScope.launch {
                    topBarHeight.snapTo(newHeight)
                }
                return Offset(0f, newHeight - previousHeight)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Snap to fully collapsed or expanded after fling
                coroutineScope.launch {
                    val threshold = (maxTopBarHeightPx - minTopBarHeightPx)
                    topBarHeight.animateTo(
                        targetValue = if (topBarHeight.value < threshold) minTopBarHeightPx else maxTopBarHeightPx,
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(defaultSelectedTab) }
    var showTabRow by remember { mutableStateOf(false) }
    
    val isColumnScrollInProgress by remember {
        derivedStateOf { gridState.isScrollInProgress }
    }

    // Hide tab row when scrolling starts
    LaunchedEffect(isColumnScrollInProgress) {
        if (showTabRow && isColumnScrollInProgress) {
            showTabRow = false
        }
    }

    Box(
        modifier = modifier.nestedScroll(topBarScrollConnection)
    ) {
        Column {
            // Dynamic spacer for top bar
            Spacer(
                modifier = Modifier.height(with(density) { topBarHeight.value.toDp() })
            )
            
            // Animated content switch between tabs
            AnimatedContent(
                targetState = selectedTab,
                label = "column-tab-animation",
            ) { tabIndex ->
                // Back handler to return to default tab
                if (selectedTab != defaultSelectedTab) {
                    BackHandler {
                        selectedTab = defaultSelectedTab
                        onTabChange(defaultSelectedTab)
                    }
                }

                LazyVerticalGrid(
                    columns = gridCells(tabIndex),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    horizontalArrangement = contentHorizontalArrangement,
                    verticalArrangement = contentVerticalArrangement
                ) {
                    tabContent(tabIndex)

                    // Bottom spacer for overscroll
                    item(span = { GridItemSpan(this.maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        // Top bar with title/tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { topBarHeight.value.toDp() })
        ) {
            val tabListState = rememberLazyListState()
            val viewportWidth by remember {
                derivedStateOf { tabListState.layoutInfo.viewportSize.width }
            }
            val boundTransformAnimationSpec = remember { spring<Rect>() }
            val contentAnimationSpec = remember { spring<Float>() }

            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                SharedTransitionLayout {
                    val selectedTabIndex by remember {
                        derivedStateOf { topBarTabs.indexOf(selectedTab) }
                    }
                    
                    AnimatedContent(
                        targetState = showTabRow,
                        transitionSpec = {
                            scaleIn(contentAnimationSpec, initialScale = 1.5f) +
                                    fadeIn(contentAnimationSpec) togetherWith
                                    scaleOut(contentAnimationSpec, targetScale = 1.5f) +
                                    fadeOut(contentAnimationSpec)
                        },
                        label = "top-bar-title-animation"
                    ) { state ->
                        when (state) {
                            // Show single centered title
                            false -> {
                                val listItemsCount by remember {
                                    derivedStateOf { tabListState.layoutInfo.totalItemsCount }
                                }

                                LaunchedEffect(listItemsCount) {
                                    tabListState.scrollToItem(index = selectedTabIndex + 1)
                                    tabListState.scrollToItem(
                                        index = selectedTabIndex + 1,
                                        scrollOffset = -viewportWidth / 2 + (tabListState
                                            .layoutInfo
                                            .visibleItemsInfo
                                            .fastFirstOrNull { it.index == selectedTabIndex + 1 }?.size ?: 0) / 2
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            showTabRow = true
                                        }
                                ) {
                                    TabTitle(
                                        selectedTab = selectedTab,
                                        style = tabTitleTextStyle,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedContent,
                                        boundTransformAnimationSpec = boundTransformAnimationSpec,
                                        modifier = Modifier.align(Alignment.Center)
                                    )

                                    topBarButtons(selectedTab)
                                }
                            }

                            // Show horizontal tab row
                            true -> {
                                LaunchedEffect(Unit) {
                                    tabListState.scrollToItem(index = topBarTabs.indexOf(selectedTab) + 1)
                                    tabListState.scrollToItem(
                                        index = topBarTabs.indexOf(selectedTab) + 1,
                                        scrollOffset = -viewportWidth / 2 + (tabListState
                                            .layoutInfo
                                            .visibleItemsInfo
                                            .fastFirstOrNull { it.index == topBarTabs.indexOf(selectedTab) + 1 }?.size ?: 0) / 2
                                    )
                                }

                                LazyRow(
                                    state = tabListState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            showTabRow = false
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    item {
                                        Spacer(
                                            modifier = Modifier.width(
                                                with(density) { (viewportWidth / 2.5f).toDp() }
                                            )
                                        )
                                    }

                                    itemsIndexed(
                                        items = topBarTabs,
                                        key = { index, tab -> "$index-$tab" }
                                    ) { _, tab ->
                                        TabRowTitle(
                                            selectedTab = selectedTab,
                                            tab = tab,
                                            style = tabRowTitleTextStyle,
                                            onClick = {
                                                selectedTab = tab
                                                onTabChange(tab)
                                                showTabRow = false
                                            },
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            boundTransformAnimationSpec = boundTransformAnimationSpec
                                        )
                                    }

                                    item {
                                        Spacer(
                                            modifier = Modifier.width(
                                                with(density) { (viewportWidth / 2.5f).toDp() }
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single centered tab title with underline indicator.
 * Shown when tab row is collapsed.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TabTitle(
    selectedTab: Tab,
    style: TextStyle,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundTransformAnimationSpec: FiniteAnimationSpec<Rect>,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Column(
            modifier = modifier.width(IntrinsicSize.Min)
        ) {
            Text(
                text = selectedTab.title,
                style = style,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(selectedTab),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> boundTransformAnimationSpec }
                    )
            )

            // Underline indicator
            Box(
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState("indicator"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        boundsTransform = { _, _ -> boundTransformAnimationSpec }
                    )
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(ShapeDefaults.ExtraLarge)
                    .background(color = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/**
 * Tab title in the horizontal row.
 * Shows underline only for selected tab.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TabRowTitle(
    selectedTab: Tab,
    tab: Tab,
    style: TextStyle,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundTransformAnimationSpec: FiniteAnimationSpec<Rect>,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Min)
                .heightIn(min = 60.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            key("$selectedTab-$tab") {
                Text(
                    text = tab.title,
                    style = style,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(tab),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> boundTransformAnimationSpec }
                        )
                )

                val primary = MaterialTheme.colorScheme.primary
                val color by remember {
                    mutableStateOf(
                        if (selectedTab == tab) primary else Color.Transparent
                    )
                }
                
                // Indicator line - only visible for selected tab
                Box(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                if (color != Color.Transparent) "indicator" else "invisible-indicator"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                            boundsTransform = { _, _ -> boundTransformAnimationSpec }
                        )
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(ShapeDefaults.ExtraLarge)
                        .background(color = color)
                )
            }
        }
    }
}

// ============================================================================
// USAGE EXAMPLE
// ============================================================================
/*
@Composable
fun MyScreen() {
    val tabs = listOf(Tab.Tracks, Tab.Albums, Tab.Artists, Tab.Playlists)
    
    LazyGridWithCollapsibleTabsTopBar(
        topBarTabs = tabs,
        defaultSelectedTab = Tab.Tracks,
        onTabChange = { tab ->
            // Handle tab change
        },
        topBarButtons = { tab ->
            // Add settings/filter buttons here
            IconButton(
                onClick = { /* open settings */ },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        gridCells = { tab ->
            when (tab) {
                Tab.Albums -> GridCells.Fixed(2)
                Tab.Artists -> GridCells.Fixed(3)
                else -> GridCells.Fixed(1)
            }
        }
    ) { tab ->
        when (tab) {
            Tab.Tracks -> {
                items(tracksList) { track ->
                    TrackItem(track)
                }
            }
            Tab.Albums -> {
                items(albumsList) { album ->
                    AlbumItem(album)
                }
            }
            Tab.Artists -> {
                items(artistsList) { artist ->
                    ArtistItem(artist)
                }
            }
            Tab.Playlists -> {
                items(playlistsList) { playlist ->
                    PlaylistItem(playlist)
                }
            }
            else -> {}
        }
    }
}
*/
