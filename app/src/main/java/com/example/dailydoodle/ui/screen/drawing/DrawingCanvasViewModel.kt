package com.example.dailydoodle.ui.screen.drawing

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.ink.brush.Brush
import androidx.ink.brush.ExperimentalInkCustomBrushApi
import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.Stroke
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.repository.PanelRepository
import com.example.dailydoodle.data.repository.StorageRepository
import com.example.dailydoodle.data.repository.UploadRepository
import com.example.dailydoodle.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Drawing Canvas screen using Jetpack Ink API.
 * Handles all drawing operations, undo/redo, brush management, and panel submission.
 */
class DrawingCanvasViewModel(
    private val storageRepository: StorageRepository = AppModule.storageRepository,
    private val panelRepository: PanelRepository = AppModule.panelRepository,
    private val uploadRepository: UploadRepository = UploadRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "DrawingCanvasViewModel"
        private const val DEFAULT_STROKE_SIZE = 5f
        private const val DEFAULT_EPSILON = 0.1f
        private const val MAX_UNDO_HISTORY = 50
    }

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    // Current brush instance (recreated when color/size changes)
    private var currentBrush: Brush? = null

    // Flows for UI binding
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _currentBrushFlow = MutableStateFlow<Brush?>(null)
    val currentBrushFlow: StateFlow<Brush?> = _currentBrushFlow.asStateFlow()

    private val _isEraserMode = MutableStateFlow(false)
    val isEraserMode: StateFlow<Boolean> = _isEraserMode.asStateFlow()

    private val _strokesFlow = MutableStateFlow<List<Stroke>>(emptyList())
    val strokesFlow: StateFlow<List<Stroke>> = _strokesFlow.asStateFlow()

    init {
        initializeDefaultBrush()
    }

    /**
     * Initialize the default brush.
     */
    @OptIn(ExperimentalInkCustomBrushApi::class)
    private fun initializeDefaultBrush() {
        currentBrush = Brush.createWithColorIntArgb(
            family = StockBrushes.marker(),
            colorIntArgb = _uiState.value.strokeColor,
            size = DEFAULT_STROKE_SIZE,
            epsilon = DEFAULT_EPSILON
        )
        _uiState.update { it.copy(selectedBrush = currentBrush) }
        _currentBrushFlow.value = currentBrush
    }

    /**
     * Get the current brush for drawing.
     */
    fun getCurrentBrush(): Brush {
        return currentBrush ?: run {
            initializeDefaultBrush()
            currentBrush!!
        }
    }

    /**
     * Handle finished strokes from InProgressStrokes.
     */
    fun onStrokesFinished(newStrokes: List<Stroke>) {
        newStrokes.forEach { stroke ->
            addStroke(stroke)
        }
    }

    /**
     * Add a new stroke to the canvas.
     */
    private fun addStroke(stroke: Stroke) {
        _uiState.update { state ->
            // Save current state to undo stack
            val newUndoStack = (state.undoStack + listOf(state.strokes))
                .takeLast(MAX_UNDO_HISTORY)
            
            val newStrokes = state.strokes + stroke
            _strokesFlow.value = newStrokes
            _canUndo.value = true
            _canRedo.value = false
            
            state.copy(
                strokes = newStrokes,
                undoStack = newUndoStack,
                redoStack = emptyList(),
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Erase stroke at the specified position.
     */
    fun erase(offsetX: Float, offsetY: Float) {
        _uiState.update { state ->
            // Find stroke at position and remove it
            val strokeToRemove = state.strokes.find { stroke ->
                // Simple hit test - check if any point is within eraser radius
                // This is a simplified version - production would use proper geometry
                isStrokeAtPosition(stroke, offsetX, offsetY, eraserRadius = 20f)
            }
            
            if (strokeToRemove != null) {
                val newUndoStack = (state.undoStack + listOf(state.strokes))
                    .takeLast(MAX_UNDO_HISTORY)
                
                val newStrokes = state.strokes - strokeToRemove
                _strokesFlow.value = newStrokes
                _canUndo.value = newUndoStack.isNotEmpty()
                
                state.copy(
                    strokes = newStrokes,
                    undoStack = newUndoStack,
                    redoStack = emptyList(),
                    hasUnsavedChanges = true
                )
            } else {
                state
            }
        }
    }

    private fun isStrokeAtPosition(stroke: Stroke, x: Float, y: Float, eraserRadius: Float): Boolean {
        // Simplified hit test - in production, use proper geometry from Ink API
        // Check the stroke's bounding box as a simple approximation
        val box = stroke.shape.computeBoundingBox() ?: return false
        return x >= box.xMin - eraserRadius && 
               x <= box.xMax + eraserRadius && 
               y >= box.yMin - eraserRadius && 
               y <= box.yMax + eraserRadius
    }

    fun startErase() {
        // Called when eraser drag starts
    }

    fun endErase() {
        // Called when eraser drag ends
    }

    /**
     * Undo the last action.
     */
    fun undo() {
        _uiState.update { state ->
            if (state.undoStack.isEmpty()) return@update state
            
            val previousStrokes = state.undoStack.last()
            val newUndoStack = state.undoStack.dropLast(1)
            val newRedoStack = state.redoStack + listOf(state.strokes)
            
            _strokesFlow.value = previousStrokes
            _canUndo.value = newUndoStack.isNotEmpty()
            _canRedo.value = true
            
            state.copy(
                strokes = previousStrokes,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Redo the last undone action.
     */
    fun redo() {
        _uiState.update { state ->
            if (state.redoStack.isEmpty()) return@update state
            
            val nextStrokes = state.redoStack.last()
            val newRedoStack = state.redoStack.dropLast(1)
            val newUndoStack = state.undoStack + listOf(state.strokes)
            
            _strokesFlow.value = nextStrokes
            _canUndo.value = true
            _canRedo.value = newRedoStack.isNotEmpty()
            
            state.copy(
                strokes = nextStrokes,
                undoStack = newUndoStack,
                redoStack = newRedoStack,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Clear all strokes from the canvas.
     */
    fun clearCanvas() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) return@update state
            
            val newUndoStack = (state.undoStack + listOf(state.strokes))
                .takeLast(MAX_UNDO_HISTORY)
            
            _strokesFlow.value = emptyList()
            _canUndo.value = true
            _canRedo.value = false
            
            state.copy(
                strokes = emptyList(),
                undoStack = newUndoStack,
                redoStack = emptyList(),
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Update the stroke color.
     */
    @OptIn(ExperimentalInkCustomBrushApi::class)
    fun setStrokeColor(color: Color) {
        val colorInt = color.toArgb()
        currentBrush = currentBrush?.let {
            Brush.createWithColorIntArgb(
                family = it.family,
                colorIntArgb = colorInt,
                size = _uiState.value.strokeSize,
                epsilon = DEFAULT_EPSILON
            )
        }
        _currentBrushFlow.value = currentBrush
        _uiState.update { it.copy(strokeColor = colorInt, selectedBrush = currentBrush) }
        
        // Disable eraser mode when selecting a color
        _isEraserMode.value = false
        _uiState.update { it.copy(isEraserEnabled = false) }
    }

    /**
     * Update the stroke size.
     */
    @OptIn(ExperimentalInkCustomBrushApi::class)
    fun setStrokeSize(size: Float) {
        currentBrush = currentBrush?.let {
            Brush.createWithColorIntArgb(
                family = it.family,
                colorIntArgb = _uiState.value.strokeColor,
                size = size,
                epsilon = DEFAULT_EPSILON
            )
        }
        _currentBrushFlow.value = currentBrush
        _uiState.update { it.copy(strokeSize = size, selectedBrush = currentBrush) }
    }

    /**
     * Set the brush family/type.
     */
    @OptIn(ExperimentalInkCustomBrushApi::class)
    fun setBrushFamily(family: androidx.ink.brush.BrushFamily) {
        currentBrush = Brush.createWithColorIntArgb(
            family = family,
            colorIntArgb = _uiState.value.strokeColor,
            size = _uiState.value.strokeSize,
            epsilon = DEFAULT_EPSILON
        )
        _currentBrushFlow.value = currentBrush
        _uiState.update { it.copy(selectedBrush = currentBrush) }
        
        // Disable eraser mode when selecting a brush
        _isEraserMode.value = false
        _uiState.update { it.copy(isEraserEnabled = false) }
    }

    /**
     * Set brush type and update brush family accordingly.
     */
    fun setBrushType(brushType: BrushType) {
        _uiState.update { it.copy(brushType = brushType) }
        setBrushFamily(brushType.toBrushFamily())
    }

    /**
     * Toggle eraser mode.
     */
    fun toggleEraser() {
        val newValue = !_isEraserMode.value
        _isEraserMode.value = newValue
        _uiState.update { it.copy(isEraserEnabled = newValue) }
    }

    /**
     * Set eraser mode.
     */
    fun setEraserEnabled(enabled: Boolean) {
        _isEraserMode.value = enabled
        _uiState.update { it.copy(isEraserEnabled = enabled) }
    }

    /**
     * Unlock premium brush via rewarded ad.
     */
    fun unlockPremiumBrush() {
        _uiState.update { it.copy(hasPremiumBrush = true) }
    }

    /**
     * Show hint overlay via rewarded ad.
     */
    fun showHintOverlay() {
        _uiState.update { it.copy(showHintOverlay = true) }
    }

    /**
     * Submit the panel to the chain.
     * Uploads to both local storage and the backend server.
     */
    fun submitPanel(
        chainId: String,
        authorId: String,
        authorName: String,
        bitmap: Bitmap,
        caption: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            
            // Upload to local storage first
            val uploadResult = storageRepository.uploadPanelImage(bitmap, authorId, chainId)
            
            if (uploadResult.isFailure) {
                _uiState.update { 
                    it.copy(
                        isUploading = false,
                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                    )
                }
                return@launch
            }

            val imageUrl = uploadResult.getOrNull() ?: run {
                _uiState.update { 
                    it.copy(isUploading = false, errorMessage = "Upload failed")
                }
                return@launch
            }

            // Also upload to backend server (for testing/sharing)
            try {
                val serverResult = uploadRepository.uploadPanel(bitmap, chainId, authorId)
                if (serverResult.isSuccess) {
                    val response = serverResult.getOrNull()
                    Log.d(TAG, "Server upload successful: ${response?.imageUrl}")
                } else {
                    Log.w(TAG, "Server upload failed (non-critical): ${serverResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                // Server upload is non-critical, log and continue
                Log.w(TAG, "Server upload error (non-critical): ${e.message}")
            }

            // Add panel to Firestore
            val panelResult = panelRepository.addPanel(
                chainId = chainId,
                authorId = authorId,
                authorName = authorName,
                imageUrl = imageUrl,
                caption = caption
            )

            if (panelResult.isSuccess) {
                com.example.dailydoodle.util.Analytics.logPanelAdded(chainId)
                _uiState.update { it.copy(isUploading = false, isSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isUploading = false,
                        errorMessage = panelResult.exceptionOrNull()?.message ?: "Failed to add panel"
                    )
                }
            }
        }
    }

    /**
     * Reset state.
     */
    fun resetState() {
        _uiState.value = DrawingUiState()
        _strokesFlow.value = emptyList()
        _canUndo.value = false
        _canRedo.value = false
        _isEraserMode.value = false
        initializeDefaultBrush()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
