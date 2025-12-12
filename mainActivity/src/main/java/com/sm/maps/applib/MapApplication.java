package com.sm.maps.applib;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;
import java.util.Locale;

import com.sm.maps.applib.data.migration.MigrationHelper;

/**
 * Main Application class for RMaps
 * This class is instantiated when the app process starts
 */
public class MapApplication extends Application {

    private static final String TAG = "MapApplication";
    private Locale defLocale;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "RMaps Application starting...");

        // Store the default locale
        defLocale = Locale.getDefault();

        // Trigger data migration from legacy SQLite to Room database
        // This runs in background and won't block app startup
        try {
            MigrationHelper.INSTANCE.triggerMigrationIfNeeded(this);
            Log.i(TAG, "Migration check initiated");
        } catch (Exception e) {
            Log.e(TAG, "Error checking migration status", e);
            // Don't crash - app can still function with legacy database
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Clear any caches if needed
    }

    /**
     * Get the default locale set at application startup
     * @return the default locale
     */
    public Locale getDefLocale() {
        return defLocale;
    }
}
