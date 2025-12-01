package com.example.dailydoodle.ui.upload

/**
 * Sealed class representing the different states of the upload process.
 * Used by UploadViewModel to communicate status to the UI.
 */
sealed class UploadState {
    
    /**
     * Initial state - ready to upload
     */
    data object Idle : UploadState()
    
    /**
     * Upload is in progress
     */
    data object Uploading : UploadState()
    
    /**
     * Upload completed successfully
     * @param imageUrl Full-size image URL
     * @param thumbUrl Thumbnail image URL
     */
    data class Success(
        val imageUrl: String,
        val thumbUrl: String
    ) : UploadState()
    
    /**
     * Upload failed
     * @param message Error message to display
     */
    data class Error(val message: String) : UploadState()
}
