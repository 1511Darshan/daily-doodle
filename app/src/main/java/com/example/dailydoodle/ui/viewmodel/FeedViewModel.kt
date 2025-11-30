package com.example.dailydoodle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.repository.ChainFilter
import com.example.dailydoodle.data.repository.ChainRepository
import com.example.dailydoodle.di.AppModule
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(
    private val chainRepository: ChainRepository = AppModule.chainRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow<ChainFilter>(ChainFilter.RECENT)
    val selectedFilter: StateFlow<ChainFilter> = _selectedFilter.asStateFlow()

    private val _favoriteChainIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteChainIds: StateFlow<Set<String>> = _favoriteChainIds.asStateFlow()

    init {
        loadChains(ChainFilter.RECENT)
    }

    fun loadChains(filter: ChainFilter) {
        viewModelScope.launch {
            _selectedFilter.value = filter
            _uiState.value = FeedUiState.Loading
            
            com.example.dailydoodle.util.Analytics.logFeedOpen(filter.name.lowercase())
            
            chainRepository.getChains(filter, limit = 20).collect { chains ->
                _uiState.value = if (chains.isEmpty()) {
                    FeedUiState.Empty
                } else {
                    // Update chains with favorite status
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val updatedChains = chains.map { chain ->
                            chain.copy(isFavorite = _favoriteChainIds.value.contains(chain.id))
                        }
                        FeedUiState.Success(updatedChains)
                    } else {
                        FeedUiState.Success(chains)
                    }
                }
            }
        }
    }

    fun toggleFavorite(chain: Chain) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            chainRepository.toggleFavorite(chain.id, userId).onSuccess { isFavorite ->
                // Update local state
                _favoriteChainIds.value = if (isFavorite) {
                    _favoriteChainIds.value + chain.id
                } else {
                    _favoriteChainIds.value - chain.id
                }
                
                // Update the chain in the UI state
                val currentState = _uiState.value
                if (currentState is FeedUiState.Success) {
                    val updatedChains = currentState.chains.map {
                        if (it.id == chain.id) it.copy(isFavorite = isFavorite) else it
                    }
                    _uiState.value = FeedUiState.Success(updatedChains)
                }
            }
        }
    }

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    fun clearDeleteError() {
        _deleteError.value = null
    }

    fun deleteChain(chain: Chain) {
        val userId = auth.currentUser?.uid ?: ""
        viewModelScope.launch {
            chainRepository.deleteChain(chain.id, userId)
                .onSuccess {
                    // Remove chain from UI state
                    val currentState = _uiState.value
                    if (currentState is FeedUiState.Success) {
                        val updatedChains = currentState.chains.filter { it.id != chain.id }
                        _uiState.value = if (updatedChains.isEmpty()) {
                            FeedUiState.Empty
                        } else {
                            FeedUiState.Success(updatedChains)
                        }
                    }
                }
                .onFailure { error ->
                    _deleteError.value = error.message ?: "Failed to delete chain"
                }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun refresh() {
        loadChains(_selectedFilter.value)
    }
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    object Empty : FeedUiState()
    data class Success(val chains: List<Chain>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}
