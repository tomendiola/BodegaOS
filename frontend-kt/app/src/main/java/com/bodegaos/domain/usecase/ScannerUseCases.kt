package com.bodegaos.domain.usecase

import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import javax.inject.Inject
import com.bodegaos.data.model.PendingScan

class RecordMovementUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(productId: String, qtyChange: Int, type: String): Boolean {
        // Toda la regla de negocio pesada ahora vive aquí
        if (qtyChange == 0) return false
        return repository.recordMovement(productId, qtyChange, type)
    }
}

class AddNewProductUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product): Boolean {
        if (product.sku.isBlank() || product.name.isBlank()) return false
        return repository.addProduct(product)
    }
}

class GetProductBySkuUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(sku: String): Product? {
        return repository.getProductBySku(sku)
    }
}

class SaveOfflineScanUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(scan: PendingScan) {
        repository.addPendingScan(scan)
    }
}