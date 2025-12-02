package com.example.dailydoodle.ui.screen.drawing

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.ui.geometry.Offset
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
        private const val ERASER_STROKE_WIDTH = 30f // Width of eraser path
    }

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    // Current brush instance (recreated when color/size changes)
    private var currentBrush: Brush? = null
    
    // Eraser path points collected during erasing
    private val eraserPathPoints = mutableListOf<Offset>()
    
    // Flow for eraser path visualization
    private val _eraserPath = MutableStateFlow<List<Offset>>(emptyList())
    val eraserPath: StateFlow<List<Offset>> = _eraserPath.asStateFlow()

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
     * Add eraser point during drag.
     * Points are collected and strokes are erased when drag ends.
     */
    fun erase(offsetX: Float, offsetY: Float) {
        eraserPathPoints.add(Offset(offsetX, offsetY))
        _eraserPath.value = eraserPathPoints.toList()
    }

    /**
     * Check if a stroke intersects with the eraser path.
     */
    private fun strokeIntersectsEraserPath(stroke: Stroke, eraserPath: Path): Boolean {
        val strokeBox = stroke.shape.computeBoundingBox() ?: return false
        
        // Create a region from the eraser path
        val eraserBounds = RectF()
        eraserPath.computeBounds(eraserBounds, true)
        
        // Expand bounds for intersection check
        val eraserRegion = Region()
        eraserRegion.setPath(
            eraserPath,
            Region(
                eraserBounds.left.toInt() - 10,
                eraserBounds.top.toInt() - 10,
                eraserBounds.right.toInt() + 10,
                eraserBounds.bottom.toInt() + 10
            )
        )
        
        // Create a region from the stroke bounding box
        val strokeRegion = Region(
            strokeBox.xMin.toInt(),
            strokeBox.yMin.toInt(),
            strokeBox.xMax.toInt(),
            strokeBox.yMax.toInt()
        )
        
        // Check if regions intersect
        return !eraserRegion.quickReject(strokeRegion) && eraserRegion.op(strokeRegion, Region.Op.INTERSECT)
    }

    /**
     * Called when eraser drag starts.
     */
    fun startErase() {
        eraserPathPoints.clear()
        _eraserPath.value = emptyList()
    }

    /**
     * Called when eraser drag ends.
     * This is where we actually remove the intersecting strokes.
     */
    fun endErase() {
        if (eraserPathPoints.size < 2) {
            eraserPathPoints.clear()
            _eraserPath.value = emptyList()
            return
        }
        
        // Build a Path from eraser points with stroke width
        val eraserPath = Path().apply {
            val firstPoint = eraserPathPoints.first()
            moveTo(firstPoint.x, firstPoint.y)
            
            for (i in 1 until eraserPathPoints.size) {
                val point = eraserPathPoints[i]
                lineTo(point.x, point.y)
            }
        }
        
        // Create a stroked version of the path for wider hit detection
        val strokedPath = Path()
        val paint = android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = ERASER_STROKE_WIDTH
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        paint.getFillPath(eraserPath, strokedPath)
        
        _uiState.update { state ->
            // Find strokes that intersect with eraser path
            val strokesToRemove = state.strokes.filter { stroke ->
                strokeIntersectsEraserPath(stroke, strokedPath)
            }
            
            if (strokesToRemove.isNotEmpty()) {
                val newUndoStack = (state.undoStack + listOf(state.strokes))
                    .takeLast(MAX_UNDO_HISTORY)
                
                val newStrokes = state.strokes - strokesToRemove.toSet()
                _strokesFlow.value = newStrokes
                _canUndo.value = newUndoStack.isNotEmpty()
                
                // Clear eraser path
                eraserPathPoints.clear()
                _eraserPath.value = emptyList()
                
                state.copy(
                    strokes = newStrokes,
                    undoStack = newUndoStack,
                    redoStack = emptyList(),
                    hasUnsavedChanges = true
                )
            } else {
                // Clear eraser path even if nothing was erased
                eraserPathPoints.clear()
                _eraserPath.value = emptyList()
                state
            }
        }
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
     * Set custom panel size.
     */
    fun setPanelSize(width: Int, height: Int) {
        _uiState.update { it.copy(panelWidth = width, panelHeight = height) }
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
            _uiState.update { 
                it.copy(
                    isUploading = true, 
                    uploadProgress = 0f,
                    uploadStage = "Preparing image..."
                ) 
            }
            
            // Simulate preparation delay for visual feedback
            kotlinx.coroutines.delay(300)
            _uiState.update { it.copy(uploadProgress = 0.15f, uploadStage = "Uploading to storage...") }
            
            // Upload to local storage first
            val uploadResult = storageRepository.uploadPanelImage(bitmap, authorId, chainId)
            
            if (uploadResult.isFailure) {
                _uiState.update { 
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                    )
                }
                return@launch
            }
            
            _uiState.update { it.copy(uploadProgress = 0.5f, uploadStage = "Storage upload complete...") }

            val imageUrl = uploadResult.getOrNull() ?: run {
                _uiState.update { 
                    it.copy(isUploading = false, uploadProgress = 0f, errorMessage = "Upload failed")
                }
                return@launch
            }
            
            _uiState.update { it.copy(uploadProgress = 0.7f, uploadStage = "Saving to database...") }

            // Add panel to Firestore
            val panelResult = panelRepository.addPanel(
                chainId = chainId,
                authorId = authorId,
                authorName = authorName,
                imageUrl = imageUrl,
                caption = caption
            )

            if (panelResult.isSuccess) {
                _uiState.update { it.copy(uploadProgress = 1f, uploadStage = "Complete!") }
                kotlinx.coroutines.delay(200) // Brief delay before showing success
                com.example.dailydoodle.util.Analytics.logPanelAdded(chainId)
                _uiState.update { it.copy(isUploading = false, isSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0f,
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
