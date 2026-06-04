package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.network.MovementResponse
import com.bodegaos.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _historyState = MutableStateFlow<List<MovementResponse>>(emptyList())
    val historyState: StateFlow<List<MovementResponse>> = _historyState.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch { _historyState.value = repository.getAllMovements() }
    }
}