package com.bodegaos.data.model

data class Product(
    val sku: String,
    val description: String,
    var stock: Int
)
