package com.example.dailydoodle.ui.screen.drawing

import androidx.annotation.DrawableRes
import androidx.ink.brush.BrushFamily

/**
 * Data class representing a custom brush loaded from resources.
 */
data class CustomBrush(
    val name: String,
    @DrawableRes val icon: Int,
    val brushFamily: BrushFamily
)
