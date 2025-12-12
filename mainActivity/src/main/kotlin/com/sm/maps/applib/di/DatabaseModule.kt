package com.sm.maps.applib.di

import android.content.Context
import androidx.room.Room
import com.sm.maps.applib.data.local.database.RMapsDatabase
import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.local.database.dao.PoiDao
import com.sm.maps.applib.data.local.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Room database and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provide singleton instance of RMapsDatabase
     */
    @Provides
    @Singleton
    fun provideRMapsDatabase(
        @ApplicationContext context: Context
    ): RMapsDatabase {
        return Room.databaseBuilder(
            context,
            RMapsDatabase::class.java,
            RMapsDatabase.DATABASE_NAME
        )
            .addMigrations(*RMapsDatabase.MIGRATIONS.toTypedArray())
            // TODO: Remove fallbackToDestructiveMigration in production
            // .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provide TrackDao from database
     */
    @Provides
    fun provideTrackDao(database: RMapsDatabase): TrackDao {
        return database.trackDao()
    }

    /**
     * Provide PoiDao from database
     */
    @Provides
    fun providePoiDao(database: RMapsDatabase): PoiDao {
        return database.poiDao()
    }

    /**
     * Provide CategoryDao from database
     */
    @Provides
    fun provideCategoryDao(database: RMapsDatabase): CategoryDao {
        return database.categoryDao()
    }
}
