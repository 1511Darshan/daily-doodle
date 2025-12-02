package com.yourapp.animations.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * A draggable player sheet that can expand/collapse with swipe gestures.
 * Features:
 * - Drag up to expand, drag down to collapse
 * - Natural decay-based fling animation
 * - Animated corner radius during drag
 * - Reset on swipe down when collapsed (to dismiss playback)
 *
 * @param isExpanded Whether the player is currently expanded
 * @param onExpandedChange Callback when expansion state changes
 * @param onReset Callback when player is swiped away (stop playback)
 * @param collapsedContent Content to show when collapsed (mini player)
 * @param expandedContent Content to show when expanded (full player)
 */
@Composable
fun DraggablePlayerSheet(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    collapsedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val thresholdY = with(density) { 200.dp.toPx() }
    val decay = rememberSplineBasedDecay<Float>()
    
    val translationY = remember {
        Animatable(0f).apply {
            if (isExpanded) {
                updateBounds(
                    lowerBound = 0f,
                    upperBound = thresholdY
                )
            } else {
                updateBounds(
                    lowerBound = -thresholdY,
                    upperBound = thresholdY
                )
            }
        }
    }
    
    val draggableState = rememberDraggableState { dragAmount ->
        coroutineScope.launch {
            translationY.snapTo(
                translationY.value + (dragAmount * (1 - (translationY.value / thresholdY).absoluteValue))
            )
        }
    }

    AnimatedContent(
        targetState = isExpanded,
        label = "player-content",
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.translationY = translationY.value

                val cornerRadius =
                    lerp(0.dp, 36.dp, this.translationY.absoluteValue / (thresholdY * 0.5f))
                this.clip = true
                this.shape = if (isExpanded) {
                    RoundedCornerShape(cornerRadius)
                } else {
                    RoundedCornerShape(
                        topStart = 36.dp,
                        topEnd = 36.dp,
                        bottomStart = cornerRadius,
                        bottomEnd = cornerRadius
                    )
                }
            }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    val decayY = decay.calculateTargetValue(
                        initialValue = translationY.value,
                        initialVelocity = velocity
                    )

                    coroutineScope.launch {
                        if (!isExpanded) {
                            val shouldStopPlayback = decayY > thresholdY * .5f
                            if (shouldStopPlayback) {
                                onReset()
                                return@launch
                            }
                        }

                        val shouldChangeExpandedState = decayY.absoluteValue > (thresholdY * 0.5f)
                        if (shouldChangeExpandedState) {
                            onExpandedChange(!isExpanded)
                            translationY.apply {
                                animateTo(0f)
                                if (isExpanded) {
                                    updateBounds(
                                        lowerBound = 0f,
                                        upperBound = thresholdY
                                    )
                                } else {
                                    updateBounds(
                                        lowerBound = -thresholdY,
                                        upperBound = thresholdY
                                    )
                                }
                            }
                        } else {
                            translationY.animateTo(0f)
                        }
                    }
                }
            )
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) { state ->
        when (state) {
            false -> collapsedContent()
            true -> expandedContent()
        }
    }
}

/**
 * Common animation specs for content transitions
 */
object PlayerAnimations {
    
    /**
     * Fade + slide transition for track changes
     */
    fun trackChangeTransition() = fadeIn() + slideInHorizontally(
        initialOffsetX = { it / 5 }
    ) togetherWith fadeOut() + slideOutHorizontally(
        targetOffsetX = { -it / 5 }
    )
    
    /**
     * Fade transition for title/artist text changes
     */
    fun textChangeTransition() = fadeIn() togetherWith fadeOut()
    
    /**
     * Sheet entrance animation (for lyrics, queue, etc.)
     */
    fun sheetEnterTransition() = fadeIn(
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    ) + slideInVertically(initialOffsetY = { it / 10 })
    
    /**
     * Sheet exit animation
     */
    fun sheetExitTransition() = fadeOut(
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    ) + slideOutVertically(targetOffsetY = { it / 10 })
    
    /**
     * Horizontal expand animation (for buttons)
     */
    fun horizontalExpand() = expandHorizontally()
    
    /**
     * Horizontal shrink animation (for buttons)
     */
    fun horizontalShrink() = shrinkHorizontally()
    
    /**
     * Progress bar animation spec
     */
    fun progressAnimation() = tween<Float>(durationMillis = 1000, easing = LinearEasing)
}

/**
 * Example of how to use AnimatedVisibility with sheet animations
 */
@Composable
fun AnimatedSheet(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = PlayerAnimations.sheetEnterTransition(),
        exit = PlayerAnimations.sheetExitTransition(),
        modifier = modifier
    ) {
        content()
    }
}
