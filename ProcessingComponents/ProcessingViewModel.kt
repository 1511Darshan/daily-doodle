package com.yourapp.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Example ViewModel demonstrating how to use the processing components.
 * Adapt this to your actual use case.
 */
class ProcessingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    /**
     * Initialize the processing with items to process
     */
    fun initialize(items: List<ProcessingItem>) {
        _uiState.update { state ->
            state.copy(
                state = OperationState.IDLE,
                dataItems = items,
                task = ProcessingTask(
                    processingIndex = 0,
                    totalCount = items.size
                ),
                preItems = listOf(
                    ProcessingSubItem("prep1", "Checking permissions"),
                    ProcessingSubItem("prep2", "Preparing storage")
                ),
                postItems = listOf(
                    ProcessingSubItem("post1", "Save icons"),
                    ProcessingSubItem("post2", "Cleanup temporary files")
                )
            )
        }
    }

    /**
     * Start the processing
     */
    fun startProcessing() {
        viewModelScope.launch {
            _uiState.update { it.copy(state = OperationState.PROCESSING) }
            
            // Preprocessing phase
            runPreprocessing()
            
            // Process each item
            val items = _uiState.value.dataItems
            for (index in items.indices) {
                processItem(index)
            }
            
            // Post-processing phase
            runPostProcessing()
            
            // Done
            _uiState.update { state ->
                state.copy(
                    state = OperationState.DONE,
                    task = state.task?.copy(
                        processingIndex = state.dataItems.size + 2,
                        successCount = state.dataItems.count { it.state == OperationState.DONE }
                    )
                )
            }
        }
    }

    private suspend fun runPreprocessing() {
        val preItems = _uiState.value.preItems
        
        _uiState.update { state ->
            state.copy(
                task = state.task?.copy(processingIndex = 0)
            )
        }

        for (i in preItems.indices) {
            _uiState.update { state ->
                state.copy(
                    task = state.task?.copy(preprocessingIndex = i),
                    preItemsProgress = (i + 1).toFloat() / preItems.size,
                    preItems = state.preItems.mapIndexed { index, item ->
                        when {
                            index < i -> item.copy(state = OperationState.DONE)
                            index == i -> item.copy(state = OperationState.PROCESSING)
                            else -> item
                        }
                    }
                )
            }
            delay(500) // Simulate work
        }

        _uiState.update { state ->
            state.copy(
                preItemsProgress = 1f,
                preItems = state.preItems.map { it.copy(state = OperationState.DONE) }
            )
        }
    }

    private suspend fun processItem(itemIndex: Int) {
        val item = _uiState.value.dataItems[itemIndex]
        
        // Move to this item
        _uiState.update { state ->
            state.copy(
                task = state.task?.copy(processingIndex = itemIndex + 1),
                dataItems = state.dataItems.mapIndexed { index, dataItem ->
                    if (index == itemIndex) {
                        dataItem.copy(state = OperationState.PROCESSING)
                    } else dataItem
                }
            )
        }

        // Process each sub-item
        for (subIndex in item.subItems.indices) {
            _uiState.update { state ->
                state.copy(
                    dataItems = state.dataItems.mapIndexed { index, dataItem ->
                        if (index == itemIndex) {
                            dataItem.copy(
                                processingIndex = subIndex,
                                progress = (subIndex + 1).toFloat() / item.subItems.size,
                                subItems = dataItem.subItems.mapIndexed { si, subItem ->
                                    when {
                                        si < subIndex -> subItem.copy(state = OperationState.DONE)
                                        si == subIndex -> subItem.copy(state = OperationState.PROCESSING)
                                        else -> subItem
                                    }
                                }
                            )
                        } else dataItem
                    }
                )
            }
            delay(300) // Simulate work
        }

        // Mark item as done
        _uiState.update { state ->
            state.copy(
                dataItems = state.dataItems.mapIndexed { index, dataItem ->
                    if (index == itemIndex) {
                        dataItem.copy(
                            state = OperationState.DONE,
                            progress = 1f,
                            subItems = dataItem.subItems.map { it.copy(state = OperationState.DONE) }
                        )
                    } else dataItem
                }
            )
        }
    }

    private suspend fun runPostProcessing() {
        val postItems = _uiState.value.postItems
        
        _uiState.update { state ->
            state.copy(
                task = state.task?.copy(processingIndex = state.dataItems.size + 1)
            )
        }

        for (i in postItems.indices) {
            _uiState.update { state ->
                state.copy(
                    task = state.task?.copy(postProcessingIndex = i),
                    postItemsProgress = (i + 1).toFloat() / postItems.size,
                    postItems = state.postItems.mapIndexed { index, item ->
                        when {
                            index < i -> item.copy(state = OperationState.DONE)
                            index == i -> item.copy(state = OperationState.PROCESSING)
                            else -> item
                        }
                    }
                )
            }
            delay(500) // Simulate work
        }

        _uiState.update { state ->
            state.copy(
                postItemsProgress = 1f,
                postItems = state.postItems.map { it.copy(state = OperationState.DONE) }
            )
        }
    }
}
