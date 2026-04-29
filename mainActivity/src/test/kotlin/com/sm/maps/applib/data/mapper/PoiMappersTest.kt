package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.PoiEntity
import com.sm.maps.applib.domain.model.PoiPoint
import org.junit.Assert.*
import org.junit.Test

class PoiMappersTest {

    @Test
    fun poiEntityToDomainConvertsCorrectly() {
        val entity = PoiEntity(
            id = 1,
            name = "Coffee Shop",
            description = "Best coffee in town",
            latitude = 45.5231,
            longitude = -122.6765,
            altitude = 100.5,
            hidden = 0,
            categoryId = 5,
            pointSourceId = 1,
            iconId = 123
        )

        val poi = entity.toDomain()

        assertEquals(1, poi.id)
        assertEquals("Coffee Shop", poi.name)
        assertEquals("Best coffee in town", poi.description)
        assertEquals(45.5231, poi.latitude, 0.0001)
        assertEquals(-122.6765, poi.longitude, 0.0001)
        assertEquals(100.5, poi.altitude, 0.001)
        assertFalse(poi.hidden)
        assertEquals(5, poi.categoryId)
        assertEquals(1, poi.pointSourceId)
        assertEquals(123, poi.iconId)
    }

    @Test
    fun poiEntityToDomainHiddenConvertsCorrectly() {
        val entity = PoiEntity(id = 2, hidden = 1)
        assertTrue(entity.toDomain().hidden)
    }

    @Test
    fun poiEntityToDomainNullIconId() {
        val entity = PoiEntity(id = 3, iconId = null)
        assertNull(entity.toDomain().iconId)
    }

    @Test
    fun poiPointToEntityConvertsCorrectly() {
        val poi = PoiPoint(
            id = 10,
            name = "Restaurant",
            description = "Italian cuisine",
            latitude = 45.5231,
            longitude = -122.6765,
            altitude = 50.0,
            hidden = true,
            categoryId = 3,
            pointSourceId = 2,
            iconId = 456
        )

        val entity = poi.toEntity()

        assertEquals(10, entity.id)
        assertEquals("Restaurant", entity.name)
        assertEquals("Italian cuisine", entity.description)
        assertEquals(45.5231, entity.latitude, 0.0001)
        assertEquals(-122.6765, entity.longitude, 0.0001)
        assertEquals(50.0, entity.altitude, 0.001)
        assertEquals(1, entity.hidden)
        assertEquals(3, entity.categoryId)
        assertEquals(2, entity.pointSourceId)
        assertEquals(456, entity.iconId)
    }

    @Test
    fun poiPointToEntityHiddenFalseConvertsToZero() {
        val poi = PoiPoint(id = 1, hidden = false)
        assertEquals(0, poi.toEntity().hidden)
    }

    @Test
    fun listOfPoiEntitiesToDomainConvertsAll() {
        val entities = listOf(
            PoiEntity(id = 1, name = "POI 1", latitude = 45.0, longitude = -93.0),
            PoiEntity(id = 2, name = "POI 2", latitude = 45.1, longitude = -93.1),
            PoiEntity(id = 3, name = "POI 3", latitude = 45.2, longitude = -93.2)
        )

        val pois = entities.toDomain()

        assertEquals(3, pois.size)
        assertEquals("POI 1", pois[0].name)
        assertEquals("POI 2", pois[1].name)
        assertEquals("POI 3", pois[2].name)
    }

    @Test
    fun listOfPoiPointsToEntityConvertsAll() {
        val pois = listOf(
            PoiPoint(id = 1, name = "A"),
            PoiPoint(id = 2, name = "B"),
            PoiPoint(id = 3, name = "C")
        )

        val entities = pois.toEntity()

        assertEquals(3, entities.size)
        assertEquals("A", entities[0].name)
        assertEquals("B", entities[1].name)
        assertEquals("C", entities[2].name)
    }
}
