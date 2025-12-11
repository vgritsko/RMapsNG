package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import com.sm.maps.applib.kml.Track
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class TrackMappersTest {

    @Test
    fun trackEntityToDomainConvertsCorrectly() {
        val trackEntity = TrackEntity(
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

        val track = trackEntity.toDomain()

        assertEquals("Morning Run", track.Name)
        assertEquals("Run in the park", track.Descr)
        assertTrue(track.Show)
        assertEquals(100, track.Cnt)
        assertEquals(5000.0, track.Distance, 0.001)
        assertEquals(1800.0, track.Duration, 0.001)
        assertEquals(2, track.Category)
        assertEquals(1, track.Activity)
        assertEquals("red", track.Style)
    }

    @Test
    fun trackToEntityConvertsCorrectly() {
        val track = Track().apply {
            Name = "Evening Walk"
            Descr = "Walk around the block"
            Date = Date(1609459200000L)
            Show = true
            Cnt = 50
            Distance = 2500.0
            Duration = 900.0
            Category = 3
            Activity = 2
            Style = "blue"
        }

        val trackEntity = track.toEntity()

        assertEquals("Evening Walk", trackEntity.name)
        assertEquals("Walk around the block", trackEntity.description)
        assertEquals(1, trackEntity.show)
        assertEquals(50, trackEntity.pointCount)
        assertEquals(2500, trackEntity.distance)
        assertEquals(900, trackEntity.duration)
        assertEquals(3, trackEntity.categoryId)
        assertEquals(2, trackEntity.activity)
        assertEquals("blue", trackEntity.style)
    }
}
