package com.bodegaos.data

import android.content.Context
import com.bodegaos.data.model.PendingScan
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SyncManager(context: Context) {
    private val prefs = context.getSharedPreferences("BodegaOS_Offline", Context.MODE_PRIVATE)

    fun savePendingScan(scan: PendingScan) {
        val currentList = getPendingScans().toMutableList()
        currentList.add(scan)
        prefs.edit().putString("sync_queue", Json.encodeToString(currentList)).apply()
    }

    fun getPendingScans(): List<PendingScan> {
        val jsonString = prefs.getString("sync_queue", "[]") ?: "[]"
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearSyncQueue() {
        prefs.edit().remove("sync_queue").apply()
    }

    // --- NUEVAS FUNCIONES CRUD ---

    fun deletePendingScan(timestamp: Long) {
        val currentList = getPendingScans().toMutableList()
        currentList.removeAll { it.timestamp == timestamp } // Borra el exacto
        prefs.edit().putString("sync_queue", Json.encodeToString(currentList)).apply()
    }

    fun updatePendingScan(timestamp: Long, newSku: String, newDesc: String, newQty: String) {
        val currentList = getPendingScans().toMutableList()
        val index = currentList.indexOfFirst { it.timestamp == timestamp }
        if (index != -1) {
            val oldScan = currentList[index]
            currentList[index] = oldScan.copy(sku = newSku, description = newDesc, quantity = newQty)
            prefs.edit().putString("sync_queue", Json.encodeToString(currentList)).apply()
        }
    }
}