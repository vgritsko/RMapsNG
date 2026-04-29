package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.model.TrackPoint
import com.sm.maps.applib.domain.repository.ITrackRepository
import com.sm.maps.applib.presentation.base.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeTrackRepository : ITrackRepository {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    var shouldThrowError = false

    fun setTracks(tracks: List<Track>) {
        _tracks.value = tracks
    }

    fun currentTracks(): List<Track> = _tracks.value

    override fun getAllTracks(): Flow<List<Track>> = if (shouldThrowError) {
        flow { throw RuntimeException("Repository error") }
    } else {
        _tracks
    }

    override fun getVisibleTracksWithPoints(): Flow<List<Track>> = _tracks

    override suspend fun getTrackById(id: Int): Track? = _tracks.value.find { it.id == id }

    override suspend fun insertTrackWithPoints(track: Track): Long {
        _tracks.value = _tracks.value + track
        return track.id.toLong()
    }

    override suspend fun updateTrack(track: Track) {
        _tracks.value = _tracks.value.map { if (it.id == track.id) track else it }
    }

    override suspend fun deleteTrack(track: Track) {
        _tracks.value = _tracks.value.filter { it.id != track.id }
    }

    override suspend fun toggleTrackVisibility(id: Int) {
        _tracks.value = _tracks.value.map { if (it.id == id) it.copy(visible = !it.visible) else it }
    }

    override suspend fun addPointToTrack(trackId: Int, point: TrackPoint) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakeTrackRepository
    private lateinit var viewModel: TrackListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeTrackRepository()
        viewModel = TrackListViewModel(fakeRepo, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        assertEquals(UiState.Loading, viewModel.trackListState.value)
    }

    @Test
    fun emitsSuccessWhenTracksAvailable() = runTest(testDispatcher) {
        val tracks = listOf(
            Track(id = 1, name = "Morning Run"),
            Track(id = 2, name = "Evening Walk")
        )

        val collectJob: Job = launch(testDispatcher) {
            viewModel.trackListState.collect {}
        }

        fakeRepo.setTracks(tracks)

        val state = viewModel.trackListState.value
        assertTrue("Expected Success but got $state", state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("Morning Run", state.data[0].name)

        collectJob.cancel()
    }

    @Test
    fun emitsEmptyWhenNoTracks() = runTest(testDispatcher) {
        val collectJob: Job = launch(testDispatcher) {
            viewModel.trackListState.collect {}
        }

        fakeRepo.setTracks(emptyList())

        assertEquals(UiState.Empty, viewModel.trackListState.value)

        collectJob.cancel()
    }

    @Test
    fun emitsErrorWhenRepositoryThrows() = runTest(testDispatcher) {
        val errorRepo = FakeTrackRepository().also { it.shouldThrowError = true }
        val errorViewModel = TrackListViewModel(errorRepo, testDispatcher)

        val collectJob: Job = launch(testDispatcher) {
            errorViewModel.trackListState.collect {}
        }

        val state = errorViewModel.trackListState.value
        assertTrue("Expected Error but got $state", state is UiState.Error)

        collectJob.cancel()
    }

    @Test
    fun deleteTrackRemovesFromRepository() = runTest(testDispatcher) {
        val track = Track(id = 1, name = "To Delete")
        fakeRepo.setTracks(listOf(track))

        val collectJob: Job = launch(testDispatcher) {
            viewModel.trackListState.collect {}
        }

        viewModel.deleteTrack(track)

        assertTrue(fakeRepo.currentTracks().isEmpty())

        collectJob.cancel()
    }

    @Test
    fun toggleVisibilityInvertsVisibleField() = runTest(testDispatcher) {
        val track = Track(id = 1, name = "Hidden Track", visible = false)
        fakeRepo.setTracks(listOf(track))

        val collectJob: Job = launch(testDispatcher) {
            viewModel.trackListState.collect {}
        }

        viewModel.toggleTrackVisibility(track.id)

        val updated = fakeRepo.currentTracks().find { it.id == 1 }
        assertNotNull(updated)
        assertTrue("Expected visible=true after toggle", updated!!.visible)

        collectJob.cancel()
    }
}
