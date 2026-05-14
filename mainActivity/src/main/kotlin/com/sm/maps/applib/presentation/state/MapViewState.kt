package com.sm.maps.applib.presentation.state

data class MapViewState(
    val latE6: Int = 0,
    val lonE6: Int = 0,
    val zoomLevel: Int = 0,
    val bearing: Float = 0f,
    val autoFollow: Boolean = true,
    val drivingDirectionUp: Boolean = true,
    val northDirectionUp: Boolean = true
)
