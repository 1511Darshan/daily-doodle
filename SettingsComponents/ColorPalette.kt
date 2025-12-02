package com.example.app.ui.settings.model

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
        primary = Color(0xFF8BC6A8),      // Green
        secondary = Color(0xFFB8D4C8),    // Light green
        tertiary = Color(0xFFF5C28D),     // Orange
    ),
    Zestful(
        displayName = "Zestful",
        primary = Color(0xFFC5C67E),      // Yellow-green (selected in screenshot)
        secondary = Color(0xFFB8C4A0),    // Muted green
        tertiary = Color(0xFF8B8B7A),     // Gray-green
    ),
    Serene(
        displayName = "Serene",
        primary = Color(0xFFB8A5D9),      // Purple
        secondary = Color(0xFFD4C5E8),    // Light purple
        tertiary = Color(0xFFE8A5D4),     // Pink
    ),
}
