package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.model.Product
import com.bodegaos.domain.usecase.DeleteProductUseCase
import com.bodegaos.domain.usecase.GetInventoryUseCase
import com.bodegaos.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : ViewModel() {

    private val _inventoryState = MutableStateFlow<List<Product>>(emptyList())
    val inventoryState: StateFlow<List<Product>> = _inventoryState

    // 1. AGREGA ESTA VARIABLE DE CARGA
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadInventory() {
        viewModelScope.launch {
            _isLoading.value = true // Empieza a cargar
            _inventoryState.value = getInventoryUseCase()
            _isLoading.value = false // Termina de cargar
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = deleteProductUseCase(id)
            if (success) {
                _inventoryState.value = getInventoryUseCase() // Recargamos directamente
            }
            _isLoading.value = false
        }
    }

    fun updateProduct(id: String, productToUpdate: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = updateProductUseCase(id, productToUpdate)
            if (success) {
                _inventoryState.value = getInventoryUseCase() // Recargamos directamente
            }
            _isLoading.value = false
        }
    }
}