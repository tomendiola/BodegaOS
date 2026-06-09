package com.bodegaos.data.model

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Product(
    val id: String? = null,
    val name: String = "",
    val sku: String,
    val description: String = "",
    @SerializedName("quantity") val stock: Int = 0,
    val category: String = "General",
    val minStock: Int = 5,
    val location: String = "Bodega Central",
    val price: Double = 0.0,
    @SerializedName("movement_type") val movementType: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)