package com.bodegaos.data.repository

import com.bodegaos.data.model.Product
import com.bodegaos.data.network.RetrofitClient
import android.util.Log
import com.bodegaos.data.network.MovementDTO
import com.bodegaos.data.network.MovementResponse
class ProductRepository {
    private val api = RetrofitClient.api

    suspend fun getAllMovements(): List<MovementResponse> {
        return try { api.getAllMovements() } catch (e: Exception) { emptyList() }
    }

    suspend fun recordMovement(productId: String, qtyChange: Int, type: String): Boolean {
        return try {
            api.recordMovement(MovementDTO(productId, qtyChange, type))
            true
        } catch (e: Exception) {
            Log.e("BODEGA_RED", "Fallo al registrar historial", e)
            false
        }
    }

    suspend fun getAllProducts(): List<Product> {
        return try {
            api.getProducts()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Si hay error o no hay red, devolvemos lista vacía
        }
    }

    suspend fun addProduct(product: Product): Boolean {
        return try {
            api.createProduct(product)
            true
        } catch (e: Exception) {

            Log.e("BODEGA_RED", "Fallo al crear producto en la red", e)
            false
        }
    }

    suspend fun updateProduct(sku: String, product: Product): Boolean {
        return try {
            api.updateProduct(sku, product)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteProduct(sku: String): Boolean {
        return try {
            api.deleteProduct(sku)
            true
        } catch (e: Exception) {
            false
        }
    }

    // En ProductRepository.kt
    suspend fun getProductBySku(sku: String): Product? {
        return try {
            // Obtenemos todos y filtramos en memoria o creamos un endpoint nuevo en Ktor
            val all = api.getProducts()
            all.find { it.sku == sku }
        } catch (e: Exception) {
            null
        }
    }
}