package com.bodegaos.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Products : UUIDTable("products") {
    val name = varchar("name", 255).index()
    val sku = varchar("sku", 100).uniqueIndex()
    val category = varchar("category", 100)
    val quantity = integer("quantity").default(0)
    val minStock = integer("minStock").default(10)
    val location = varchar("location", 255).nullable()
    val price = double("price").nullable()
    val description = text("description").nullable()
    val lastUpdated = varchar("lastUpdated", 100).default(LocalDateTime.now().toString())
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object Users : UUIDTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50).default("user")
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

object Usuarios : IntIdTable("usuario") {
    val usuario = varchar("usuario", 100).uniqueIndex()
    val contra = varchar("contra", 255)
    val nombre = varchar("nombre", 255).nullable()
}

object StatusChecks : UUIDTable("status_checks") {
    val clientName = varchar("client_name", 255)
    val status = varchar("status", 50).default("active")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

object InventoryMovements : UUIDTable("inventory_movements") {
    val productId = uuid("product_id").index()
    val quantityChange = integer("quantity_change")
    val movementType = varchar("movement_type", 50) // entrada, salida, ajuste
    val reason = varchar("reason", 255).nullable()
    val userId = uuid("user_id").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}
