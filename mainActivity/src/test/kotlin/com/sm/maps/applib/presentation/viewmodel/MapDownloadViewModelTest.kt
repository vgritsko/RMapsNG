package com.sm.maps.applib.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MapDownloadViewModelTest {

    private lateinit var viewModel: MapDownloadViewModel

    @Before
    fun setup() {
        viewModel = MapDownloadViewModel()
    }

    @Test
    fun initDownload_setsCorrectFields() {
        viewModel.initDownload("mapnik", "Mapnik", 55_000000, 37_000000, 10)
        val state = viewModel.state.value
        assertEquals("mapnik", state.mapId)
        assertEquals(55_000000, state.centerLat)
        assertEquals(37_000000, state.centerLon)
        assertEquals(10, state.zoomLevel)
        assertEquals(DownloadPhase.AREA_SELECTION, state.phase)
    }

    @Test
    fun proceedToZoomSelection_setsPhaseAndArea() {
        val coords = AreaCoords(10, 20, 30, 40)
        viewModel.proceedToZoomSelection(coords)
        val state = viewModel.state.value
        assertEquals(DownloadPhase.ZOOM_SELECTION, state.phase)
        assertEquals(coords, state.selectedArea)
    }

    @Test
    fun backToAreaSelection_setsPhaseBack() {
        viewModel.proceedToZoomSelection(AreaCoords(0, 0, 0, 0))
        viewModel.backToAreaSelection()
        assertEquals(DownloadPhase.AREA_SELECTION, viewModel.state.value.phase)
    }

    @Test
    fun startDownload_setsCorrectPhaseAndParams() {
        val zooms = intArrayOf(5, 6, 7)
        viewModel.startDownload(zooms, "mymap", false, true, false)
        val state = viewModel.state.value
        assertEquals(DownloadPhase.DOWNLOADING, state.phase)
        assertEquals("mymap", state.fileName)
        assertFalse(state.isOnlineCache)
        assertTrue(state.overwriteFile)
        assertFalse(state.overwriteTiles)
        assertTrue(state.selectedZooms.contentEquals(zooms))
        assertFalse(state.isDownloadDone)
    }

    @Test
    fun updateProgress_updatesState() {
        viewModel.updateProgress(42, 3)
        val state = viewModel.state.value
        assertEquals(42, state.tileCntDone)
        assertEquals(3, state.errorCnt)
    }

    @Test
    fun onDownloadStarted_setsTotalAndTime() {
        viewModel.onDownloadStarted(100, 1_000_000L)
        val state = viewModel.state.value
        assertEquals(100, state.tileCntTotal)
        assertEquals(1_000_000L, state.startTime)
    }

    @Test
    fun onDownloadDone_setsFlag() {
        viewModel.onDownloadDone()
        assertTrue(viewModel.state.value.isDownloadDone)
    }
}
