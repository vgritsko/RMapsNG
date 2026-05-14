package com.sm.maps.applib.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.state.MapEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapCoordinatorViewModel @Inject constructor() : BaseViewModel() {

    private val _mapEvents = MutableSharedFlow<MapEvent>(extraBufferCapacity = 16)
    val mapEvents: SharedFlow<MapEvent> = _mapEvents.asSharedFlow()

    fun emitEvent(event: MapEvent) {
        viewModelScope.launch { _mapEvents.emit(event) }
    }
}
