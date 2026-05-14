package com.sm.maps.applib.presentation.ui.map

import android.location.Location
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sm.maps.applib.presentation.state.MapEvent
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

class MapEventBridge(
    private val lifecycleOwner: LifecycleOwner,
    private val coordinatorViewModel: MapCoordinatorViewModel
) {
    interface Listener {
        fun onLocationUpdated(location: Location)
        fun onBearingChanged(bearing: Float)
        fun onAutoFollowChanged(enabled: Boolean)
        fun onCenterOnLocationRequested(location: Location?)
        fun onMeasureToolOpen()
        fun onMeasureToolClose()
        fun onMeasureAddPointOnCenter()
        fun onMeasureClear()
        fun onMeasureUndo()
        fun onMeasureShowInfoBubble(show: Boolean)
        fun onMeasureShowLineInfo(show: Boolean)
        fun onMapSourceChanged(mapId: String, overlayId: String, showOverlay: Boolean)
        fun onOverlayRefreshRequested()
        fun onCompassToggled(enabled: Boolean)
        fun onMapViewportChanged(latE6: Int, lonE6: Int, zoom: Int)
        fun onRotationChanged(bearing: Float)
    }

    fun observe(listener: Listener) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                coordinatorViewModel.mapEvents.collect { event ->
                    when (event) {
                        is MapEvent.LocationUpdated -> listener.onLocationUpdated(event.location)
                        is MapEvent.BearingChanged -> listener.onBearingChanged(event.bearing)
                        is MapEvent.AutoFollowChanged -> listener.onAutoFollowChanged(event.enabled)
                        is MapEvent.CenterOnLocationRequested -> listener.onCenterOnLocationRequested(event.location)
                        is MapEvent.MeasureToolOpen -> listener.onMeasureToolOpen()
                        is MapEvent.MeasureToolClose -> listener.onMeasureToolClose()
                        is MapEvent.MeasureAddPointOnCenter -> listener.onMeasureAddPointOnCenter()
                        is MapEvent.MeasureClear -> listener.onMeasureClear()
                        is MapEvent.MeasureUndo -> listener.onMeasureUndo()
                        is MapEvent.MeasureShowInfoBubble -> listener.onMeasureShowInfoBubble(event.show)
                        is MapEvent.MeasureShowLineInfo -> listener.onMeasureShowLineInfo(event.show)
                        is MapEvent.MapSourceChanged -> listener.onMapSourceChanged(event.mapId, event.overlayId, event.showOverlay)
                        is MapEvent.OverlayRefreshRequested -> listener.onOverlayRefreshRequested()
                        is MapEvent.CompassToggled -> listener.onCompassToggled(event.enabled)
                        is MapEvent.MapViewportChanged -> listener.onMapViewportChanged(event.latE6, event.lonE6, event.zoom)
                        is MapEvent.RotationChanged -> listener.onRotationChanged(event.bearing)
                    }
                }
            }
        }
    }
}
