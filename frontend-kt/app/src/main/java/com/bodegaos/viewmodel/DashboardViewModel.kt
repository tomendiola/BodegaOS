package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _pendingSyncsCount = MutableStateFlow(0)
    val pendingSyncsCount: StateFlow<Int> = _pendingSyncsCount

    fun loadStats() {
        viewModelScope.launch {
            // Obtenemos la cantidad de elementos directamente desde Room
            _pendingSyncsCount.value = repository.getPendingScans().size
        }
    }
}