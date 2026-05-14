package com.sm.maps.applib.presentation.state

import android.location.Location

sealed class MapEvent {
    data class LocationUpdated(val location: Location) : MapEvent()
    data class BearingChanged(val bearing: Float) : MapEvent()
    data class AutoFollowChanged(val enabled: Boolean) : MapEvent()
    data class CenterOnLocationRequested(val location: Location?) : MapEvent()
}
