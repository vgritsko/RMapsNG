package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.TrackDao
import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.model.TrackPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

// Helper to avoid null-returns from Mockito matchers for non-null Kotlin types
private fun <T> anyNonNull(type: Class<T>): T = org.mockito.Mockito.any(type) ?: type.getDeclaredConstructor().newInstance()

@OptIn(ExperimentalCoroutinesApi::class)
class TrackRepositoryTest {

    private lateinit var trackDao: TrackDao
    private lateinit var repository: TrackRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        trackDao = mock(TrackDao::class.java)
        repository = TrackRepository(trackDao, testDispatcher)
    }

    @Test
    fun `getAllTracks returns mapped domain models`() = runTest {
        val entities = listOf(
            TrackEntity(id = 1, name = "Track 1", show = 1, activity = 2, date = 1000L),
            TrackEntity(id = 2, name = "Track 2", show = 0, activity = 1, date = 2000L)
        )
        `when`(trackDao.getAllTracks()).thenReturn(flowOf(entities))

        val tracks = repository.getAllTracks().first()

        assertEquals(2, tracks.size)
        assertEquals(1, tracks[0].id)
        assertEquals("Track 1", tracks[0].name)
        assertTrue(tracks[0].visible)
        assertEquals(2, tracks[0].activityId)
        assertEquals(1000L * 1000L, tracks[0].date)
        assertFalse(tracks[1].visible)
    }

    @Test
    fun `getTrackById returns null when not found`() = runTest {
        `when`(trackDao.getTrackById(99)).thenReturn(null)

        val result = repository.getTrackById(99)

        assertNull(result)
    }

    @Test
    fun `getTrackById returns track with points when found`() = runTest {
        val entity = TrackEntity(id = 1, name = "My Track", date = 500L)
        val pointEntities = listOf(
            TrackPointEntity(id = 10, trackId = 1, latitude = 55.0, longitude = 37.0, date = 100L),
            TrackPointEntity(id = 11, trackId = 1, latitude = 55.1, longitude = 37.1, date = 200L)
        )
        `when`(trackDao.getTrackById(1)).thenReturn(entity)
        `when`(trackDao.getTrackPoints(1)).thenReturn(pointEntities)

        val track = repository.getTrackById(1)

        assertNotNull(track)
        assertEquals("My Track", track!!.name)
        assertEquals(2, track.points.size)
        assertEquals(55.0, track.points[0].latitude, 0.0001)
        assertEquals(100L * 1000L, track.points[0].date)
    }

    @Test
    fun `insertTrackWithPoints inserts track and all points`() = runTest {
        val track = Track(
            name = "New Track",
            points = listOf(
                TrackPoint(latitude = 55.0, longitude = 37.0),
                TrackPoint(latitude = 55.1, longitude = 37.1)
            )
        )
        `when`(trackDao.insertTrack(TrackEntity(name = "New Track"))).thenReturn(42L)
        `when`(trackDao.insertTrack(anyNonNull(TrackEntity::class.java))).thenReturn(42L)

        val id = repository.insertTrackWithPoints(track)

        assertEquals(42L, id)
        verify(trackDao).insertTrack(anyNonNull(TrackEntity::class.java))
        verify(trackDao, times(2)).insertTrackPoint(anyNonNull(TrackPointEntity::class.java))
    }

    @Test
    fun `toggleTrackVisibility delegates to dao`() = runTest {
        repository.toggleTrackVisibility(7)
        verify(trackDao).toggleTrackVisibility(7)
    }

    @Test
    fun `updateTrack delegates to dao`() = runTest {
        val track = Track(id = 3, name = "Updated")
        repository.updateTrack(track)
        verify(trackDao).updateTrack(anyNonNull(TrackEntity::class.java))
    }

    @Test
    fun `deleteTrack delegates to dao`() = runTest {
        val track = Track(id = 4, name = "To Delete")
        repository.deleteTrack(track)
        verify(trackDao).deleteTrack(anyNonNull(TrackEntity::class.java))
    }

    @Test
    fun `addPointToTrack delegates to dao`() = runTest {
        val point = TrackPoint(latitude = 55.0, longitude = 37.0)
        repository.addPointToTrack(trackId = 1, point = point)
        verify(trackDao).insertTrackPoint(anyNonNull(TrackPointEntity::class.java))
    }
}
