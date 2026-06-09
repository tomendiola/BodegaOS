package com.bodegaos.domain.usecase

import com.bodegaos.data.model.Product
import com.bodegaos.data.repository.ProductRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(private val repository: ProductRepository) {
    suspend operator fun invoke(): List<Product> {
        return repository.getAllMovements()
    }
}