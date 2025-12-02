package com.example.dailydoodle.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Common animation patterns for DailyDoodle app.
 * Inspired by Lotus music player animations.
 */

// ============================================
// ANIMATION SPECS
// ============================================

/**
 * Bouncy spring for playful interactions (buttons, FAB)
 */
val BouncySpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Smooth spring for subtle transitions
 */
val SmoothSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

/**
 * Quick spring for responsive feedback
 */
val QuickSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

// ============================================
// DIALOG ANIMATIONS
// ============================================

/**
 * Dialog enter: scale up + fade in with bounce
 */
val DialogEnterTransition = scaleIn(
    initialScale = 0.85f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
) + fadeIn(
    animationSpec = tween(150)
)

/**
 * Dialog exit: scale down + fade out
 */
val DialogExitTransition = scaleOut(
    targetScale = 0.9f,
    animationSpec = tween(100)
) + fadeOut(
    animationSpec = tween(100)
)

// ============================================
// SHEET/OVERLAY ANIMATIONS
// ============================================

/**
 * Sheet enter: slide up + fade in
 */
val SheetEnterTransition = slideInVertically(
    initialOffsetY = { it / 4 },
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
) + fadeIn(
    animationSpec = tween(200)
)

/**
 * Sheet exit: slide down + fade out
 */
val SheetExitTransition = slideOutVertically(
    targetOffsetY = { it / 4 },
    animationSpec = tween(150)
) + fadeOut(
    animationSpec = tween(150)
)

// ============================================
// LIST ITEM ANIMATIONS
// ============================================

/**
 * List item enter: slide in + fade
 */
fun listItemEnterTransition(index: Int) = slideInVertically(
    initialOffsetY = { 50 },
    animationSpec = tween(
        durationMillis = 300,
        delayMillis = index * 50,
        easing = FastOutSlowInEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = 300,
        delayMillis = index * 50
    )
)

/**
 * List item exit
 */
val ListItemExitTransition = slideOutVertically(
    targetOffsetY = { -50 },
    animationSpec = tween(200)
) + fadeOut(
    animationSpec = tween(200)
)

// ============================================
// BUTTON/FAB ANIMATIONS
// ============================================

/**
 * Press scale animation modifier
 */
@Composable
fun Modifier.pressScale(
    pressed: Boolean,
    pressedScale: Float = 0.95f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Animated visibility with custom enter/exit for content
 */
@Composable
fun AnimatedSheetContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = SheetEnterTransition,
        exit = SheetExitTransition,
        modifier = modifier,
        content = content
    )
}

/**
 * Animated visibility for dialogs
 */
@Composable
fun AnimatedDialogContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = DialogEnterTransition,
        exit = DialogExitTransition,
        modifier = modifier,
        content = content
    )
}

// ============================================
// CONTENT TRANSITIONS
// ============================================

/**
 * Fade + scale content transition
 */
val FadeScaleTransition: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    fadeIn(animationSpec = tween(200)) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(200)
    ) togetherWith fadeOut(animationSpec = tween(150)) + scaleOut(
        targetScale = 1.08f,
        animationSpec = tween(150)
    )
}

/**
 * Slide + fade content transition (for tab switching)
 */
val SlideTabTransition: AnimatedContentTransitionScope<Int>.() -> ContentTransform = {
    if (targetState > initialState) {
        // Going forward
        slideInHorizontally { it / 3 } + fadeIn() togetherWith
        slideOutHorizontally { -it / 3 } + fadeOut()
    } else {
        // Going back
        slideInHorizontally { -it / 3 } + fadeIn() togetherWith
        slideOutHorizontally { it / 3 } + fadeOut()
    }
}

// ============================================
// COMPOSABLE HELPERS
// ============================================

/**
 * A composable that animates its appearance when first shown
 */
@Composable
fun AnimateOnFirstAppear(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Staggered animation for list items
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 50).toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Saveable Animatable that survives config changes
 */
@Composable
fun rememberAnimatable(
    initialValue: Float,
    visibilityThreshold: Float = Spring.DefaultDisplacementThreshold
): Animatable<Float, AnimationVector1D> {
    return remember {
        Animatable(initialValue, visibilityThreshold)
    }
}

// ============================================
// ROTATION ANIMATIONS
// ============================================

/**
 * Continuous rotation animation
 */
@Composable
fun rememberContinuousRotation(
    durationMillis: Int = 1000,
    enabled: Boolean = true
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (enabled) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_rotation"
    )
    return if (enabled) rotation else 0f
}

/**
 * Pulse animation (for loading states, attention)
 */
@Composable
fun rememberPulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    return scale
}
