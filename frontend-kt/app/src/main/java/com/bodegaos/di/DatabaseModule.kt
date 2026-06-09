package com.bodegaos.di

import android.content.Context
import androidx.room.Room
import com.bodegaos.data.local.BodegaDao
import com.bodegaos.data.local.BodegaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBodegaDatabase(@ApplicationContext context: Context): BodegaDatabase {
        return Room.databaseBuilder(
            context,
            BodegaDatabase::class.java,
            "bodega_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideBodegaDao(database: BodegaDatabase): BodegaDao {
        return database.bodegaDao()
    }
}