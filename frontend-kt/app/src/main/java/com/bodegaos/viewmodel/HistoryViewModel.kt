package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.network.MovementResponse
import com.bodegaos.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.bodegaos.data.model.Product
import com.bodegaos.domain.usecase.GetHistoryUseCase

// BORRAR: private val repository: ProductRepository
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase // <--- INYECTAMOS EL CASO DE USO
) : ViewModel() {

    private val _historyState = MutableStateFlow<List<Product>>(emptyList())
    val historyState: StateFlow<List<Product>> = _historyState

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = getHistoryUseCase() // <--- LO LLAMAMOS DIRECTAMENTE
        }
    }
}