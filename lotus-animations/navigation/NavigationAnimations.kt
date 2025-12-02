package com.yourapp.animations.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * Navigation animations for horizontal screen transitions.
 * Creates a smooth slide effect like iOS page navigation.
 */
object NavigationAnimations {
    /**
     * Enter transition: slide in from right
     */
    val enterTransition = slideInHorizontally(initialOffsetX = { it })
    
    /**
     * Exit transition: slide out to left
     */
    val exitTransition = slideOutHorizontally(targetOffsetX = { -it })
    
    /**
     * Pop enter transition: slide in from left (going back)
     */
    val popEnterTransition = slideInHorizontally(initialOffsetX = { -it })
    
    /**
     * Pop exit transition: slide out to right (going back)
     */
    val popExitTransition = slideOutHorizontally(targetOffsetX = { it })
}

/**
 * A NavHost with pre-configured slide animations
 */
@Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavigationAnimations.enterTransition },
        exitTransition = { NavigationAnimations.exitTransition },
        popEnterTransition = { NavigationAnimations.popEnterTransition },
        popExitTransition = { NavigationAnimations.popExitTransition },
        modifier = modifier,
        builder = builder
    )
}
