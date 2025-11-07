package com.sm.maps.applib.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sm.maps.applib.data.local.database.entity.*
import com.sm.maps.applib.data.local.database.dao.*
import com.sm.maps.applib.data.local.database.migration.MIGRATION_22_23

@Database(
    entities = [
        PoiEntity::class,
        TrackEntity::class,
        TrackPointEntity::class,
        CategoryEntity::class,
        ActivityEntity::class,
        MapEntity::class
    ],
    version = 23,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RMapsDatabase : RoomDatabase() {

    abstract fun poiDao(): PoiDao
    abstract fun trackDao(): TrackDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "rmaps.db"

        val MIGRATIONS = listOf(
            MIGRATION_22_23
        )
    }
}
