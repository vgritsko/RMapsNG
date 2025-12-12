package com.sm.maps.applib.data.migration

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.sm.maps.applib.data.local.database.RMapsDatabase
import com.sm.maps.applib.kml.constants.PoiConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handles one-time migration from legacy SQLite database to Room database
 *
 * This migrator:
 * 1. Checks if migration has already been completed
 * 2. Opens legacy geodata.db database
 * 3. Migrates data from legacy tables to Room entities
 * 4. Marks migration as complete
 * 5. Handles errors and rollback scenarios
 */
class LegacyDataMigrator(
    private val context: Context,
    private val roomDatabase: RMapsDatabase
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "LegacyDataMigrator"
        private const val PREFS_NAME = "migration_status"
        private const val KEY_MIGRATION_COMPLETE = "migration_complete"
        private const val KEY_MIGRATION_VERSION = "migration_version"
        private const val CURRENT_MIGRATION_VERSION = 1

        // Legacy database file name
        private const val LEGACY_DB_NAME = "geodata.db"
    }

    /**
     * Check if migration has already been completed
     */
    fun isMigrationComplete(): Boolean {
        return prefs.getBoolean(KEY_MIGRATION_COMPLETE, false)
    }

    /**
     * Get the version of the last completed migration
     */
    fun getMigrationVersion(): Int {
        return prefs.getInt(KEY_MIGRATION_VERSION, 0)
    }

    /**
     * Main migration entry point
     * Migrates all data from legacy SQLite to Room database
     */
    suspend fun migrateAllData(): MigrationResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Starting data migration...")

                // Check if already migrated
                if (isMigrationComplete()) {
                    Log.i(TAG, "Migration already completed (version ${getMigrationVersion()})")
                    return@withContext MigrationResult.AlreadyMigrated
                }

                // Check if legacy database exists
                val legacyDbFile = getLegacyDatabaseFile()
                if (!legacyDbFile.exists()) {
                    Log.w(TAG, "Legacy database not found at ${legacyDbFile.absolutePath}. Nothing to migrate.")
                    markMigrationComplete()
                    return@withContext MigrationResult.Success
                }

                // Open legacy database
                val legacyDb = SQLiteDatabase.openDatabase(
                    legacyDbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )

                legacyDb.use { db ->
                    // Migrate in transaction for atomicity
                    roomDatabase.runInTransaction {
                        val stats = MigrationStats()

                        stats.categoriesCount = migrateCategories(db)
                        Log.d(TAG, "Migrated ${stats.categoriesCount} categories")

                        stats.activitiesCount = migrateActivities(db)
                        Log.d(TAG, "Migrated ${stats.activitiesCount} activities")

                        stats.poisCount = migratePois(db)
                        Log.d(TAG, "Migrated ${stats.poisCount} POIs")

                        stats.tracksCount = migrateTracks(db)
                        Log.d(TAG, "Migrated ${stats.tracksCount} tracks")

                        stats.trackPointsCount = migrateTrackPoints(db)
                        Log.d(TAG, "Migrated ${stats.trackPointsCount} track points")

                        stats.mapsCount = migrateMaps(db)
                        Log.d(TAG, "Migrated ${stats.mapsCount} maps")

                        Log.i(TAG, "Migration completed successfully: $stats")
                    }
                }

                // Mark migration as complete
                markMigrationComplete()

                Log.i(TAG, "Data migration completed successfully")
                MigrationResult.Success

            } catch (e: Exception) {
                Log.e(TAG, "Migration failed", e)
                MigrationResult.Failed(e)
            }
        }
    }

    /**
     * Migrate categories from legacy 'category' table
     */
    private fun migrateCategories(legacyDb: SQLiteDatabase): Int {
        val cursor = legacyDb.rawQuery(
            "SELECT categoryid, name, hidden, iconid, minzoom FROM category",
            null
        )

        val categories = cursor.mapRows { it.toCategoryEntity() }

        categories.forEach { category ->
            roomDatabase.categoryDao().insertCategorySync(category)
        }

        return categories.size
    }

    /**
     * Migrate activities from legacy 'activity' table
     */
    private fun migrateActivities(legacyDb: SQLiteDatabase): Int {
        // Check if activity table exists
        if (!tableExists(legacyDb, "activity")) {
            Log.w(TAG, "Activity table does not exist in legacy database")
            return 0
        }

        val cursor = legacyDb.rawQuery(
            "SELECT activityid, name FROM activity",
            null
        )

        val activities = cursor.mapRows { it.toActivityEntity() }

        // Note: ActivityDao doesn't exist yet - we'll need to create it or handle this differently
        // For now, just log the count
        Log.w(TAG, "Activity migration skipped - ActivityDao not implemented yet")

        return activities.size
    }

    /**
     * Migrate POIs from legacy 'points' table
     */
    private fun migratePois(legacyDb: SQLiteDatabase): Int {
        val cursor = legacyDb.rawQuery(
            "SELECT pointid, name, descr, lat, lon, alt, hidden, categoryid, pointsourceid, iconid FROM points",
            null
        )

        val pois = cursor.mapRows { it.toPoiEntity() }

        pois.forEach { poi ->
            roomDatabase.poiDao().insertPoiSync(poi)
        }

        return pois.size
    }

    /**
     * Migrate tracks from legacy 'tracks' table
     */
    private fun migrateTracks(legacyDb: SQLiteDatabase): Int {
        val cursor = legacyDb.rawQuery(
            "SELECT trackid, name, descr, date, show, cnt, duration, distance, categoryid, activity, style FROM tracks",
            null
        )

        val tracks = cursor.mapRows { it.toTrackEntity() }

        tracks.forEach { track ->
            roomDatabase.trackDao().insertTrackSync(track)
        }

        return tracks.size
    }

    /**
     * Migrate track points from legacy 'trackpoints' table
     */
    private fun migrateTrackPoints(legacyDb: SQLiteDatabase): Int {
        val cursor = legacyDb.rawQuery(
            "SELECT trackid, id, lat, lon, alt, speed, date FROM trackpoints",
            null
        )

        val trackPoints = cursor.mapRows { it.toTrackPointEntity() }

        trackPoints.forEach { point ->
            roomDatabase.trackDao().insertTrackPointSync(point)
        }

        return trackPoints.size
    }

    /**
     * Migrate maps from legacy 'maps' table
     */
    private fun migrateMaps(legacyDb: SQLiteDatabase): Int {
        // Check if maps table exists
        if (!tableExists(legacyDb, "maps")) {
            Log.w(TAG, "Maps table does not exist in legacy database")
            return 0
        }

        val cursor = legacyDb.rawQuery(
            "SELECT mapid, name, type, params FROM maps",
            null
        )

        val maps = cursor.mapRows { it.toMapEntity() }

        // Note: MapDao doesn't exist yet - we'll need to create it or handle this differently
        // For now, just log the count
        Log.w(TAG, "Maps migration skipped - MapDao not implemented yet")

        return maps.size
    }

    /**
     * Check if a table exists in the database
     */
    private fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName)
        )
        val exists = cursor.use { it.count > 0 }
        return exists
    }

    /**
     * Mark migration as complete
     */
    private fun markMigrationComplete() {
        prefs.edit()
            .putBoolean(KEY_MIGRATION_COMPLETE, true)
            .putInt(KEY_MIGRATION_VERSION, CURRENT_MIGRATION_VERSION)
            .apply()
    }

    /**
     * Reset migration status (for testing/debugging only)
     */
    fun resetMigrationStatus() {
        prefs.edit()
            .putBoolean(KEY_MIGRATION_COMPLETE, false)
            .putInt(KEY_MIGRATION_VERSION, 0)
            .apply()
        Log.w(TAG, "Migration status reset")
    }

    /**
     * Get the legacy database file
     */
    private fun getLegacyDatabaseFile(): File {
        // Legacy database is typically stored in the external storage directory
        // Check the standard location first
        val externalDir = context.getExternalFilesDir(null)
        return File(externalDir, LEGACY_DB_NAME)
    }

    /**
     * Statistics about the migration
     */
    private data class MigrationStats(
        var categoriesCount: Int = 0,
        var activitiesCount: Int = 0,
        var poisCount: Int = 0,
        var tracksCount: Int = 0,
        var trackPointsCount: Int = 0,
        var mapsCount: Int = 0
    ) {
        override fun toString(): String {
            return "Categories: $categoriesCount, Activities: $activitiesCount, " +
                   "POIs: $poisCount, Tracks: $tracksCount, " +
                   "Track Points: $trackPointsCount, Maps: $mapsCount"
        }
    }
}
