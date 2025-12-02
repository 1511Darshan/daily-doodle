package com.example.dailydoodle.ui.screen.processing

/**
 * Represents the state of a processing operation.
 */
enum class OperationState {
    IDLE,       // Not started yet
    PROCESSING, // Currently in progress
    DONE,       // Completed successfully
    ERROR       // Failed with an error
}

/**
 * Represents an item being processed (e.g., a step in the upload flow).
 */
data class ProcessingItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val state: OperationState = OperationState.IDLE,
    val progress: Float = 0f, // 0f to 1f for individual item progress
    val errorMessage: String? = null
)

/**
 * Represents the overall state of the processing screen.
 */
data class ProcessingScreenState(
    val title: String = "Processing",
    val overallState: OperationState = OperationState.IDLE,
    val overallProgress: Float = 0f, // 0f to 1f for the linear progress bar
    val statusTitle: String = "Ready",
    val statusSubtitle: String = "Tap continue to start",
    val items: List<ProcessingItem> = emptyList(),
    val currentItemIndex: Int = 0,
    val errorMessage: String? = null
) {
    val segmentProgress: Float
        get() = if (items.isEmpty()) 0f else (currentItemIndex.toFloat() / items.size)
    
    val segments: Int
        get() = items.size.coerceAtLeast(1)
}
