package com.sm.maps.applib.presentation.viewmodel

import com.sm.maps.applib.domain.model.PoiPoint
import com.sm.maps.applib.domain.repository.IPoiRepository
import com.sm.maps.applib.presentation.base.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakePoiRepository : IPoiRepository {

    private val _pois = MutableStateFlow<List<PoiPoint>>(emptyList())
    var shouldThrowError = false

    fun setPois(pois: List<PoiPoint>) {
        _pois.value = pois
    }

    fun currentPois(): List<PoiPoint> = _pois.value

    override fun getAllPois(): Flow<List<PoiPoint>> = if (shouldThrowError) {
        flow { throw RuntimeException("Repository error") }
    } else {
        _pois
    }

    override fun getPoisInBounds(
        minLon: Double, maxLon: Double, minLat: Double, maxLat: Double
    ): Flow<List<PoiPoint>> = _pois.map { pois ->
        pois.filter { it.longitude in minLon..maxLon && it.latitude in minLat..maxLat }
    }

    override suspend fun getPoiById(id: Int): PoiPoint? = _pois.value.find { it.id == id }

    override suspend fun insertPoi(poi: PoiPoint): Long {
        _pois.value = _pois.value + poi
        return poi.id.toLong()
    }

    override suspend fun updatePoi(poi: PoiPoint) {
        _pois.value = _pois.value.map { if (it.id == poi.id) poi else it }
    }

    override suspend fun deletePoi(poi: PoiPoint) {
        _pois.value = _pois.value.filter { it.id != poi.id }
    }

    override suspend fun deletePoiById(id: Int) {
        _pois.value = _pois.value.filter { it.id != id }
    }

    override suspend fun deleteAllPois() {
        _pois.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PoiListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakePoiRepository
    private lateinit var viewModel: PoiListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakePoiRepository()
        viewModel = PoiListViewModel(fakeRepo, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        assertEquals(UiState.Loading, viewModel.poiListState.value)
    }

    @Test
    fun emitsSuccessWhenPoisAvailable() = runTest(testDispatcher) {
        val pois = listOf(
            PoiPoint(id = 1, name = "Cafe", latitude = 55.0, longitude = 37.0),
            PoiPoint(id = 2, name = "Park", latitude = 55.1, longitude = 37.1)
        )

        val collectJob: Job = launch(testDispatcher) {
            viewModel.poiListState.collect {}
        }

        fakeRepo.setPois(pois)

        val state = viewModel.poiListState.value
        assertTrue("Expected Success but got $state", state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("Cafe", state.data[0].name)

        collectJob.cancel()
    }

    @Test
    fun emitsEmptyWhenNoPois() = runTest(testDispatcher) {
        val collectJob: Job = launch(testDispatcher) {
            viewModel.poiListState.collect {}
        }

        fakeRepo.setPois(emptyList())

        assertEquals(UiState.Empty, viewModel.poiListState.value)

        collectJob.cancel()
    }

    @Test
    fun emitsErrorWhenRepositoryThrows() = runTest(testDispatcher) {
        val errorRepo = FakePoiRepository().also { it.shouldThrowError = true }
        val errorViewModel = PoiListViewModel(errorRepo, testDispatcher)

        val collectJob: Job = launch(testDispatcher) {
            errorViewModel.poiListState.collect {}
        }

        val state = errorViewModel.poiListState.value
        assertTrue("Expected Error but got $state", state is UiState.Error)

        collectJob.cancel()
    }

    @Test
    fun deletePoiRemovesFromRepository() = runTest(testDispatcher) {
        val poi = PoiPoint(id = 1, name = "To Delete")
        fakeRepo.setPois(listOf(poi))

        val collectJob: Job = launch(testDispatcher) {
            viewModel.poiListState.collect {}
        }

        viewModel.deletePoi(poi)

        assertTrue(fakeRepo.currentPois().isEmpty())

        collectJob.cancel()
    }

    @Test
    fun toggleVisibilityUpdatesHiddenField() = runTest(testDispatcher) {
        val poi = PoiPoint(id = 1, name = "Visible POI", hidden = false)
        fakeRepo.setPois(listOf(poi))

        val collectJob: Job = launch(testDispatcher) {
            viewModel.poiListState.collect {}
        }

        viewModel.togglePoiVisibility(poi)

        val updated = fakeRepo.currentPois().find { it.id == 1 }
        assertNotNull(updated)
        assertTrue("Expected hidden=true after toggle", updated!!.hidden)

        collectJob.cancel()
    }
}
