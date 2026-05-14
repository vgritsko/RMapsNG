package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.state.MapEvent
import com.sm.maps.applib.presentation.state.MeasureState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MeasureToolViewModel @Inject constructor(
    private val coordinatorViewModel: MapCoordinatorViewModel
) : BaseViewModel() {

    private val _measureState = MutableStateFlow(MeasureState())
    val measureState: StateFlow<MeasureState> = _measureState.asStateFlow()

    fun initialize(showInfoBubble: Boolean, showLineInfo: Boolean) {
        _measureState.update { it.copy(showInfoBubble = showInfoBubble, showLineInfo = showLineInfo) }
    }

    fun openTool() {
        _measureState.update { it.copy(isActive = true) }
    }

    fun closeTool() {
        _measureState.update { it.copy(isActive = false, pointCount = 0, totalDistance = 0.0) }
    }

    fun addPointOnCenter() {
        coordinatorViewModel.emitEvent(MapEvent.MeasureAddPointOnCenter)
    }

    fun clear() {
        _measureState.update { it.copy(pointCount = 0, totalDistance = 0.0) }
    }

    fun undo() {
        coordinatorViewModel.emitEvent(MapEvent.MeasureUndo)
    }

    fun setShowInfoBubble(show: Boolean) {
        _measureState.update { it.copy(showInfoBubble = show) }
    }

    fun setShowLineInfo(show: Boolean) {
        _measureState.update { it.copy(showLineInfo = show) }
    }

    fun onPointAdded(newCount: Int, newDistance: Double) {
        _measureState.update { it.copy(pointCount = newCount, totalDistance = newDistance) }
    }
}
