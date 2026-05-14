package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.state.OverlayControlState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class OverlayControlViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(OverlayControlState())
    val state: StateFlow<OverlayControlState> = _state.asStateFlow()

    private val _refreshEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvents: SharedFlow<Unit> = _refreshEvents.asSharedFlow()

    fun initialize(compassEnabled: Boolean) {
        _state.update { it.copy(compassEnabled = compassEnabled) }
    }

    fun toggleCompass() {
        _state.update { it.copy(compassEnabled = !it.compassEnabled) }
    }

    fun requestOverlayRefresh() {
        _refreshEvents.tryEmit(Unit)
    }
}
