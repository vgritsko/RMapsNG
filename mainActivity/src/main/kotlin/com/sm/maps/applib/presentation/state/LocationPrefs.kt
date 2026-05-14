package com.sm.maps.applib.presentation.state

data class LocationPrefs(
    val gpsProvider: Boolean = true,
    val networkProvider: Boolean = true,
    val fastUpdate: Boolean = true,
    val drivingDirectionUp: Boolean = true,
    val northDirectionUp: Boolean = true
)
