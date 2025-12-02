package com.yourapp.theme

/**
 * Theme configuration and palette styles
 */
object ThemeConfig {
    
    /**
     * Appearance options for the app theme
     */
    enum class Appearance {
        System,  // Follow system dark/light mode
        Light,   // Always light mode
        Dark     // Always dark mode
    }

    /**
     * Palette styles for dynamic theming
     * These work with the MaterialKolor library for advanced color generation
     * 
     * Add to build.gradle:
     * implementation("com.materialkolor:material-kolor:1.4.0")
     */
    enum class PaletteStyle {
        TonalSpot,   // Default Material You style
        Neutral,     // Muted, desaturated colors
        Vibrant,     // Bright, saturated colors
        Expressive,  // High contrast, bold colors
        Rainbow,     // Colorful, varied palette
        FruitSalad,  // Playful, mixed colors
        Monochrome,  // Single color, grayscale-ish
        Fidelity,    // Close to source color
        Content      // Based on content colors
    }
}

/**
 * Extension functions for working with theme
 */
fun ThemeConfig.Appearance.isDark(isSystemDark: Boolean): Boolean {
    return when (this) {
        ThemeConfig.Appearance.System -> isSystemDark
        ThemeConfig.Appearance.Light -> false
        ThemeConfig.Appearance.Dark -> true
    }
}
