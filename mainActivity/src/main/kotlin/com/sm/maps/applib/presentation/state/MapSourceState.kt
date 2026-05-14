package com.sm.maps.applib.presentation.state

data class MapSourceState(
    val mapId: String = "mapnik",
    val overlayId: String = "",
    val showOverlay: Boolean = true
)
