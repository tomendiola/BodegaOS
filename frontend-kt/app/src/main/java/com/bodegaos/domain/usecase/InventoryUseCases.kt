package com.bodegaos.domain.usecase

import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import javax.inject.Inject

class GetInventoryUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<Product> {
        return repository.getAllProducts()
    }
}

class DeleteProductUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String): Boolean {
        return repository.deleteProduct(id)
    }
}

class UpdateProductUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String, product: Product): Boolean {
        if (product.stock < 0) return false // Regla de negocio de ejemplo
        return repository.updateProduct(id, product)
    }
}