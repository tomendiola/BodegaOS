package com.bodegaos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PendingScan(
    val sku: String,
    val description: String,
    val quantity: String,
    val type: String, // "Entrada" o "Salida"
    val timestamp: Long = System.currentTimeMillis()
)