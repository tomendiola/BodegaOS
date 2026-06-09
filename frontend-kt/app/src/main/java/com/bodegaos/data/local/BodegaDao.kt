package com.bodegaos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bodegaos.data.model.PendingScanEntity
import com.bodegaos.data.model.ProductEntity
import com.bodegaos.data.model.HistoryEntity

@Dao
interface BodegaDao {
    // --- Inventario ---
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    // --- Historial de Movimientos ---
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllMovements(): List<HistoryEntity>
    // --- Operaciones de Modificación ---
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("UPDATE products SET name = :name, sku = :sku, description = :description, stock = :stock WHERE id = :id")
    suspend fun updateProductById(id: String, name: String, sku: String, description: String, stock: Int)

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Insert
    suspend fun insertMovement(history: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    // --- Cola de Sincronización (Offline) ---
    @Insert
    suspend fun insertPendingScan(scan: PendingScanEntity)

    @Query("SELECT * FROM pending_scans ORDER BY timestamp ASC")
    suspend fun getAllPendingScans(): List<PendingScanEntity>

    @Query("DELETE FROM pending_scans WHERE id = :id")
    suspend fun deletePendingScan(id: Int)
}