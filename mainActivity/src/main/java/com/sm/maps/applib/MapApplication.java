package com.sm.maps.applib;

import android.app.Application;
import android.content.res.Configuration;
import java.util.Locale;

/**
 * Main Application class for RMaps
 * This class is instantiated when the app process starts
 */
public class MapApplication extends Application {

    private Locale defLocale;

    @Override
    public void onCreate() {
        super.onCreate();

        // Store the default locale
        defLocale = Locale.getDefault();
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
