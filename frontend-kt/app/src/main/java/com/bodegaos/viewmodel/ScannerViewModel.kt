package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository

class ScannerViewModel : ViewModel() {
    var scannedCode by mutableStateOf<String?>(null)
    var productFound by mutableStateOf<Product?>(null)
    var isNewProduct by mutableStateOf(false)
    var justAddedMessage by mutableStateOf(false)
    var manualInput by mutableStateOf("")
    var showManualSearchBar by mutableStateOf(false)

    // Form fields
    var description by mutableStateOf("")
    var sku by mutableStateOf("")
    var quantity by mutableStateOf("1")

    fun onCodeScanned(code: String) {
        scannedCode = code
        val product = ProductRepository.findBySku(code)
        if (product != null) {
            productFound = product
            isNewProduct = false
            // Auto-increment for existing product
            ProductRepository.updateStock(code, 1)
            justAddedMessage = true
        } else {
            productFound = null
            isNewProduct = true
            sku = code
            description = ""
            quantity = "1"
            justAddedMessage = false
        }
    }

    fun registerNewProduct() {
        val qty = quantity.toIntOrNull() ?: 0
        val newProduct = Product(sku, description, qty)
        ProductRepository.addProduct(newProduct)
        productFound = newProduct
        isNewProduct = false
        justAddedMessage = true
    }

    fun resetScanner() {
        scannedCode = null
        productFound = null
        isNewProduct = false
        justAddedMessage = false
        description = ""
        sku = ""
        quantity = "1"
    }
}
