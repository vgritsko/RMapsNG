package com.sm.maps.applib.data.migration

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.sm.maps.applib.data.local.database.RMapsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper class to trigger data migration from Application class
 *
 * This provides a simple way to trigger migration without Hilt dependency injection.
 * Once Hilt is set up (Phase 1), this can be replaced with proper DI.
 */
object MigrationHelper {

    private const val TAG = "MigrationHelper"
    private var migrationInProgress = false

    /**
     * Trigger migration in the background
     * Call this from Application.onCreate()
     */
    fun triggerMigrationIfNeeded(context: Context) {
        if (migrationInProgress) {
            Log.w(TAG, "Migration already in progress")
            return
        }

        migrationInProgress = true

        // Run migration in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Checking if migration is needed...")

                // Build Room database instance
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    RMapsDatabase::class.java,
                    RMapsDatabase.DATABASE_NAME
                )
                    .addMigrations(*RMapsDatabase.MIGRATIONS.toTypedArray())
                    .build()

                // Create migrator
                val migrator = LegacyDataMigrator(
                    context = context.applicationContext,
                    roomDatabase = database
                )

                // Check if migration needed
                if (migrator.isMigrationComplete()) {
                    Log.i(TAG, "Migration already completed (version ${migrator.getMigrationVersion()})")
                    migrationInProgress = false
                    return@launch
                }

                Log.i(TAG, "Starting migration...")

                // Perform migration
                when (val result = migrator.migrateAllData()) {
                    is MigrationResult.Success -> {
                        Log.i(TAG, "✅ Migration completed successfully")
                    }
                    is MigrationResult.AlreadyMigrated -> {
                        Log.i(TAG, "ℹ️ Migration was already completed")
                    }
                    is MigrationResult.Failed -> {
                        Log.e(TAG, "❌ Migration failed", result.exception)
                        // Don't crash the app, just log the error
                        // User can still use legacy database
                    }
                    is MigrationResult.Skipped -> {
                        Log.i(TAG, "⏭️ Migration skipped")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during migration", e)
            } finally {
                migrationInProgress = false
            }
        }
    }

    /**
     * Check if migration is complete (synchronous check)
     */
    fun isMigrationComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences("migration_status", Context.MODE_PRIVATE)
        return prefs.getBoolean("migration_complete", false)
    }

    /**
     * For testing/debugging: reset migration status
     */
    fun resetMigrationForTesting(context: Context) {
        val prefs = context.getSharedPreferences("migration_status", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("migration_complete", false)
            .putInt("migration_version", 0)
            .apply()
        Log.w(TAG, "⚠️ Migration status reset (testing only)")
    }
}
