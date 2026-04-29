package com.sm.maps.applib.domain.model

data class TrackPoint(
    val id: Int = 0,
    val trackId: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Double = 0.0,
    val date: Long = 0L
)
