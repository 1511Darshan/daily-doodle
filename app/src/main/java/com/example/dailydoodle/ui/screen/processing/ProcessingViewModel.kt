package com.example.dailydoodle.ui.screen.processing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for processing screens.
 * Extend this class to implement specific processing logic.
 */
abstract class BaseProcessingViewModel : ViewModel() {
    
    protected val _state = MutableStateFlow(ProcessingScreenState())
    val state: StateFlow<ProcessingScreenState> = _state.asStateFlow()
    
    /**
     * Override this to define the processing steps.
     */
    abstract suspend fun process()
    
    /**
     * Start the processing.
     */
    open suspend fun start() {
        _state.update { 
            it.copy(
                overallState = OperationState.PROCESSING,
                statusTitle = "Processing...",
                statusSubtitle = "Please wait"
            )
        }
        
        try {
            process()
            
            _state.update {
                it.copy(
                    overallState = OperationState.DONE,
                    overallProgress = 1f,
                    statusTitle = "Complete!",
                    statusSubtitle = "All tasks finished successfully"
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    overallState = OperationState.ERROR,
                    statusTitle = "Error",
                    statusSubtitle = e.message ?: "An error occurred",
                    errorMessage = e.message
                )
            }
        }
    }
    
    /**
     * Update a specific item's state.
     */
    protected fun updateItem(itemId: String, update: (ProcessingItem) -> ProcessingItem) {
        _state.update { state ->
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) update(item) else item
            }
            val currentIndex = updatedItems.indexOfFirst { it.state == OperationState.PROCESSING }
                .takeIf { it >= 0 } ?: updatedItems.count { it.state == OperationState.DONE }
            
            state.copy(
                items = updatedItems,
                currentItemIndex = currentIndex,
                overallProgress = updatedItems.count { it.state == OperationState.DONE }.toFloat() / updatedItems.size
            )
        }
    }
    
    /**
     * Set item as processing.
     */
    protected fun setItemProcessing(itemId: String, subtitle: String = "") {
        updateItem(itemId) { it.copy(state = OperationState.PROCESSING, subtitle = subtitle) }
        _state.update { it.copy(statusSubtitle = subtitle.ifEmpty { "Processing..." }) }
    }
    
    /**
     * Set item as done.
     */
    protected fun setItemDone(itemId: String, subtitle: String = "") {
        updateItem(itemId) { it.copy(state = OperationState.DONE, subtitle = subtitle, progress = 1f) }
    }
    
    /**
     * Set item as error.
     */
    protected fun setItemError(itemId: String, errorMessage: String) {
        updateItem(itemId) { it.copy(state = OperationState.ERROR, errorMessage = errorMessage) }
    }
    
    /**
     * Update item progress.
     */
    protected fun setItemProgress(itemId: String, progress: Float) {
        updateItem(itemId) { it.copy(progress = progress) }
    }
    
    /**
     * Initialize with items.
     */
    protected fun initializeItems(items: List<ProcessingItem>) {
        _state.update {
            it.copy(
                items = items,
                overallState = OperationState.IDLE,
                statusTitle = "Ready",
                statusSubtitle = "Tap continue to start"
            )
        }
    }
    
    /**
     * Reset for retry.
     */
    open fun reset() {
        _state.update { state ->
            state.copy(
                overallState = OperationState.IDLE,
                overallProgress = 0f,
                statusTitle = "Ready",
                statusSubtitle = "Tap continue to start",
                currentItemIndex = 0,
                errorMessage = null,
                items = state.items.map { it.copy(state = OperationState.IDLE, progress = 0f, errorMessage = null) }
            )
        }
    }
}

/**
 * Example upload processing ViewModel.
 * Shows how to use the BaseProcessingViewModel for panel uploads.
 */
class UploadProcessingViewModel : BaseProcessingViewModel() {
    
    fun initialize(panelName: String = "Panel") {
        initializeItems(listOf(
            ProcessingItem(
                id = "prepare",
                title = "Prepare image",
                subtitle = "Optimizing for upload"
            ),
            ProcessingItem(
                id = "upload",
                title = "Upload to cloud",
                subtitle = "Sending to server"
            ),
            ProcessingItem(
                id = "save",
                title = "Save metadata",
                subtitle = "Updating database"
            )
        ))
        
        _state.update {
            it.copy(
                title = "Uploading $panelName",
                statusTitle = "Ready to upload",
                statusSubtitle = "Tap continue to start upload"
            )
        }
    }
    
    override suspend fun process() {
        // Step 1: Prepare
        setItemProcessing("prepare", "Optimizing image...")
        delay(1500) // Simulate work
        setItemDone("prepare", "Image optimized")
        
        // Step 2: Upload
        setItemProcessing("upload", "Uploading to cloud...")
        for (i in 1..10) {
            delay(200)
            setItemProgress("upload", i / 10f)
        }
        setItemDone("upload", "Upload complete")
        
        // Step 3: Save metadata
        setItemProcessing("save", "Saving to database...")
        delay(1000)
        setItemDone("save", "Saved successfully")
    }
}
