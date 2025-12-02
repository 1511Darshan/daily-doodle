package com.yourapp.animations.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * Common animation patterns used throughout the Lotus app.
 * Ready to use in your Compose projects.
 */

// ============================================
// ENTER/EXIT ANIMATIONS
// ============================================

/**
 * Fade + slide from bottom animation for sheets/dialogs
 */
val SheetEnterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
        slideInVertically(initialOffsetY = { it / 10 })

val SheetExitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
        slideOutVertically(targetOffsetY = { it / 10 })

/**
 * Horizontal expand/collapse for buttons
 */
val HorizontalExpandAnimation = expandHorizontally()
val HorizontalShrinkAnimation = shrinkHorizontally()

/**
 * Vertical expand/collapse
 */
val VerticalExpandAnimation = expandVertically()
val VerticalShrinkAnimation = shrinkVertically()

/**
 * Simple fade animations
 */
val FadeInAnimation = fadeIn()
val FadeOutAnimation = fadeOut()

/**
 * Slide from right (for navigation)
 */
val SlideInFromRight = slideInHorizontally(initialOffsetX = { it })
val SlideOutToLeft = slideOutHorizontally(targetOffsetX = { -it })

/**
 * Slide from left (for back navigation)
 */
val SlideInFromLeft = slideInHorizontally(initialOffsetX = { -it })
val SlideOutToRight = slideOutHorizontally(targetOffsetX = { it })

// ============================================
// ANIMATION SPECS
// ============================================

/**
 * Linear progress animation (for progress bars)
 */
fun linearProgressSpec(durationMillis: Int = 1000) = 
    tween<Float>(durationMillis = durationMillis, easing = LinearEasing)

/**
 * Bouncy spring animation
 */
fun bouncySpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Smooth spring animation
 */
fun smoothSpring() = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

// ============================================
// COMPOSABLE HELPERS
// ============================================

/**
 * An animated progress bar that smoothly animates width changes
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = linearProgressSpec(),
        label = "progress-animation"
    )
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(color = MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * Content that animates when visibility changes with sheet animation
 */
@Composable
fun AnimatedSheetContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = SheetEnterAnimation,
        exit = SheetExitAnimation,
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Content that animates when state changes with fade + slide
 */
@Composable
fun <T> AnimatedStateContent(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn() + slideInHorizontally { it / 5 } togetherWith 
            fadeOut() + slideOutHorizontally { -it / 5 }
        },
        modifier = modifier,
        label = "state-content-animation"
    ) { state ->
        content(state)
    }
}

/**
 * Content that fades between states
 */
@Composable
fun <T> FadingContent(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier,
        label = "fading-content-animation"
    ) { state ->
        content(state)
    }
}
