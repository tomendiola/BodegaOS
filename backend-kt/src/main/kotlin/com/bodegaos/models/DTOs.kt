package com.bodegaos.models

import kotlinx.serialization.Serializable


@Serializable
data class ProductDTO(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val quantity: Int,
    val minStock: Int,
    val location: String?,
    val price: Double?,
    val description: String?,
    val lastUpdated: String
)

@Serializable
data class ProductCreateDTO(
    val name: String,
    val sku: String,
    val category: String,
    val quantity: Int = 0,
    val minStock: Int = 10,
    val location: String? = null,
    val price: Double? = null,
    val description: String? = null
)

@Serializable
data class ProductUpdateDTO(
    val name: String? = null,
    val quantity: Int? = null,
    val minStock: Int? = null,
    val location: String? = null,
    val price: Double? = null,
    val description: String? = null
)

@Serializable
data class LoginRequest(
    val usuario: String,
    val password: String
)

@Serializable
data class UsuarioResponse(
    val id: Int,
    val usuario: String,
    val nombre: String?
)

@Serializable
data class StatusCheckCreateDTO(
    val client_name: String
)

@Serializable
data class StatusCheckResponse(
    val id: String,
    val client_name: String,
    val status: String,
    val created_at: String
)

@Serializable
data class InventoryMovementCreateDTO(
    val product_id: String,
    val quantity_change: Int,
    val movement_type: String,
    val reason: String? = null,
    val user_id: String? = null
)
@Serializable
data class MovementResponseDTO(
    val id: String,
    val product_id: String,
    val sku: String,
    val product_name: String,
    val quantity_change: Int,
    val movement_type: String,
    val created_at: String
)