package com.yourapp.animations.topbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.yourapp.animations.animatable.rememberAnimatable
import kotlinx.coroutines.launch

/**
 * A Column with a collapsible top bar that shrinks/expands based on scroll.
 * The top bar smoothly animates between min and max height.
 *
 * @param topBarContent Content to display in the collapsible top bar
 * @param minTopBarHeight Minimum height when collapsed
 * @param maxTopBarHeight Maximum height when expanded
 * @param collapsedByDefault Whether to start in collapsed state
 * @param collapseFraction Callback with current collapse fraction (0 = expanded, 1 = collapsed)
 * @param content The scrollable content
 */
@Composable
fun ColumnWithCollapsibleTopBar(
    topBarContent: @Composable BoxScope.() -> Unit,
    minTopBarHeight: Dp = 60.dp,
    maxTopBarHeight: Dp = 250.dp,
    maxTopBarHeightLandscape: Dp = 150.dp,
    collapsedByDefault: Boolean = false,
    collapseFraction: (Float) -> Unit = {},
    contentScrollState: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // For landscape detection, you can use your own utility
    val isInLandscapeOrientation = false // Replace with actual landscape detection
    
    val minTopBarHeightPx = remember { with(density) { minTopBarHeight.toPx() } }
    val maxTopBarHeightPx = remember {
        with(density) {
            if (isInLandscapeOrientation) {
                maxTopBarHeightLandscape.toPx()
            } else maxTopBarHeight.toPx()
        }
    }
    val topBarHeight = rememberAnimatable(
        initialValue = if (collapsedByDefault || isInLandscapeOrientation) {
            minTopBarHeightPx
        } else maxTopBarHeightPx
    )

    LaunchedEffect(isInLandscapeOrientation) {
        if (isInLandscapeOrientation) {
            topBarHeight.snapTo(minTopBarHeightPx)
        }
    }

    LaunchedEffect(topBarHeight.value) {
        collapseFraction(
            (topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)
        )
    }

    val topBarScrollConnection = remember {
        return@remember object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val previousHeight = topBarHeight.value
                val newHeight =
                    (previousHeight + available.y - contentScrollState.value).coerceIn(
                        minTopBarHeightPx,
                        maxTopBarHeightPx
                    )
                coroutineScope.launch {
                    topBarHeight.snapTo(newHeight)
                }
                return Offset(0f, newHeight - previousHeight)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
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

    Box(
        modifier = modifier
            .nestedScroll(topBarScrollConnection)
    ) {
        Column {
            Spacer(
                modifier = Modifier
                    .height(with(density) { topBarHeight.value.toDp() })
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
                    .padding(contentPadding),
                horizontalAlignment = contentHorizontalAlignment,
                verticalArrangement = contentVerticalArrangement
            ) {
                content()

                Spacer(modifier = Modifier.height(200.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { topBarHeight.value.toDp() }),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                topBarContent()
            }
        }
    }
}

/**
 * A LazyColumn with a collapsible top bar.
 * Better for long lists as it uses lazy loading.
 */
@Composable
fun LazyColumnWithCollapsibleTopBar(
    topBarContent: @Composable BoxScope.() -> Unit,
    minTopBarHeight: Dp = 60.dp,
    maxTopBarHeight: Dp = 250.dp,
    maxTopBarHeightLandscape: Dp = 150.dp,
    collapsedByDefault: Boolean = false,
    collapseFraction: (Float) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.Top,
    enableScrollbar: Boolean = true,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val isInLandscapeOrientation = false // Replace with actual landscape detection
    
    val minTopBarHeightPx = remember { with(density) { minTopBarHeight.toPx() } }
    val maxTopBarHeightPx = remember {
        with(density) {
            if (isInLandscapeOrientation) {
                maxTopBarHeightLandscape.toPx()
            } else maxTopBarHeight.toPx()
        }
    }
    val topBarHeight = rememberAnimatable(
        initialValue = if (collapsedByDefault || isInLandscapeOrientation) {
            minTopBarHeightPx
        } else maxTopBarHeightPx
    )

    LaunchedEffect(isInLandscapeOrientation) {
        topBarHeight.snapTo(maxTopBarHeightPx)
    }

    LaunchedEffect(topBarHeight.value) {
        collapseFraction(
            (topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)
        )
    }

    val topBarScrollConnection = remember {
        return@remember object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val previousHeight = topBarHeight.value
                val newHeight = if (listState.firstVisibleItemIndex >= 0 && available.y < 0) {
                    (previousHeight + available.y).coerceIn(
                        minTopBarHeightPx,
                        maxTopBarHeightPx
                    )
                } else if (
                    listState.firstVisibleItemIndex == 0 &&
                    listState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset == 0
                ) {
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

    Box(
        modifier = modifier
            .nestedScroll(topBarScrollConnection)
    ) {
        Column {
            Spacer(
                modifier = Modifier
                    .height(with(density) { topBarHeight.value.toDp() })
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                horizontalAlignment = contentHorizontalAlignment,
                verticalArrangement = contentVerticalArrangement
            ) {
                content()

                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { topBarHeight.value.toDp() }),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                topBarContent()
            }
        }
    }
}
