package com.sm.maps.applib.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.repository.ITrackRepository
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
class TrackListViewModel @Inject constructor(
    private val trackRepository: ITrackRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseViewModel() {

    val trackListState: StateFlow<UiState<List<Track>>> = trackRepository
        .getAllTracks()
        .map { tracks -> if (tracks.isEmpty()) UiState.Empty else UiState.Success(tracks) }
        .catch { e -> emit(UiState.Error(e)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun deleteTrack(track: Track) {
        launchWithLoading(showLoading = false) {
            trackRepository.deleteTrack(track)
        }
    }

    fun toggleTrackVisibility(id: Int) {
        launchWithLoading(showLoading = false) {
            trackRepository.toggleTrackVisibility(id)
        }
    }
}
