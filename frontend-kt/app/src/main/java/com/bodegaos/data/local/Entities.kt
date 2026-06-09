package com.bodegaos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: String? = null, // <-- CAMBIA ESTO DE Int? a String?
    val name: String,
    val sku: String,
    val description: String,
    val stock: Int
)

@Entity(tableName = "pending_scans")
data class PendingScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sku: String,
    val description: String,
    val quantity: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis() // Clave para el Last-Write-Wins
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sku: String,
    val name: String,
    val description: String,
    val quantity: Int,
    val movementType: String,
    val timestamp: Long = System.currentTimeMillis() // Para ordenarlos por fecha
)