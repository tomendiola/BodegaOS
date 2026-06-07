package com.bodegaos.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.request.*
import com.bodegaos.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

fun Application.configureRouting() {
    routing {
        get("/") {
            // Se declara explícitamente el tipo del Map
            call.respond<Map<String, String>>(mapOf("message" to "Welcome to BodegaOS API (Kotlin)", "version" to "1.0.0"))
        }

        route("/api") {
            get("/health") {
                call.respond<Map<String, String>>(mapOf("status" to "ok", "message" to "BodegaOS API is running"))
            }

            // Auth
            post("/auth/login") {
                val loginRequest = call.receive<LoginRequest>()
                val user = transaction {
                    Usuarios.select { Usuarios.usuario eq loginRequest.usuario }
                        .map {
                            if (it[Usuarios.contra] == loginRequest.password) {
                                UsuarioResponse(
                                    id = it[Usuarios.id].value,
                                    usuario = it[Usuarios.usuario],
                                    nombre = it[Usuarios.nombre]
                                )
                            } else null
                        }.singleOrNull()
                }

                if (user != null) {
                    call.respond(user)
                } else {
                    // Para texto plano, se usa respondText
                    call.respondText("Usuario o contraseña incorrectos", status = HttpStatusCode.Unauthorized)
                }
            }

            // Products
            route("/products") {
                // Obtener todos los productos activos
                get {
                    val category = call.parameters["category"]
                    val products = transaction {
                        val query = if (category != null) {
                            Products.select { (Products.category eq category) and (Products.isDeleted eq false) }
                        } else {
                            Products.select { Products.isDeleted eq false }
                        }
                        query.map { it.toProductDTO() }
                    }
                    call.respond(products)
                }

                // Obtener un producto activo específico por ID
                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                    val product = transaction {
                        Products.select { (Products.id eq UUID.fromString(id)) and (Products.isDeleted eq false) }
                            .map { it.toProductDTO() }
                            .singleOrNull()
                    }
                    if (product != null) call.respond(product) else call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }

                // Crear un nuevo producto (o revivir uno previamente eliminado)
                post {
                    val dto = call.receive<ProductCreateDTO>()
                    try {
                        val newProduct = transaction {
                            val existingDeleted = Products.select { (Products.sku eq dto.sku) and (Products.isDeleted eq true) }.singleOrNull()

                            val generatedId = if (existingDeleted != null) {
                                val id = existingDeleted[Products.id].value
                                Products.update({ Products.id eq id }) {
                                    it[name] = dto.name
                                    it[category] = dto.category
                                    it[quantity] = dto.quantity
                                    it[minStock] = dto.minStock
                                    it[location] = dto.location
                                    it[price] = dto.price
                                    it[description] = dto.description
                                    it[isDeleted] = false
                                    it[lastUpdated] = LocalDateTime.now().toString()
                                }
                                id
                            } else {
                                val statement = Products.insert {
                                    it[name] = dto.name
                                    it[sku] = dto.sku
                                    it[category] = dto.category
                                    it[quantity] = dto.quantity
                                    it[minStock] = dto.minStock
                                    it[location] = dto.location
                                    it[price] = dto.price
                                    it[description] = dto.description
                                    it[isDeleted] = false
                                }
                                statement[Products.id].value
                            }

                            // Guardar movimiento respetando si fue "Sync" o creación ordinaria
                            InventoryMovements.insert {
                                it[productId] = generatedId
                                it[quantityChange] = dto.quantity
                                it[movementType] = dto.movement_type ?: "Entrada"
                                it[reason] = if (dto.movement_type == "Sync") "Registro inicial vía Sync" else "Registro inicial"
                            }
                            Products.select { Products.id eq generatedId }.map { it.toProductDTO() }.single()
                        }
                        call.respond<ProductDTO>(HttpStatusCode.Created, newProduct)
                    } catch (e: org.jetbrains.exposed.exceptions.ExposedSQLException) {
                        call.respondText("El SKU ya está registrado", status = HttpStatusCode.Conflict)
                    } catch (e: Exception) {
                        call.respondText("Error", status = HttpStatusCode.InternalServerError)
                    }
                }

                // Actualizar producto (Modificación / Edición)
                put("/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                    val dto = call.receive<ProductUpdateDTO>()
                    val updated = transaction {
                        val currentProduct = Products.select { Products.id eq UUID.fromString(id) }.singleOrNull()
                        val oldStock = currentProduct?.get(Products.quantity) ?: 0

                        val success = Products.update({ Products.id eq UUID.fromString(id) }) {
                            dto.name?.let { n -> it[name] = n }
                            dto.quantity?.let { q -> it[quantity] = q }
                            dto.minStock?.let { m -> it[minStock] = m }
                            dto.location?.let { l -> it[location] = l }
                            dto.price?.let { p -> it[price] = p }
                            dto.description?.let { d -> it[description] = d }
                            it[lastUpdated] = LocalDateTime.now().toString()
                        } > 0

                        if (success && currentProduct != null) {
                            val newStock = dto.quantity ?: oldStock
                            InventoryMovements.insert {
                                it[productId] = UUID.fromString(id)
                                it[quantityChange] = newStock - oldStock
                                it[movementType] = dto.movement_type ?: "Edición"
                                it[reason] = if (dto.movement_type == "Sync") "Actualización vía Sync" else "Modificación manual"
                            }
                        }
                        success
                    }
                    if (updated) call.respondText("Updated", status = HttpStatusCode.OK) else call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }

                // Eliminación Lógica (Soft Delete)
                delete("/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
                    val deleted = transaction {
                        val currentProduct = Products.select { (Products.id eq UUID.fromString(id)) and (Products.isDeleted eq false) }.singleOrNull()
                        if (currentProduct != null) {
                            val currentStock = currentProduct[Products.quantity]

                            Products.update({ Products.id eq UUID.fromString(id) }) {
                                it[isDeleted] = true
                                it[lastUpdated] = LocalDateTime.now().toString()
                            }

                            // Deja rastro de la eliminación en la bitácora restando el stock existente
                            InventoryMovements.insert {
                                it[productId] = UUID.fromString(id)
                                it[quantityChange] = -currentStock
                                it[movementType] = "Eliminación"
                                it[reason] = "Producto removido del sistema"
                            }
                            true
                        } else false
                    }
                    if (deleted) call.respond<Map<String, String>>(HttpStatusCode.OK, mapOf("message" to "Product deleted successfully"))
                    else call.respondText("Not Found", status = HttpStatusCode.NotFound)
                }
            }

            // Status
            route("/status") {
                get {
                    val checks = transaction {
                        StatusChecks.selectAll().map {
                            StatusCheckResponse(
                                id = it[StatusChecks.id].value.toString(),
                                client_name = it[StatusChecks.clientName],
                                status = it[StatusChecks.status],
                                created_at = it[StatusChecks.createdAt].toString()
                            )
                        }
                    }
                    call.respond(checks)
                }

                post {
                    val dto = call.receive<StatusCheckCreateDTO>()
                    val newCheck = transaction {
                        val id = UUID.randomUUID()
                        StatusChecks.insert {
                            it[StatusChecks.id] = id
                            it[clientName] = dto.client_name
                        }
                        StatusChecks.select { StatusChecks.id eq id }.map {
                            StatusCheckResponse(
                                id = it[StatusChecks.id].value.toString(),
                                client_name = it[StatusChecks.clientName],
                                status = it[StatusChecks.status],
                                created_at = it[StatusChecks.createdAt].toString()
                            )
                        }.single()
                    }
                    // Explicitamos <StatusCheckResponse>
                    call.respond<StatusCheckResponse>(HttpStatusCode.Created, newCheck)
                }
            }

            // Inventory
            route("/inventory") {
                // Registrar movimiento y auto-actualizar stock
                post("/movements") {
                    val dto = call.receive<InventoryMovementCreateDTO>()
                    val result = transaction {
                        val product = Products.select { Products.id eq UUID.fromString(dto.product_id) }.singleOrNull()
                            ?: return@transaction null

                        // Asegúrate de que tu ruta POST tenga esto:
                        val newQty = product[Products.quantity] + dto.quantity_change // <--- ¡EL SIGNO MÁS ES CLAVE!

                        Products.update({ Products.id eq UUID.fromString(dto.product_id) }) {
                            it[quantity] = newQty // Aquí guardamos el resultado de la suma
                            it[lastUpdated] = LocalDateTime.now().toString()
                        }

                        InventoryMovements.insert {
                            it[productId] = UUID.fromString(dto.product_id)
                            it[quantityChange] = dto.quantity_change
                            it[movementType] = dto.movement_type
                            it[reason] = dto.reason
                        }
                        newQty
                    }
                    if (result != null) call.respond<Map<String, Any>>(mapOf("message" to "Ok", "new_quantity" to result))
                    else call.respondText("Not found", status = HttpStatusCode.NotFound)
                }

                // Obtener historial completo para Android

                get("/movements") {
                    val movements = transaction {
                        (InventoryMovements innerJoin Products)
                            .selectAll()
                            .orderBy(InventoryMovements.createdAt to SortOrder.DESC)
                            .map {
                                // AHORA USAMOS NUESTRO DTO EN LUGAR DE mapOf
                                MovementResponseDTO(
                                    id = it[InventoryMovements.id].value.toString(),
                                    product_id = it[InventoryMovements.productId].toString(),
                                    sku = it[Products.sku],
                                    product_name = it[Products.name],
                                    quantity_change = it[InventoryMovements.quantityChange],
                                    movement_type = it[InventoryMovements.movementType],
                                    created_at = it[InventoryMovements.createdAt].toString()
                                )
                            }
                    }
                    call.respond(movements) // Ahora Ktor lo serializará felizmente
                }
            }
        }
    }
}

fun ResultRow.toProductDTO() = ProductDTO(
    id = this[Products.id].value.toString(),
    name = this[Products.name],
    sku = this[Products.sku],
    category = this[Products.category],
    quantity = this[Products.quantity],
    minStock = this[Products.minStock],
    location = this[Products.location],
    price = this[Products.price],
    description = this[Products.description],
    lastUpdated = this[Products.lastUpdated]
)