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
                    }
                }
            }
        }
    }
}
