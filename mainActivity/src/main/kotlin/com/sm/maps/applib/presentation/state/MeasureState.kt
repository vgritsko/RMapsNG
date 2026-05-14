package com.sm.maps.applib.presentation.state

data class MeasureState(
    val isActive: Boolean = false,
    val pointCount: Int = 0,
    val totalDistance: Double = 0.0,
    val showInfoBubble: Boolean = true,
    val showLineInfo: Boolean = true
)
