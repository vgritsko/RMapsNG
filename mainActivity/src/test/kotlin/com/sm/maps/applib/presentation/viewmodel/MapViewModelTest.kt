package com.sm.maps.applib.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapViewModelTest {

    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        viewModel = MapViewModel()
    }

    @Test
    fun initialize_setsAllFields() {
        viewModel.initialize(
            latE6 = 55_123456,
            lonE6 = 37_654321,
            zoomLevel = 12,
            autoFollow = false,
            drivingDirectionUp = true,
            northDirectionUp = false
        )
        val state = viewModel.state.value
        assertEquals(55_123456, state.latE6)
        assertEquals(37_654321, state.lonE6)
        assertEquals(12, state.zoomLevel)
        assertEquals(false, state.autoFollow)
        assertEquals(true, state.drivingDirectionUp)
        assertEquals(false, state.northDirectionUp)
    }

    @Test
    fun updateViewport_updatesOnlyThreeFields() {
        viewModel.initialize(0, 0, 0, false, true, true)
        viewModel.updateViewport(10, 20, 5)
        val state = viewModel.state.value
        assertEquals(10, state.latE6)
        assertEquals(20, state.lonE6)
        assertEquals(5, state.zoomLevel)
        assertEquals(false, state.autoFollow)
    }

    @Test
    fun updateBearing_updatesBearingOnly() {
        viewModel.initialize(0, 0, 0, true, true, true)
        viewModel.updateBearing(90f)
        assertEquals(90f, viewModel.state.value.bearing, 0.01f)
        assertEquals(true, viewModel.state.value.autoFollow)
    }

    @Test
    fun updateAutoFollow_togglesCorrectly() {
        viewModel.initialize(0, 0, 0, true, true, true)
        viewModel.updateAutoFollow(false)
        assertEquals(false, viewModel.state.value.autoFollow)
        viewModel.updateAutoFollow(true)
        assertEquals(true, viewModel.state.value.autoFollow)
    }
}
