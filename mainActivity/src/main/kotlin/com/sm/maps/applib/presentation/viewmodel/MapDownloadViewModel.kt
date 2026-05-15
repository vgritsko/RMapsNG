package com.sm.maps.applib.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sm.maps.applib.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DownloadPhase { AREA_SELECTION, ZOOM_SELECTION, DOWNLOADING }

data class AreaCoords(val lat0: Int, val lon0: Int, val lat1: Int, val lon1: Int)

data class DownloadState(
    val mapId: String = "",
    val mapName: String = "",
    val centerLat: Int = 0,
    val centerLon: Int = 0,
    val zoomLevel: Int = 0,
    val selectedArea: AreaCoords? = null,
    val selectedZooms: IntArray = intArrayOf(),
    val fileName: String = "NewFile",
    val isOnlineCache: Boolean = false,
    val overwriteFile: Boolean = true,
    val overwriteTiles: Boolean = false,
    val phase: DownloadPhase = DownloadPhase.AREA_SELECTION,
    val tileCntTotal: Int = 0,
    val tileCntDone: Int = 0,
    val errorCnt: Int = 0,
    val startTime: Long = 0L,
    val isDownloadDone: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadState) return false
        return mapId == other.mapId &&
            mapName == other.mapName &&
            centerLat == other.centerLat &&
            centerLon == other.centerLon &&
            zoomLevel == other.zoomLevel &&
            selectedArea == other.selectedArea &&
            selectedZooms.contentEquals(other.selectedZooms) &&
            fileName == other.fileName &&
            isOnlineCache == other.isOnlineCache &&
            overwriteFile == other.overwriteFile &&
            overwriteTiles == other.overwriteTiles &&
            phase == other.phase &&
            tileCntTotal == other.tileCntTotal &&
            tileCntDone == other.tileCntDone &&
            errorCnt == other.errorCnt &&
            startTime == other.startTime &&
            isDownloadDone == other.isDownloadDone
    }

    override fun hashCode(): Int {
        var result = mapId.hashCode()
        result = 31 * result + phase.hashCode()
        result = 31 * result + tileCntDone
        result = 31 * result + selectedZooms.contentHashCode()
        return result
    }
}

@HiltViewModel
class MapDownloadViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    fun initDownload(mapId: String, mapName: String, lat: Int, lon: Int, zoom: Int) {
        _state.update { it.copy(mapId = mapId, mapName = mapName, centerLat = lat, centerLon = lon, zoomLevel = zoom) }
    }

    fun setSelectedArea(coords: AreaCoords) {
        _state.update { it.copy(selectedArea = coords) }
    }

    fun proceedToZoomSelection(coords: AreaCoords) {
        _state.update { it.copy(selectedArea = coords, phase = DownloadPhase.ZOOM_SELECTION) }
    }

    fun backToAreaSelection() {
        _state.update { it.copy(phase = DownloadPhase.AREA_SELECTION) }
    }

    fun startDownload(
        zooms: IntArray,
        fileName: String,
        isOnlineCache: Boolean,
        overwriteFile: Boolean,
        overwriteTiles: Boolean
    ) {
        _state.update {
            it.copy(
                selectedZooms = zooms,
                fileName = fileName,
                isOnlineCache = isOnlineCache,
                overwriteFile = overwriteFile,
                overwriteTiles = overwriteTiles,
                phase = DownloadPhase.DOWNLOADING,
                tileCntTotal = 0,
                tileCntDone = 0,
                errorCnt = 0,
                isDownloadDone = false
            )
        }
    }

    fun onDownloadStarted(tileCnt: Int, startTime: Long) {
        _state.update { it.copy(tileCntTotal = tileCnt, startTime = startTime) }
    }

    fun updateProgress(tileCntDone: Int, errorCnt: Int) {
        _state.update { it.copy(tileCntDone = tileCntDone, errorCnt = errorCnt) }
    }

    fun onDownloadDone() {
        _state.update { it.copy(isDownloadDone = true) }
    }
}
