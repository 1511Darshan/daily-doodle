package com.example.dailydoodle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.repository.ChainRepository
import com.example.dailydoodle.data.repository.FavoritesRepository
import com.example.dailydoodle.data.repository.TrashRepository
import com.example.dailydoodle.di.AppModule
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = AppModule.favoritesRepository,
    private val trashRepository: TrashRepository = AppModule.trashRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = FavoritesUiState.Empty
            return
        }

        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            
            favoritesRepository.getFavoriteChains(userId).collect { chains ->
                _uiState.value = if (chains.isEmpty()) {
                    FavoritesUiState.Empty
                } else {
                    FavoritesUiState.Success(chains)
                }
            }
        }
    }

    fun removeFromFavorites(chain: Chain) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            favoritesRepository.removeFromFavorites(chain.id, userId)
                .onSuccess {
                    // Remove from UI
                    val currentState = _uiState.value
                    if (currentState is FavoritesUiState.Success) {
                        val updatedChains = currentState.chains.filter { it.id != chain.id }
                        _uiState.value = if (updatedChains.isEmpty()) {
                            FavoritesUiState.Empty
                        } else {
                            FavoritesUiState.Success(updatedChains)
                        }
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to remove from favorites"
                }
        }
    }

    fun moveToTrash(chain: Chain) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            trashRepository.moveToTrash(chain.id, userId)
                .onSuccess {
                    // Remove from favorites UI
                    val currentState = _uiState.value
                    if (currentState is FavoritesUiState.Success) {
                        val updatedChains = currentState.chains.filter { it.id != chain.id }
                        _uiState.value = if (updatedChains.isEmpty()) {
                            FavoritesUiState.Empty
                        } else {
                            FavoritesUiState.Success(updatedChains)
                        }
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to move to trash"
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Success(val chains: List<Chain>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}
