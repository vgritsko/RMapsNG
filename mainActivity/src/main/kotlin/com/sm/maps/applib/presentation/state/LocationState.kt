package com.sm.maps.applib.presentation.state

import android.location.Location

data class LocationState(
    val currentLocation: Location? = null,
    val providerName: String = "",
    val satelliteCount: Int = -1,
    val providerStatus: Int = -1,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val isFollowingLocation: Boolean = true,
    val isCompassEnabled: Boolean = false,
    val compassAzimuth: Float = 0f
)
