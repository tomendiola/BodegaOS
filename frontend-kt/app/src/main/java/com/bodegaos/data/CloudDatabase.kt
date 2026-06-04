package com.bodegaos.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.bodegaos.data.model.Product
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Transaction(
    val sku: String,
    val description: String,
    val quantity: Int,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

object CloudDatabase {
    // Listas vacías, ¡ya no hay galletas ni aguas fantasma!
    val inventory = mutableStateListOf<Product>()
    val transactionLog = mutableStateListOf<Transaction>()
    private var isInitialized = false

    // Cargar datos al abrir la app
    fun init(context: Context) {
        if (isInitialized) return
        val prefs = context.getSharedPreferences("Bodega_Cloud_DB", Context.MODE_PRIVATE)
        try {
            val invJson = prefs.getString("inventory", "[]") ?: "[]"
            val histJson = prefs.getString("history", "[]") ?: "[]"

            inventory.clear()
            inventory.addAll(Json.decodeFromString<List<Product>>(invJson))

            transactionLog.clear()
            transactionLog.addAll(Json.decodeFromString<List<Transaction>>(histJson))
        } catch (e: Exception) { }
        isInitialized = true
    }

    // Guardar en el disco duro del teléfono
    private fun saveToDisk(context: Context) {
        val prefs = context.getSharedPreferences("Bodega_Cloud_DB", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("inventory", Json.encodeToString(inventory.toList()))
            .putString("history", Json.encodeToString(transactionLog.toList()))
            .apply()
    }

    // Función actualizada que pide 'context' para poder guardar
    fun addOrUpdateProduct(context: Context, sku: String, description: String, quantity: Int, isEntry: Boolean, isSync: Boolean = false) {
        val index = inventory.indexOfFirst { it.sku == sku }
        if (index != -1) {
            val current = inventory[index]
            val newStock = if (isEntry) current.stock + quantity else current.stock - quantity
            inventory[index] = current.copy(stock = newStock)
        } else {
            inventory.add(Product(sku = sku, description = description, stock = if (isEntry) quantity else 0))
        }

        val transactionType = if (isSync) "Sync" else if (isEntry) "Entrada" else "Salida"
        transactionLog.add(0, Transaction(sku, description, quantity, transactionType))

        saveToDisk(context) // Guardado definitivo
    }

    // Función para Editar un producto existente (Actualizada para soportar cambio de SKU)
    fun editProduct(context: Context, oldSku: String, newSku: String, newDescription: String, newStock: Int) {
        val index = inventory.indexOfFirst { it.sku == oldSku }
        if (index != -1) {
            // Reemplazamos el producto con su nuevo SKU y datos
            inventory[index] = Product(sku = newSku, description = newDescription, stock = newStock)

            // Registramos la edición en la bitácora con el nuevo SKU
            transactionLog.add(0, Transaction(newSku, newDescription, newStock, "Edición"))
            saveToDisk(context)
        }
    }

    // Función para Eliminar un producto por completo
    fun deleteProduct(context: Context, sku: String) {
        val product = inventory.find { it.sku == sku }
        if (product != null) {
            inventory.remove(product)

            // Registramos la eliminación en la bitácora
            transactionLog.add(0, Transaction(sku, product.description, product.stock, "Eliminación"))
            saveToDisk(context)
        }
    }
}