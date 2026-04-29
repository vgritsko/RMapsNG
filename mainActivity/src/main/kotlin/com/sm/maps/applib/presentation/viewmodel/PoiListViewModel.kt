package com.sm.maps.applib.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.model.PoiPoint
import com.sm.maps.applib.domain.repository.IPoiRepository
import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PoiListViewModel @Inject constructor(
    private val poiRepository: IPoiRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel() {

    val poiListState: StateFlow<UiState<List<PoiPoint>>> = poiRepository
        .getAllPois()
        .map { pois -> if (pois.isEmpty()) UiState.Empty else UiState.Success(pois) }
        .catch { e -> emit(UiState.Error(e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun deletePoi(poi: PoiPoint) {
        launchWithLoading(showLoading = false) {
            poiRepository.deletePoi(poi)
        }
    }

    fun togglePoiVisibility(poi: PoiPoint) {
        launchWithLoading(showLoading = false) {
            poiRepository.updatePoi(poi.copy(hidden = !poi.hidden))
        }
    }
}
