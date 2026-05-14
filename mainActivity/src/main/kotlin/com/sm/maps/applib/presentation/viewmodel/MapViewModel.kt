package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.state.MapViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(MapViewState())
    val state: StateFlow<MapViewState> = _state.asStateFlow()

    fun initialize(
        latE6: Int,
        lonE6: Int,
        zoomLevel: Int,
        autoFollow: Boolean,
        drivingDirectionUp: Boolean,
        northDirectionUp: Boolean
    ) {
        _state.update {
            it.copy(
                latE6 = latE6,
                lonE6 = lonE6,
                zoomLevel = zoomLevel,
                autoFollow = autoFollow,
                drivingDirectionUp = drivingDirectionUp,
                northDirectionUp = northDirectionUp
            )
        }
    }

    fun updateViewport(latE6: Int, lonE6: Int, zoomLevel: Int) {
        _state.update { it.copy(latE6 = latE6, lonE6 = lonE6, zoomLevel = zoomLevel) }
    }

    fun updateBearing(bearing: Float) {
        _state.update { it.copy(bearing = bearing) }
    }

    fun updateAutoFollow(enabled: Boolean) {
        _state.update { it.copy(autoFollow = enabled) }
    }
}
