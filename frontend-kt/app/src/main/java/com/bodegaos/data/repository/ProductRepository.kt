package com.bodegaos.data.repository

import com.bodegaos.data.model.Product

object ProductRepository {
    private val products = mutableListOf<Product>(
        Product("12345", "Caja de Galletas Surtidas", 10)
    )

    fun findBySku(sku: String): Product? {
        return products.find { it.sku == sku }
    }

    fun addProduct(product: Product) {
        products.add(product)
    }

    fun updateStock(sku: String, quantity: Int): Boolean {
        val product = findBySku(sku)
        return if (product != null) {
            product.stock += quantity
            true
        } else {
            false
        }
    }
    
    fun getAllProducts(): List<Product> = products
}
