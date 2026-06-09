package com.bodegaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.bodegaos.data.model.PendingScan
import com.bodegaos.domain.usecase.*


@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val addNewProductUseCase: AddNewProductUseCase,
    private val recordMovementUseCase: RecordMovementUseCase,
    private val getProductBySkuUseCase: GetProductBySkuUseCase,
    private val saveOfflineScanUseCase: SaveOfflineScanUseCase
) : ViewModel() {


    
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
            // Reemplazamos repository por el caso de uso
            val product = getProductBySkuUseCase(sku)
            onResult(product)
        }
    }

    fun saveOfflineScan(sku: String, description: String, quantity: String, type: String) {
        viewModelScope.launch {
            val scan = PendingScan(sku = sku, description = description, quantity = quantity, type = type)
            // Reemplazamos repository por el caso de uso
            saveOfflineScanUseCase(scan)
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            // Llamamos al caso de uso directamente gracias al operador invoke()
            val success = addNewProductUseCase(product)
            if (success) {
                resetScanner()
            }
        }
    }

    fun recordMovement(productId: String, qtyChange: Int, type: String) {
        viewModelScope.launch {
            // Llamamos al caso de uso directamente
            val success = recordMovementUseCase(productId, qtyChange, type)
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
