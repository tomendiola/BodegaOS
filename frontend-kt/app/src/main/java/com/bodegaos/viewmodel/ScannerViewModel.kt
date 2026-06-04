package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {
    private val repository = ProductRepository()
    
    var scannedCode by mutableStateOf<String?>(null)
    var productFound by mutableStateOf<Product?>(null)
    var isNewProduct by mutableStateOf(false)
    var justAddedMessage by mutableStateOf(false)
    var manualInput by mutableStateOf("")
    var isCheckingProduct by mutableStateOf(false)

    // Form fields
    var description by mutableStateOf("")
    var quantity by mutableStateOf("1")
    var transactionType by mutableStateOf("Entrada")

    fun onSkuChanged(sku: String) {
        if (sku.isBlank()) return
        scannedCode = sku
        isCheckingProduct = true
        checkProductExists(sku) { existingProduct ->
            isCheckingProduct = false
            if (existingProduct != null) {
                productFound = existingProduct
                description = existingProduct.description
                isNewProduct = false
            } else {
                productFound = null
                description = ""
                isNewProduct = true
            }
            justAddedMessage = false
        }
    }

    fun checkProductExists(sku: String, onResult: (Product?) -> Unit) {
        viewModelScope.launch {
            val product = repository.getProductBySku(sku)
            onResult(product)
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            val success = repository.addProduct(product)
            if (success) {
                resetScanner()
            }
        }
    }

    fun updateProduct(id: String, product: Product) {
        viewModelScope.launch {
            val success = repository.updateProduct(id, product)
            if (success) {
                resetScanner()
            }
        }
    }

    fun recordMovement(productId: String, qtyChange: Int, type: String) {
        viewModelScope.launch {
            val success = repository.recordMovement(productId, qtyChange, type)
            // No importa si falla o no, regresamos para permitir re-intentar o seguir trabajando
            resetScanner()
        }
    }

    fun resetScanner() {
        scannedCode = null
        productFound = null
        isNewProduct = false
        justAddedMessage = false
        description = ""
        quantity = "1"
        transactionType = "Entrada"
        manualInput = ""
    }
}
