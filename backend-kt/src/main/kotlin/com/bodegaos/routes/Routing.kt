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

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("message" to "Welcome to BodegaOS API (Kotlin)", "version" to "1.0.0"))
        }

        route("/api") {
            get("/health") {
                call.respond(mapOf("status" to "ok", "message" to "BodegaOS API is running"))
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
                    call.respond(HttpStatusCode.Unauthorized, "Usuario o contraseña incorrectos")
                }
            }

            // Products
            route("/products") {
                get {
                    val category = call.parameters["category"]
                    val products = transaction {
                        val query = if (category != null) {
                            Products.select { Products.category eq category }
                        } else {
                            Products.selectAll()
                        }
                        query.map { it.toProductDTO() }
                    }
                    call.respond(products)
                }

                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val product = transaction {
                        Products.select { Products.id eq UUID.fromString(id) }
                            .map { it.toProductDTO() }
                            .singleOrNull()
                    }
                    if (product != null) call.respond(product) else call.respond(HttpStatusCode.NotFound)
                }

                post {
                    val dto = call.receive<ProductCreateDTO>()
                    val newProduct = transaction {
                        val id = UUID.randomUUID()
                        Products.insert {
                            it[Products.id] = id
                            it[name] = dto.name
                            it[sku] = dto.sku
                            it[category] = dto.category
                            it[quantity] = dto.quantity
                            it[minStock] = dto.minStock
                            it[location] = dto.location
                            it[price] = dto.price
                            it[description] = dto.description
                        }
                        Products.select { Products.id eq id }.map { it.toProductDTO() }.single()
                    }
                    call.respond(HttpStatusCode.Created, newProduct)
                }

                put("/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                    val dto = call.receive<ProductUpdateDTO>()
                    val updated = transaction {
                        Products.update({ Products.id eq UUID.fromString(id) }) {
                            dto.name?.let { n -> it[name] = n }
                            dto.quantity?.let { q -> it[quantity] = q }
                            dto.minStock?.let { m -> it[minStock] = m }
                            dto.location?.let { l -> it[location] = l }
                            dto.price?.let { p -> it[price] = p }
                            dto.description?.let { d -> it[description] = d }
                            it[lastUpdated] = LocalDateTime.now().toString()
                        } > 0
                    }
                    if (updated) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
                }

                delete("/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val deleted = transaction {
                        Products.deleteWhere { Products.id eq UUID.fromString(id) } > 0
                    }
                    if (deleted) call.respond(HttpStatusCode.OK, mapOf("message" to "Product deleted successfully"))
                    else call.respond(HttpStatusCode.NotFound)
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
                    call.respond(HttpStatusCode.Created, newCheck)
                }
            }

            // Inventory
            route("/inventory") {
                post("/movements") {
                    val dto = call.receive<InventoryMovementCreateDTO>()
                    val result = transaction {
                        val product = Products.select { Products.id eq UUID.fromString(dto.product_id) }.singleOrNull()
                            ?: return@transaction null
                        
                        val newQty = product[Products.quantity] + dto.quantity_change
                        
                        Products.update({ Products.id eq UUID.fromString(dto.product_id) }) {
                            it[quantity] = newQty
                            it[lastUpdated] = LocalDateTime.now().toString()
                        }

                        InventoryMovements.insert {
                            it[productId] = UUID.fromString(dto.product_id)
                            it[quantityChange] = dto.quantity_change
                            it[movementType] = dto.movement_type
                            it[reason] = dto.reason
                            it[userId] = dto.user_id?.let { u -> UUID.fromString(u) }
                        }
                        newQty
                    }

                    if (result != null) {
                        call.respond(mapOf("message" to "Inventory movement recorded", "new_quantity" to result))
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Product not found")
                    }
                }

                get("/movements/{productId}") {
                    val productId = call.parameters["productId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val movements = transaction {
                        InventoryMovements.select { InventoryMovements.productId eq UUID.fromString(productId) }
                            .map {
                                mapOf(
                                    "id" to it[InventoryMovements.id].value.toString(),
                                    "product_id" to it[InventoryMovements.productId].toString(),
                                    "quantity_change" to it[InventoryMovements.quantityChange],
                                    "movement_type" to it[InventoryMovements.movementType],
                                    "reason" to it[InventoryMovements.reason],
                                    "created_at" to it[InventoryMovements.createdAt].toString()
                                )
                            }
                    }
                    call.respond(movements)
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
