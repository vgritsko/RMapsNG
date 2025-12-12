package com.sm.maps

import com.sm.maps.applib.MapApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for RMaps Free (application module)
 * Extends MapApplication from the library module and adds Hilt support
 *
 * @HiltAndroidApp triggers Hilt code generation and must be in the application module
 */
@HiltAndroidApp
class RMapsFreeApplication : MapApplication()
