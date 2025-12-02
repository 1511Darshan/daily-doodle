package com.yourapp.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for working with colors and theme
 */

/**
 * Get the appropriate content color for a surface color
 */
@Composable
fun Color.contentColor(): Color {
    return if (this.luminance() > 0.5f) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surface
    }
}

/**
 * Calculate the luminance of a color (0 = dark, 1 = light)
 */
fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}

/**
 * Darken a color by a factor
 */
fun Color.darken(factor: Float = 0.1f): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Lighten a color by a factor
 */
fun Color.lighten(factor: Float = 0.1f): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceIn(0f, 1f),
        green = (green + (1 - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Create a semi-transparent version of a color
 */
fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

/**
 * Check if we should use dark content colors on this background
 */
fun Color.shouldUseDarkContent(): Boolean {
    return luminance() > 0.5f
}

/**
 * Get harmonized colors for lyrics sheet
 * Based on whether dark palette is used
 */
@Composable
fun getLyricsColors(
    useDarkPalette: Boolean = false,
    amoledDarkTheme: Boolean = false
): Pair<Color, Color> {
    val isDarkTheme = isSystemInDarkTheme()
    
    val containerColor = when {
        useDarkPalette && !isDarkTheme -> MaterialTheme.colorScheme.primary
        else -> {
            if (!amoledDarkTheme) {
                MaterialTheme.colorScheme.inversePrimary
            } else {
                MaterialTheme.colorScheme.background
            }
        }
    }
    
    val contentColor = when {
        useDarkPalette && !isDarkTheme -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.inverseSurface
    }
    
    return containerColor to contentColor
}

/**
 * Common color combinations used in the app
 */
object AppColors {
    // Player colors
    val PlayerBackground @Composable get() = MaterialTheme.colorScheme.surface
    val PlayerSurface @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val PlayerPrimary @Composable get() = MaterialTheme.colorScheme.primary
    
    // Progress bar colors  
    val ProgressFilled @Composable get() = MaterialTheme.colorScheme.primary
    val ProgressBackground @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    
    // Text colors
    val TextPrimary @Composable get() = MaterialTheme.colorScheme.onSurface
    val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextAccent @Composable get() = MaterialTheme.colorScheme.primary
    
    // Card colors
    val CardBackground @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val CardBorder @Composable get() = MaterialTheme.colorScheme.outlineVariant
}
