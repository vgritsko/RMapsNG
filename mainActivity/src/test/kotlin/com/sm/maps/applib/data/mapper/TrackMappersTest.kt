package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.model.TrackPoint
import org.junit.Assert.*
import org.junit.Test

class TrackMappersTest {

    @Test
    fun trackEntityToDomainConvertsCorrectly() {
        val entity = TrackEntity(
            id = 1,
            name = "Morning Run",
            description = "Run in the park",
            date = 1609459200L,
            show = 1,
            pointCount = 100,
            distance = 5000,
            duration = 1800,
            categoryId = 2,
            activity = 1,
            style = "red"
        )

        val track = entity.toDomain()

        assertEquals(1, track.id)
        assertEquals("Morning Run", track.name)
        assertEquals("Run in the park", track.description)
        assertEquals(1609459200L * 1000L, track.date)
        assertTrue(track.visible)
        assertEquals(100, track.pointCount)
        assertEquals(5000, track.distance)
        assertEquals(1800, track.duration)
        assertEquals(2, track.categoryId)
        assertEquals(1, track.activityId)
        assertEquals("red", track.style)
        assertTrue(track.points.isEmpty())
    }

    @Test
    fun trackEntityToDomainHiddenShowFalse() {
        val entity = TrackEntity(id = 2, show = 0)
        assertFalse(entity.toDomain().visible)
    }

    @Test
    fun trackToEntityConvertsCorrectly() {
        val dateMillis = 1609459200000L
        val track = Track(
            id = 5,
            name = "Evening Walk",
            description = "Walk around the block",
            date = dateMillis,
            visible = true,
            pointCount = 50,
            distance = 2500,
            duration = 900,
            categoryId = 3,
            activityId = 2,
            style = "blue"
        )

        val entity = track.toEntity()

        assertEquals(5, entity.id)
        assertEquals("Evening Walk", entity.name)
        assertEquals("Walk around the block", entity.description)
        assertEquals(dateMillis / 1000L, entity.date)
        assertEquals(1, entity.show)
        assertEquals(50, entity.pointCount)
        assertEquals(2500, entity.distance)
        assertEquals(900, entity.duration)
        assertEquals(3, entity.categoryId)
        assertEquals(2, entity.activity)
        assertEquals("blue", entity.style)
    }

    @Test
    fun trackEntityToDomainWithPointsConvertsCorrectly() {
        val entity = TrackEntity(id = 1, name = "Track")
        val pointEntities = listOf(
            TrackPointEntity(id = 10, trackId = 1, latitude = 55.0, longitude = 37.0, altitude = 100.0, speed = 5.0, date = 1000L),
            TrackPointEntity(id = 11, trackId = 1, latitude = 55.1, longitude = 37.1, altitude = 101.0, speed = 6.0, date = 2000L)
        )

        val track = entity.toDomainWithPoints(pointEntities)

        assertEquals(2, track.points.size)
        assertEquals(55.0, track.points[0].latitude, 0.0001)
        assertEquals(37.0, track.points[0].longitude, 0.0001)
        assertEquals(1000L * 1000L, track.points[0].date)
        assertEquals(55.1, track.points[1].latitude, 0.0001)
    }

    @Test
    fun trackPointEntityToDomainConvertsCorrectly() {
        val entity = TrackPointEntity(
            id = 10, trackId = 1, latitude = 55.75, longitude = 37.61,
            altitude = 150.0, speed = 10.0, date = 1609459200L
        )

        val point = entity.toDomain()

        assertEquals(10, point.id)
        assertEquals(1, point.trackId)
        assertEquals(55.75, point.latitude, 0.0001)
        assertEquals(37.61, point.longitude, 0.0001)
        assertEquals(150.0, point.altitude, 0.001)
        assertEquals(10.0, point.speed, 0.001)
        assertEquals(1609459200L * 1000L, point.date)
    }

    @Test
    fun trackPointToEntityConvertsCorrectly() {
        val point = TrackPoint(
            id = 5, trackId = 0, latitude = 55.75, longitude = 37.61,
            altitude = 150.0, speed = 10.0, date = 1609459200000L
        )

        val entity = point.toEntity(trackId = 42)

        assertEquals(5, entity.id)
        assertEquals(42, entity.trackId)
        assertEquals(55.75, entity.latitude, 0.0001)
        assertEquals(37.61, entity.longitude, 0.0001)
        assertEquals(150.0, entity.altitude, 0.001)
        assertEquals(10.0, entity.speed, 0.001)
        assertEquals(1609459200L, entity.date)
    }

    @Test
    fun listToDomainConvertsAll() {
        val entities = listOf(
            TrackEntity(id = 1, name = "Track 1"),
            TrackEntity(id = 2, name = "Track 2"),
            TrackEntity(id = 3, name = "Track 3")
        )

        val tracks = entities.toDomain()

        assertEquals(3, tracks.size)
        assertEquals("Track 1", tracks[0].name)
        assertEquals("Track 2", tracks[1].name)
        assertEquals("Track 3", tracks[2].name)
    }
}
