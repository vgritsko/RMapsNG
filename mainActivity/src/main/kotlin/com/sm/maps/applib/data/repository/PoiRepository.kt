package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.PoiDao
import com.sm.maps.applib.data.local.database.dao.CategoryDao
import com.sm.maps.applib.data.mapper.toDomain
import com.sm.maps.applib.data.mapper.toEntity
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.repository.IPoiRepository
import com.sm.maps.applib.kml.PoiPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of POI repository
 * Handles data operations for Points of Interest using Room database
 */
@Singleton
class PoiRepository @Inject constructor(
    private val poiDao: PoiDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IPoiRepository {
    override fun getAllPois(): Flow<List<PoiPoint>> =
        poiDao.getAllPois().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getPoisInBounds(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): Flow<List<PoiPoint>> =
        poiDao.getPoisInBounds(minLon, maxLon, minLat, maxLat)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getPoiById(id: Int): PoiPoint? = withContext(ioDispatcher) {
        poiDao.getPoiById(id)?.toDomain()
    }

    override suspend fun insertPoi(poi: PoiPoint): Long = withContext(ioDispatcher) {
        poiDao.insert(poi.toEntity())
    }

    override suspend fun updatePoi(poi: PoiPoint) = withContext(ioDispatcher) {
        poiDao.update(poi.toEntity())
    }

    override suspend fun deletePoi(poi: PoiPoint) = withContext(ioDispatcher) {
        poiDao.delete(poi.toEntity())
    }

    override suspend fun deletePoiById(id: Int) = withContext(ioDispatcher) {
        poiDao.deleteById(id)
    }

    override suspend fun deleteAllPois() = withContext(ioDispatcher) {
        poiDao.deleteAll()
    }

    suspend fun insertAll(pois: List<PoiPoint>) = withContext(ioDispatcher) {
        poiDao.insertAll(pois.map { it.toEntity() })
    }
}
