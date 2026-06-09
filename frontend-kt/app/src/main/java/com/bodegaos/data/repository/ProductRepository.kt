package com.bodegaos.data.repository

import com.bodegaos.data.local.BodegaDao
import com.bodegaos.data.model.HistoryEntity
import com.bodegaos.data.model.PendingScan
import com.bodegaos.data.model.PendingScanEntity
import com.bodegaos.data.model.Product
import com.bodegaos.data.model.ProductEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val bodegaDao: BodegaDao
) {
    // --- Inventario Local (Single Source of Truth) ---
    suspend fun getAllProducts(): List<Product> {
        // Obtenemos de Room y mapeamos al modelo original para no romper la UI
        return bodegaDao.getAllProducts().map {
            Product(id = it.id, name = it.name, sku = it.sku, description = it.description, stock = it.stock)
        }
    }
    // --- Historial ---
    suspend fun getAllMovements(): List<Product> {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        return bodegaDao.getAllMovements().map { history ->
            Product(
                id = history.id.toString(),
                sku = history.sku,
                name = history.name,
                description = history.description,
                stock = history.quantity, // Aquí guardamos la cantidad movida
                movementType = history.movementType,
                createdAt = sdf.format(java.util.Date(history.timestamp)) // <-- Convertimos el timestamp a fecha
            )
        }
    }

    suspend fun addMovementToHistory(product: Product, quantity: Int, type: String) {
        bodegaDao.insertMovement(
            HistoryEntity(
                sku = product.sku,
                name = product.name,
                description = product.description,
                quantity = quantity,
                movementType = type
            )
        )
    }
    // --- Modificación y Eliminación ---
    suspend fun deleteProduct(id: String): Boolean {
        return try {
            bodegaDao.deleteProductById(id)
            true // Retorna verdadero si se borró con éxito de Room
        } catch (e: Exception) {
            false // Retorna falso si hubo algún error
        }
    }

    suspend fun updateProduct(id: String, product: Product): Boolean {
        return try {
            bodegaDao.updateProductById(
                id = id,
                name = product.name,
                sku = product.sku,
                description = product.description,
                stock = product.stock
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getProductBySku(sku: String): Product? {
        val entity = bodegaDao.getProductBySku(sku) ?: return null
        return Product(id = entity.id, name = entity.name, sku = entity.sku, description = entity.description, stock = entity.stock)
    }

    suspend fun addProduct(product: Product): Boolean {
        return try {
            bodegaDao.insertProduct(
                ProductEntity(id = product.id, name = product.name, sku = product.sku, description = product.description, stock = product.stock)
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recordMovement(productId: String, qtyChange: Int, type: String): Boolean {
        return try {
            // 1. Buscamos el producto en la base de datos usando el DAO
            val product = bodegaDao.getProductById(productId) ?: return false

            // 2. Calculamos y actualizamos el nuevo stock en el Inventario
            val nuevoStock = product.stock + qtyChange
            bodegaDao.updateProductById(
                id = productId,
                name = product.name,
                sku = product.sku,
                description = product.description,
                stock = nuevoStock
            )

            // 3. Guardamos el registro de la acción en la tabla de Historial
            bodegaDao.insertMovement(
                HistoryEntity(
                    sku = product.sku,
                    name = product.name,
                    description = product.description,
                    quantity = qtyChange, // Lo que sumamos o restamos
                    movementType = type
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addPendingScan(scan: PendingScan) {
        bodegaDao.insertPendingScan(
            PendingScanEntity(sku = scan.sku, description = scan.description, quantity = scan.quantity, type = scan.type)
        )
    }

    suspend fun getPendingScans(): List<PendingScan> {
        return bodegaDao.getAllPendingScans().map {
            PendingScan(sku = it.sku, description = it.description, quantity = it.quantity, type = it.type)
        }
    }

    suspend fun clearPendingScans() {
        // Limpia los escaneos uno por uno o crea un método deleteAll en el DAO
        val scans = bodegaDao.getAllPendingScans()
        for (scan in scans) {
            bodegaDao.deletePendingScan(scan.id)
        }
    }
}