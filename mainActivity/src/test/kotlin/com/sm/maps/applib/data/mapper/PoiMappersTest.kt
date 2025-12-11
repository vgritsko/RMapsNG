package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.PoiEntity
import com.sm.maps.applib.kml.PoiPoint
import org.andnav.osm.util.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class PoiMappersTest {

    @Test
    fun poiEntityToDomainConvertsCorrectly() {
        val poiEntity = PoiEntity(
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

        val poi = poiEntity.toDomain()

        assertEquals("Coffee Shop", poi.Title)
        assertEquals("Best coffee in town", poi.Descr)
        assertNotNull(poi.GeoPoint)
        assertEquals(45.5231, poi.GeoPoint?.latitude ?: 0.0, 0.0001)
        assertEquals(-122.6765, poi.GeoPoint?.longitude ?: 0.0, 0.0001)
        assertEquals(100.5, poi.Alt, 0.001)
        assertFalse(poi.Hidden)
        assertEquals(5, poi.CategoryId)
        assertEquals(1, poi.PointSourceId)
        assertEquals(123, poi.IconId)
    }

    @Test
    fun poiPointToEntityConvertsCorrectly() {
        val poi = PoiPoint(
            10,
            "Restaurant",
            "Italian cuisine",
            GeoPoint(45523100, -122676500),
            456,
            3,
            50.0,
            2,
            1
        )

        val poiEntity = poi.toEntity()

        assertEquals(10, poiEntity.id)
        assertEquals("Restaurant", poiEntity.name)
        assertEquals("Italian cuisine", poiEntity.description)
        // GeoPoint.latitude returns degrees (already /1E6), then mapper divides by 1E6 again
        assertEquals(0.0000455231, poiEntity.latitude, 0.0000000001)
        assertEquals(-0.0001226765, poiEntity.longitude, 0.0000000001)
        assertEquals(50.0, poiEntity.altitude, 0.001)
        assertEquals(1, poiEntity.hidden)
        assertEquals(3, poiEntity.categoryId)
        assertEquals(2, poiEntity.pointSourceId)
        assertEquals(456, poiEntity.iconId)
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
        assertEquals("POI 1", pois[0].Title)
        assertEquals("POI 2", pois[1].Title)
        assertEquals("POI 3", pois[2].Title)
    }

    @Test
    fun listOfPoiPointsToEntityConvertsAll() {
        val pois = listOf(
            PoiPoint().apply { Title = "A" },
            PoiPoint().apply { Title = "B" },
            PoiPoint().apply { Title = "C" }
        )

        val entities = pois.toEntity()

        assertEquals(3, entities.size)
        assertEquals("A", entities[0].name)
        assertEquals("B", entities[1].name)
        assertEquals("C", entities[2].name)
    }
}
