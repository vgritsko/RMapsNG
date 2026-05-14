package com.sm.maps.applib.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OverlayControlViewModelTest {

    private lateinit var viewModel: OverlayControlViewModel

    @Before
    fun setup() {
        viewModel = OverlayControlViewModel()
    }

    @Test
    fun initialize_setsCompassEnabled() {
        viewModel.initialize(compassEnabled = true)
        assertEquals(true, viewModel.state.value.compassEnabled)
    }

    @Test
    fun initialize_defaultIsFalse() {
        assertEquals(false, viewModel.state.value.compassEnabled)
    }

    @Test
    fun toggleCompass_invertsFalseToTrue() {
        viewModel.initialize(false)
        viewModel.toggleCompass()
        assertEquals(true, viewModel.state.value.compassEnabled)
    }

    @Test
    fun toggleCompass_invertsTrueToFalse() {
        viewModel.initialize(true)
        viewModel.toggleCompass()
        assertEquals(false, viewModel.state.value.compassEnabled)
    }

    @Test
    fun toggleCompass_calledTwice_returnsOriginal() {
        viewModel.initialize(true)
        viewModel.toggleCompass()
        viewModel.toggleCompass()
        assertEquals(true, viewModel.state.value.compassEnabled)
    }
}
