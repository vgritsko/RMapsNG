package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.state.MapSourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MapSourceViewModel @Inject constructor() : BaseViewModel() {

    private val _mapSourceState = MutableStateFlow(MapSourceState())
    val mapSourceState: StateFlow<MapSourceState> = _mapSourceState.asStateFlow()

    fun initialize(mapId: String, overlayId: String, showOverlay: Boolean) {
        _mapSourceState.update { it.copy(mapId = mapId, overlayId = overlayId, showOverlay = showOverlay) }
    }

    fun selectMapSource(mapId: String, overlayId: String = "", showOverlay: Boolean = true) {
        _mapSourceState.update { it.copy(mapId = mapId, overlayId = overlayId, showOverlay = showOverlay) }
    }

    fun setOverlayVisibility(overlayId: String, show: Boolean) {
        _mapSourceState.update { it.copy(overlayId = overlayId, showOverlay = show) }
    }
}
