package com.bodegaos

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.bodegaos.plugins.*
import com.bodegaos.routes.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.bodegaos.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun main() {
    embeddedServer(Netty, port = 8000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    initDB()
    configureSerialization()
    configureCORS()
    configureRouting()
}

fun initDB() {
    val config = HikariConfig().apply {
        jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/bodegaos"
        driverClassName = "org.postgresql.Driver"
        username = "postgres"
        password = "601712" // Should be in env
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Products, Users, Usuarios, StatusChecks, InventoryMovements)
    }
}
