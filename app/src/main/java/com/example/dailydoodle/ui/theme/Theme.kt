package com.example.dailydoodle.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.dailydoodle.ui.screen.settings.ColorPalette

// Dynamic palette color schemes
private val DynamicLightColorScheme = lightColorScheme(
    primary = DynamicPrimary,
    secondary = DynamicSecondary,
    tertiary = DynamicTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

private val DynamicDarkColorScheme = darkColorScheme(
    primary = DynamicPrimary,
    secondary = DynamicSecondary,
    tertiary = DynamicTertiary,
)

// Default palette color schemes
private val DefaultLightColorScheme = lightColorScheme(
    primary = DefaultPrimary,
    secondary = DefaultSecondary,
    tertiary = DefaultTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

private val DefaultDarkColorScheme = darkColorScheme(
    primary = DefaultPrimary,
    secondary = DefaultSecondary,
    tertiary = DefaultTertiary,
)

// Zestful palette color schemes
private val ZestfulLightColorScheme = lightColorScheme(
    primary = ZestfulPrimary,
    secondary = ZestfulSecondary,
    tertiary = ZestfulTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

private val ZestfulDarkColorScheme = darkColorScheme(
    primary = ZestfulPrimary,
    secondary = ZestfulSecondary,
    tertiary = ZestfulTertiary,
)

// Serene palette color schemes
private val SereneLightColorScheme = lightColorScheme(
    primary = SerenePrimary,
    secondary = SereneSecondary,
    tertiary = SereneTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
)

private val SereneDarkColorScheme = darkColorScheme(
    primary = SerenePrimary,
    secondary = SereneSecondary,
    tertiary = SereneTertiary,
)

@Composable
fun DailyDoodleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPalette: ColorPalette = ColorPalette.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = when (colorPalette) {
        ColorPalette.Dynamic -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DynamicDarkColorScheme else DynamicLightColorScheme
            }
        }
        ColorPalette.Default -> if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
        ColorPalette.Zestful -> if (darkTheme) ZestfulDarkColorScheme else ZestfulLightColorScheme
        ColorPalette.Serene -> if (darkTheme) SereneDarkColorScheme else SereneLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}