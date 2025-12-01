package com.example.dailydoodle.ui.upload

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.repository.UploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling panel upload operations.
 * Manages upload state and communicates with the UploadRepository.
 */
class UploadViewModel(
    private val repository: UploadRepository = UploadRepository()
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    /**
     * Upload a panel bitmap to the server.
     * Updates uploadState throughout the process.
     * 
     * @param bitmap The panel image to upload
     * @param chainId The chain this panel belongs to
     * @param authorId The user ID of the panel author
     */
    fun upload(bitmap: Bitmap, chainId: String, authorId: String) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            
            val result = repository.uploadPanel(bitmap, chainId, authorId)
            
            _uploadState.value = if (result.isSuccess) {
                val response = result.getOrThrow()
                UploadState.Success(
                    imageUrl = response.imageUrl,
                    thumbUrl = response.thumbUrl
                )
            } else {
                val errorMessage = result.exceptionOrNull()?.localizedMessage 
                    ?: "Upload failed. Please try again."
                UploadState.Error(errorMessage)
            }
        }
    }

    /**
     * Reset the upload state to Idle.
     * Call this when dismissing error dialogs or starting a new upload flow.
     */
    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
    
    /**
     * Retry the last upload operation.
     * Resets state to Idle so the user can attempt upload again.
     */
    fun retry() {
        resetState()
    }
}
