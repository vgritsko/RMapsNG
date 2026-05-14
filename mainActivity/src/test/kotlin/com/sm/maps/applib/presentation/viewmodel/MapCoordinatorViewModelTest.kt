package com.sm.maps.applib.presentation.viewmodel

import android.location.Location
import com.sm.maps.applib.presentation.state.MapEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapCoordinatorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MapCoordinatorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MapCoordinatorViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emitEvent_locationUpdated_receivedByCollector() = runTest(testDispatcher) {
        val location = Location("gps")
        var received: MapEvent? = null

        val collectJob: Job = launch(testDispatcher) {
            viewModel.mapEvents.collect { received = it }
        }

        viewModel.emitEvent(MapEvent.LocationUpdated(location))

        assertNotNull("Expected event to be received", received)
        assertTrue("Expected LocationUpdated event", received is MapEvent.LocationUpdated)
        assertSame("Expected same Location instance", location, (received as MapEvent.LocationUpdated).location)

        collectJob.cancel()
    }

    @Test
    fun emitEvent_autoFollowChanged_receivedByCollector() = runTest(testDispatcher) {
        var received: MapEvent? = null

        val collectJob: Job = launch(testDispatcher) {
            viewModel.mapEvents.collect { received = it }
        }

        viewModel.emitEvent(MapEvent.AutoFollowChanged(true))

        assertEquals(MapEvent.AutoFollowChanged(true), received)

        collectJob.cancel()
    }

    @Test
    fun emitEvent_bearingChanged_receivedByCollector() = runTest(testDispatcher) {
        var received: MapEvent? = null

        val collectJob: Job = launch(testDispatcher) {
            viewModel.mapEvents.collect { received = it }
        }

        viewModel.emitEvent(MapEvent.BearingChanged(45f))

        assertEquals(MapEvent.BearingChanged(45f), received)

        collectJob.cancel()
    }

    @Test
    fun emitEvent_multipleEvents_allReceived() = runTest(testDispatcher) {
        val events = mutableListOf<MapEvent>()

        val collectJob: Job = launch(testDispatcher) {
            viewModel.mapEvents.collect { events.add(it) }
        }

        viewModel.emitEvent(MapEvent.AutoFollowChanged(true))
        viewModel.emitEvent(MapEvent.BearingChanged(90f))
        viewModel.emitEvent(MapEvent.CenterOnLocationRequested(null))

        assertEquals(3, events.size)
        assertEquals(MapEvent.AutoFollowChanged(true), events[0])
        assertEquals(MapEvent.BearingChanged(90f), events[1])
        assertEquals(MapEvent.CenterOnLocationRequested(null), events[2])

        collectJob.cancel()
    }
}
