package com.example.dailydoodle.ui.component.processing

/**
 * Represents an item being processed (e.g., an app, a file, etc.)
 */
data class ProcessingItem(
    val id: String,
    val title: String,
    val iconData: Any? = null, // Can be packageName, drawable resource, or bitmap
    val state: OperationState = OperationState.IDLE,
    val subItems: List<ProcessingSubItem> = emptyList(),
    val processingIndex: Int = -1, // Current sub-item being processed
    val progress: Float = 0f // 0f to 1f
)

/**
 * Represents a sub-task within a processing item (e.g., Backup APK, Backup DATA, etc.)
 */
data class ProcessingSubItem(
    val id: String,
    val title: String,
    val content: String = "", // Additional info like file size
    val state: OperationState = OperationState.IDLE
)

/**
 * Represents the overall task state
 */
data class ProcessingTask(
    val id: Long = System.currentTimeMillis(),
    val processingIndex: Int = 0, // 0 = preprocessing, 1..n = items, n+1 = postprocessing, n+2 = done
    val preprocessingIndex: Int = 0,
    val postProcessingIndex: Int = 0,
    val totalCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0
)

/**
 * UI state for the processing screen
 */
data class ProcessingUiState(
    val state: OperationState = OperationState.IDLE,
    val task: ProcessingTask? = null,
    val preItems: List<ProcessingSubItem> = emptyList(),
    val postItems: List<ProcessingSubItem> = emptyList(),
    val dataItems: List<ProcessingItem> = emptyList(),
    val preItemsProgress: Float = 0f,
    val postItemsProgress: Float = 0f
)
