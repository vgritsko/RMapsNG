package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.PoiDao
import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.mapper.toDomain
import com.sm.maps.applib.data.mapper.toEntity
import com.sm.maps.applib.kml.PoiPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PoiRepository(
    private val poiDao: PoiDao,
    private val categoryDao: CategoryDao
) {
    fun getAllPois(): Flow<List<PoiPoint>> =
        poiDao.getAllPois().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getPoisInBounds(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): Flow<List<PoiPoint>> =
        poiDao.getPoisInBounds(minLon, maxLon, minLat, maxLat)
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun getPoiById(id: Int): PoiPoint? = withContext(Dispatchers.IO) {
        poiDao.getPoiById(id)?.toDomain()
    }

    suspend fun insertPoi(poi: PoiPoint): Long = withContext(Dispatchers.IO) {
        poiDao.insert(poi.toEntity())
    }

    suspend fun updatePoi(poi: PoiPoint) = withContext(Dispatchers.IO) {
        poiDao.update(poi.toEntity())
    }

    suspend fun deletePoi(poi: PoiPoint) = withContext(Dispatchers.IO) {
        poiDao.delete(poi.toEntity())
    }

    suspend fun deletePoiById(id: Int) = withContext(Dispatchers.IO) {
        poiDao.deleteById(id)
    }

    suspend fun deleteAllPois() = withContext(Dispatchers.IO) {
        poiDao.deleteAll()
    }

    suspend fun insertAll(pois: List<PoiPoint>) = withContext(Dispatchers.IO) {
        poiDao.insertAll(pois.map { it.toEntity() })
    }
}
