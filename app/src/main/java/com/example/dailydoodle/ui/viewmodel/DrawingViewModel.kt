package com.example.dailydoodle.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.repository.PanelRepository
import com.example.dailydoodle.data.repository.StorageRepository
import com.example.dailydoodle.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrawingViewModel(
    private val storageRepository: StorageRepository = AppModule.storageRepository,
    private val panelRepository: PanelRepository = AppModule.panelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DrawingUiState>(DrawingUiState.Idle)
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

    fun saveDrawing(bitmap: Bitmap) {
        _currentBitmap.value = bitmap
    }

    fun submitPanel(
        chainId: String,
        authorId: String,
        authorName: String,
        bitmap: Bitmap,
        caption: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = DrawingUiState.Uploading
            
            // Upload image
            val uploadResult = storageRepository.uploadPanelImage(bitmap, authorId, chainId)
            
            if (uploadResult.isFailure) {
                _uiState.value = DrawingUiState.Error(
                    uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                )
                return@launch
            }

            val imageUrl = uploadResult.getOrNull() ?: run {
                _uiState.value = DrawingUiState.Error("Upload failed")
                return@launch
            }

            // Add panel to Firestore
            val panelResult = panelRepository.addPanel(
                chainId = chainId,
                authorId = authorId,
                authorName = authorName,
                imageUrl = imageUrl,
                caption = caption
            )

            _uiState.value = when {
                panelResult.isSuccess -> {
                    com.example.dailydoodle.util.Analytics.logPanelAdded(chainId)
                    DrawingUiState.Success
                }
                else -> DrawingUiState.Error(
                    panelResult.exceptionOrNull()?.message ?: "Failed to add panel"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = DrawingUiState.Idle
        _currentBitmap.value = null
    }
}

sealed class DrawingUiState {
    object Idle : DrawingUiState()
    object Uploading : DrawingUiState()
    object Success : DrawingUiState()
    data class Error(val message: String) : DrawingUiState()
}
