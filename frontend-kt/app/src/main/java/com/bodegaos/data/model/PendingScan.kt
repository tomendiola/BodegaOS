package com.bodegaos.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PendingScan(
    val sku: String,
    val description: String,
    val quantity: String,
    val type: String, // "Entrada" o "Salida"
    val timestamp: Long = System.currentTimeMillis(),
)