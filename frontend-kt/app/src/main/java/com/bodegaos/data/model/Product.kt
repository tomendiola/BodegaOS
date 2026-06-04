package com.bodegaos.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String? = null,
    val name: String = "",
    val sku: String,
    val description: String = "",
    @SerializedName("quantity") val stock: Int = 0,
    val category: String = "General",

    // Agregamos lo que exige Ktor para no ser rechazados
    val minStock: Int = 5,
    val location: String = "Bodega Central",
    val price: Double = 0.0
)