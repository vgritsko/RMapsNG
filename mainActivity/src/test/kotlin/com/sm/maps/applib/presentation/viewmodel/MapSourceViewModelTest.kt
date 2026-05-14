package com.sm.maps.applib.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapSourceViewModelTest {

    private lateinit var viewModel: MapSourceViewModel

    @Before
    fun setup() {
        viewModel = MapSourceViewModel()
    }

    @Test
    fun initialize_setsMapId() {
        viewModel.initialize("osm", "overlay1", true)
        val state = viewModel.mapSourceState.value
        assertEquals("osm", state.mapId)
        assertEquals("overlay1", state.overlayId)
        assertEquals(true, state.showOverlay)
    }

    @Test
    fun selectMapSource_updatesAllFields() {
        viewModel.initialize("osm", "", true)
        viewModel.selectMapSource("mapnik", "overlay2", false)
        val state = viewModel.mapSourceState.value
        assertEquals("mapnik", state.mapId)
        assertEquals("overlay2", state.overlayId)
        assertEquals(false, state.showOverlay)
    }

    @Test
    fun setOverlayVisibility_updatesOverlayFields() {
        viewModel.initialize("osm", "old", true)
        viewModel.setOverlayVisibility("new_overlay", false)
        val state = viewModel.mapSourceState.value
        assertEquals("new_overlay", state.overlayId)
        assertEquals(false, state.showOverlay)
        assertEquals("osm", state.mapId)
    }
}
