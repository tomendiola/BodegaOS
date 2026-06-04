package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    // Inyectamos nuestro repositorio real
    private val repository = ProductRepository()

    private val _inventoryState = MutableStateFlow<List<Product>>(emptyList())
    val inventoryState: StateFlow<List<Product>> = _inventoryState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInventory()
    }

    // --- AHORA HABLAMOS CON EL SERVIDOR KTOR REAL ---

    fun loadInventory() {
        viewModelScope.launch {
            _isLoading.value = true

            // Llama a la red: GET http://10.0.2.2:8080/productos
            val data = repository.getAllProducts()

            _inventoryState.value = data
            _isLoading.value = false
        }
    }

    fun deleteProduct(id: String) { // CAMBIAMOS SKU POR ID
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteProduct(id)
            if (success) loadInventory() else _isLoading.value = false
        }
    }

    fun updateProduct(id: String, newSku: String, newDesc: String, newStock: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val productToUpdate = Product(id = id, name = newDesc, sku = newSku, description = newDesc, stock = newStock)
            val success = repository.updateProduct(id, productToUpdate)
            if (success) loadInventory() else _isLoading.value = false
        }
    }
}