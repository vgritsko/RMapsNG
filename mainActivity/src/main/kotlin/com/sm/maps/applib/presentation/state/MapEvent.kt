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
    data class MapSourceChanged(val mapId: String, val overlayId: String, val showOverlay: Boolean) : MapEvent()
    data object OverlayRefreshRequested : MapEvent()
    data class CompassToggled(val enabled: Boolean) : MapEvent()
    data class MapViewportChanged(val latE6: Int, val lonE6: Int, val zoom: Int) : MapEvent()
    data class RotationChanged(val bearing: Float) : MapEvent()
}
