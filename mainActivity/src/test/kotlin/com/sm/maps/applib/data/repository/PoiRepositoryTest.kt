package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.local.database.dao.PoiDao
import com.sm.maps.applib.data.local.database.entity.PoiEntity
import com.sm.maps.applib.domain.model.PoiPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

private fun <T> anyNonNull(type: Class<T>): T = org.mockito.Mockito.any(type) ?: type.getDeclaredConstructor().newInstance()

@OptIn(ExperimentalCoroutinesApi::class)
class PoiRepositoryTest {

    private lateinit var poiDao: PoiDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: PoiRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        poiDao = mock(PoiDao::class.java)
        categoryDao = mock(CategoryDao::class.java)
        repository = PoiRepository(poiDao, categoryDao, testDispatcher)
    }

    @Test
    fun `getAllPois returns mapped domain models`() = runTest {
        val entities = listOf(
            PoiEntity(id = 1, name = "Cafe", latitude = 55.0, longitude = 37.0, hidden = 0),
            PoiEntity(id = 2, name = "Park", latitude = 55.1, longitude = 37.1, hidden = 1)
        )
        `when`(poiDao.getAllPois()).thenReturn(flowOf(entities))

        val pois = repository.getAllPois().first()

        assertEquals(2, pois.size)
        assertEquals(1, pois[0].id)
        assertEquals("Cafe", pois[0].name)
        assertEquals(55.0, pois[0].latitude, 0.0001)
        assertFalse(pois[0].hidden)
        assertTrue(pois[1].hidden)
    }

    @Test
    fun `getPoiById returns null when not found`() = runTest {
        `when`(poiDao.getPoiById(99)).thenReturn(null)

        val result = repository.getPoiById(99)

        assertNull(result)
    }

    @Test
    fun `getPoiById returns poi when found`() = runTest {
        val entity = PoiEntity(id = 5, name = "Museum", latitude = 48.8, longitude = 2.35, hidden = 0, iconId = 7)
        `when`(poiDao.getPoiById(5)).thenReturn(entity)

        val poi = repository.getPoiById(5)

        assertNotNull(poi)
        assertEquals(5, poi!!.id)
        assertEquals("Museum", poi.name)
        assertEquals(48.8, poi.latitude, 0.0001)
        assertFalse(poi.hidden)
        assertEquals(7, poi.iconId)
    }

    @Test
    fun `insertPoi delegates to dao`() = runTest {
        val poi = PoiPoint(id = 0, name = "New POI", latitude = 55.0, longitude = 37.0)
        `when`(poiDao.insert(anyNonNull(PoiEntity::class.java))).thenReturn(10L)

        val id = repository.insertPoi(poi)

        assertEquals(10L, id)
        verify(poiDao).insert(anyNonNull(PoiEntity::class.java))
    }

    @Test
    fun `updatePoi delegates to dao`() = runTest {
        val poi = PoiPoint(id = 3, name = "Updated POI")
        repository.updatePoi(poi)
        verify(poiDao).update(anyNonNull(PoiEntity::class.java))
    }

    @Test
    fun `deletePoi delegates to dao`() = runTest {
        val poi = PoiPoint(id = 4, name = "To Delete")
        repository.deletePoi(poi)
        verify(poiDao).delete(anyNonNull(PoiEntity::class.java))
    }

    @Test
    fun `deletePoiById delegates to dao`() = runTest {
        repository.deletePoiById(5)
        verify(poiDao).deleteById(5)
    }

    @Test
    fun `deleteAllPois delegates to dao`() = runTest {
        repository.deleteAllPois()
        verify(poiDao).deleteAll()
    }
}
