package com.example.dailydoodle.ui.screen.settings

import androidx.compose.ui.graphics.Color

/**
 * Color palette options with preview colors.
 * 
 * Each palette shows 3 colors in the preview:
 * - primary (top-left)
 * - secondary (top-right)  
 * - tertiary (bottom)
 */
enum class ColorPalette(
    val displayName: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
) {
    Dynamic(
        displayName = "Dynamic",
        primary = Color(0xFF8B9DC3),      // Blue-ish
        secondary = Color(0xFFB8C5D6),    // Light blue
        tertiary = Color(0xFFD4A5D9),     // Pink/purple
    ),
    Default(
        displayName = "Default",
        primary = Color(0xFF8BC6A8),      // Green (top-left)
        secondary = Color(0xFFB8D4C8),    // Light green (top-right)
        tertiary = Color(0xFFF5C28D),     // Orange (bottom)
    ),
    Zestful(
        displayName = "Zestful",
        primary = Color(0xFFD4D98C),      // Yellow-green (top-left)
        secondary = Color(0xFFC5C9A0),    // Muted sage (top-right)
        tertiary = Color(0xFF8B8B6A),     // Olive (bottom)
    ),
    Serene(
        displayName = "Serene",
        primary = Color(0xFFB8A5D9),      // Purple (top-left)
        secondary = Color(0xFFD4C5E8),    // Light purple (top-right)
        tertiary = Color(0xFFE8A5D4),     // Pink (bottom)
    ),
}
