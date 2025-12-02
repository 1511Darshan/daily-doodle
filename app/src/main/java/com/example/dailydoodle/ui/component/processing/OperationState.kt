package com.example.dailydoodle.ui.component.processing

/**
 * Represents the state of an operation/task
 */
enum class OperationState {
    IDLE,       // Not started yet
    PROCESSING, // Currently in progress
    DONE,       // Completed successfully
    ERROR,      // Failed with error
    SKIP        // Skipped
}
