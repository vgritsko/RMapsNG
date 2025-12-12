package com.sm.maps.applib

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.sm.maps.applib.data.migration.MigrationHelper
import java.util.Locale

/**
 * Base Application class for RMaps library module
 * This class contains core application logic and can be extended by app modules
 *
 * Note: The extending class in the application module should have @HiltAndroidApp annotation
 */
open class MapApplication : Application() {

    companion object {
        private const val TAG = "MapApplication"
    }

    private lateinit var defLocale: Locale

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "RMaps Application starting...")

        // Store the default locale
        defLocale = Locale.getDefault()

        // Trigger data migration from legacy SQLite to Room database
        // This runs in background and won't block app startup
        try {
            MigrationHelper.triggerMigrationIfNeeded(this)
            Log.i(TAG, "Migration check initiated")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking migration status", e)
            // Don't crash - app can still function with legacy database
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Clear any caches if needed
    }

    /**
     * Get the default locale set at application startup
     * @return the default locale
     */
    fun getDefLocale(): Locale = defLocale
}
