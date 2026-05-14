package com.sm.maps.applib.presentation.state

import android.location.Location

sealed class MapEvent {
    data class LocationUpdated(val location: Location) : MapEvent()
    data class BearingChanged(val bearing: Float) : MapEvent()
    data class AutoFollowChanged(val enabled: Boolean) : MapEvent()
    data class CenterOnLocationRequested(val location: Location?) : MapEvent()
    data object MeasureToolOpen : MapEvent()
    data object MeasureToolClose : MapEvent()
    data object MeasureAddPointOnCenter : MapEvent()
    data object MeasureClear : MapEvent()
    data object MeasureUndo : MapEvent()
    data class MeasureShowInfoBubble(val show: Boolean) : MapEvent()
    data class MeasureShowLineInfo(val show: Boolean) : MapEvent()
}
