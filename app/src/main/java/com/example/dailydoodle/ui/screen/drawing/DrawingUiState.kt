package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.ui.graphics.Color
import androidx.ink.brush.Brush
import androidx.ink.strokes.Stroke

/**
 * Represents the complete UI state for the Drawing canvas.
 * This immutable data class holds all state needed to render the drawing UI.
 */
data class DrawingUiState(
    /** List of all strokes currently on the canvas */
    val strokes: List<Stroke> = emptyList(),
    
    /** Currently selected brush for drawing */
    val selectedBrush: Brush? = null,
    
    /** Current brush type */
    val brushType: BrushType = BrushType.MARKER,
    
    /** Current stroke size/width in pixels */
    val strokeSize: Float = 5f,
    
    /** Current stroke color as ARGB integer */
    val strokeColor: Int = 0xFF000000.toInt(),
    
    /** Whether eraser mode is enabled */
    val isEraserEnabled: Boolean = false,
    
    /** Stack of stroke states for undo functionality */
    val undoStack: List<List<Stroke>> = emptyList(),
    
    /** Stack of stroke states for redo functionality */
    val redoStack: List<List<Stroke>> = emptyList(),
    
    /** Whether the canvas has unsaved changes */
    val hasUnsavedChanges: Boolean = false,
    
    /** Whether the panel is currently being uploaded */
    val isUploading: Boolean = false,
    
    /** Whether upload was successful */
    val isSuccess: Boolean = false,
    
    /** Error message to display, if any */
    val errorMessage: String? = null,
    
    /** Whether the user has unlocked premium brush via rewarded ad */
    val hasPremiumBrush: Boolean = false,
    
    /** Whether to show hint overlay (unlocked via rewarded ad) */
    val showHintOverlay: Boolean = false
) {
    /**
     * Whether undo is available (has history to go back to).
     */
    val canUndo: Boolean
        get() = undoStack.isNotEmpty()
    
    /**
     * Whether redo is available (has undone actions to restore).
     */
    val canRedo: Boolean
        get() = redoStack.isNotEmpty()
    
    /**
     * Total number of strokes on the canvas.
     */
    val strokeCount: Int
        get() = strokes.size
    
    /**
     * Whether the canvas is empty (no strokes).
     */
    val isEmpty: Boolean
        get() = strokes.isEmpty()
}

/**
 * Enum for brush types available in the drawing screen.
 */
enum class BrushType {
    MARKER,
    PEN,
    HIGHLIGHTER,
    DASHED
}

/**
 * Represents the standard colors available for drawing.
 */
object DrawingColors {
    val standardColors = listOf(
        Color.Black,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan
    )
    
    val premiumColors = listOf(
        Color(0xFFFF6B6B), // Coral
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Sky Blue
        Color(0xFFFFA07A), // Light Salmon
        Color(0xFF98D8C8), // Mint
        Color(0xFFF7DC6F), // Gold
        Color(0xFFBB8FCE)  // Lavender
    )
}
