package com.example.dailydoodle.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.model.Panel
import com.example.dailydoodle.data.repository.ChainRepository
import com.example.dailydoodle.data.repository.PanelRepository
import com.example.dailydoodle.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChainViewModel(
    private val chainRepository: ChainRepository = AppModule.chainRepository,
    private val panelRepository: PanelRepository = AppModule.panelRepository
) : ViewModel() {

    private val _chain = MutableStateFlow<Chain?>(null)
    val chain: StateFlow<Chain?> = _chain.asStateFlow()

    private val _panels = MutableStateFlow<List<Panel>>(emptyList())
    val panels: StateFlow<List<Panel>> = _panels.asStateFlow()

    private val _uiState = MutableStateFlow<ChainUiState>(ChainUiState.Loading)
    val uiState: StateFlow<ChainUiState> = _uiState.asStateFlow()

    fun loadChain(chainId: String) {
        viewModelScope.launch {
            _uiState.value = ChainUiState.Loading
            
            com.example.dailydoodle.util.Analytics.logChainOpened(chainId)
            
            val chain = chainRepository.getChain(chainId)
            _chain.value = chain

            if (chain != null) {
                panelRepository.getPanelsForChain(chainId).collect { panels ->
                    _panels.value = panels
                    _uiState.value = ChainUiState.Success
                }
            } else {
                _uiState.value = ChainUiState.Error("Chain not found")
            }
        }
    }

    fun refresh(chainId: String) {
        loadChain(chainId)
    }
}

sealed class ChainUiState {
    object Loading : ChainUiState()
    object Success : ChainUiState()
    data class Error(val message: String) : ChainUiState()
}
