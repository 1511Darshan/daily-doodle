package com.example.dailydoodle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.model.DeletedChain
import com.example.dailydoodle.data.repository.TrashRepository
import com.example.dailydoodle.di.AppModule
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrashViewModel(
    private val trashRepository: TrashRepository = AppModule.trashRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrashUiState>(TrashUiState.Loading)
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadTrash()
    }

    fun loadTrash() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = TrashUiState.Empty
            return
        }

        viewModelScope.launch {
            _uiState.value = TrashUiState.Loading
            
            trashRepository.getTrashChains(userId).collect { chains ->
                _uiState.value = if (chains.isEmpty()) {
                    TrashUiState.Empty
                } else {
                    TrashUiState.Success(chains)
                }
            }
        }
    }

    fun restoreChain(deletedChain: DeletedChain) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            trashRepository.restoreFromTrash(deletedChain.id, userId)
                .onSuccess { newChainId ->
                    _successMessage.value = "Chain restored successfully"
                    // Remove from UI
                    val currentState = _uiState.value
                    if (currentState is TrashUiState.Success) {
                        val updatedChains = currentState.chains.filter { it.id != deletedChain.id }
                        _uiState.value = if (updatedChains.isEmpty()) {
                            TrashUiState.Empty
                        } else {
                            TrashUiState.Success(updatedChains)
                        }
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to restore chain"
                }
        }
    }

    fun permanentlyDelete(deletedChain: DeletedChain) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            trashRepository.permanentlyDelete(deletedChain.id, userId)
                .onSuccess {
                    // Remove from UI
                    val currentState = _uiState.value
                    if (currentState is TrashUiState.Success) {
                        val updatedChains = currentState.chains.filter { it.id != deletedChain.id }
                        _uiState.value = if (updatedChains.isEmpty()) {
                            TrashUiState.Empty
                        } else {
                            TrashUiState.Success(updatedChains)
                        }
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to delete chain"
                }
        }
    }

    fun emptyTrash() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            trashRepository.emptyTrash(userId)
                .onSuccess {
                    _uiState.value = TrashUiState.Empty
                    _successMessage.value = "Trash emptied"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to empty trash"
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

sealed class TrashUiState {
    object Loading : TrashUiState()
    object Empty : TrashUiState()
    data class Success(val chains: List<DeletedChain>) : TrashUiState()
    data class Error(val message: String) : TrashUiState()
}
