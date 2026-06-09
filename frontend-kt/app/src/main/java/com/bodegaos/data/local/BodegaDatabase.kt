package com.bodegaos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bodegaos.data.model.PendingScanEntity
import com.bodegaos.data.model.ProductEntity
import com.bodegaos.data.model.HistoryEntity

@Database(
    entities = [ProductEntity::class, PendingScanEntity::class, HistoryEntity::class], // <-- Agregada aquí
    version = 1,
    exportSchema = false
)

abstract class BodegaDatabase : RoomDatabase() {
    abstract fun bodegaDao(): BodegaDao
}