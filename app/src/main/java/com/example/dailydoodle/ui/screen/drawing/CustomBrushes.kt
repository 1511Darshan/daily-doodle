package com.example.dailydoodle.ui.screen.drawing

import android.content.Context
import android.util.Log
import androidx.annotation.ColorInt
import androidx.ink.brush.Brush
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.ExperimentalInkCustomBrushApi
import androidx.ink.brush.StockBrushes
import androidx.ink.storage.decode
import com.example.dailydoodle.R

/**
 * Custom brush configurations for the drawing canvas.
 * Loads custom brushes from raw resources and provides stock brushes.
 */
@OptIn(ExperimentalInkCustomBrushApi::class)
object CustomBrushes {
    private var customBrushes: List<CustomBrush>? = null
    private const val TAG = "CustomBrushes"

    /**
     * Get all custom brushes. Brushes are loaded lazily and cached.
     */
    fun getBrushes(context: Context): List<CustomBrush> {
        return customBrushes ?: synchronized(this) {
            customBrushes ?: loadCustomBrushes(context).also { customBrushes = it }
        }
    }

    /**
     * Load custom brushes from raw resources.
     * Each brush is stored as a .gz file in res/raw.
     */
    private fun loadCustomBrushes(context: Context): List<CustomBrush> {
        val brushFiles = mapOf(
            "Calligraphy" to (R.raw.calligraphy to R.drawable.ic_calligraphy),
            "Flag Banner" to (R.raw.flag_banner to R.drawable.ic_flag),
            "Graffiti" to (R.raw.graffiti to R.drawable.ic_graffiti),
            "Groovy" to (R.raw.groovy to R.drawable.ic_groovy),
            "Holiday lights" to (R.raw.holiday_lights to R.drawable.ic_holiday_lights),
            "Lace" to (R.raw.lace to R.drawable.ic_lace),
            "Music" to (R.raw.music to R.drawable.ic_music),
            "Shadow" to (R.raw.shadow to R.drawable.ic_shadow),
            "Twisted yarn" to (R.raw.twisted_yarn to R.drawable.ic_line_weight),
            "Wet paint" to (R.raw.wet_paint to R.drawable.ic_wet_paint)
        )

        val loadedBrushes = brushFiles.mapNotNull { (name, pair) ->
            val (resourceId, icon) = pair
            try {
                val brushFamily = context.resources.openRawResource(resourceId).use { inputStream ->
                    BrushFamily.decode(inputStream)
                }
                CustomBrush(name, icon, brushFamily.copy(clientBrushFamilyId = name))
            } catch (e: Exception) {
                Log.e(TAG, "Error loading custom brush $name", e)
                null
            }
        }
        return loadedBrushes
    }

    /**
     * Creates a brush with the specified color and size.
     */
    fun createBrush(
        family: BrushFamily,
        @ColorInt color: Int,
        size: Float,
        epsilon: Float = DEFAULT_EPSILON
    ): Brush {
        return Brush.createWithColorIntArgb(
            family = family,
            colorIntArgb = color,
            size = size,
            epsilon = epsilon
        )
    }

    /**
     * Stock brush types available in the app.
     */
    enum class StockBrushType(
        val displayName: String,
        val brushFamily: BrushFamily,
        val iconResId: Int,
        val defaultSize: Float = 5f
    ) {
        PEN(
            displayName = "Pen",
            brushFamily = StockBrushes.pressurePen(),
            iconResId = R.drawable.ic_pen,
            defaultSize = 5f
        ),
        MARKER(
            displayName = "Marker",
            brushFamily = StockBrushes.marker(),
            iconResId = R.drawable.ic_marker,
            defaultSize = 8f
        ),
        HIGHLIGHTER(
            displayName = "Highlighter",
            brushFamily = StockBrushes.highlighter(),
            iconResId = R.drawable.ic_highlighter,
            defaultSize = 15f
        ),
        DASHED_LINE(
            displayName = "Dashed line",
            brushFamily = StockBrushes.marker(), // Use marker as base for dashed
            iconResId = R.drawable.ic_dashed_line,
            defaultSize = 5f
        )
    }

    /**
     * Common stroke sizes available in the size picker.
     */
    val strokeSizes = listOf(2f, 5f, 8f, 12f, 16f, 24f, 32f)

    /**
     * Default epsilon value for brush creation.
     * Epsilon affects the smoothing of strokes.
     */
    const val DEFAULT_EPSILON = 0.1f
}
