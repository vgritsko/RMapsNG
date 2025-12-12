package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.MapDao
import com.sm.maps.applib.data.local.database.entity.MapEntity
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.repository.IMapRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Map repository
 * Handles data operations for maps using Room database
 */
@Singleton
class MapRepository @Inject constructor(
    private val mapDao: MapDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IMapRepository {

    override fun getAllMaps(): Flow<List<MapEntity>> =
        mapDao.getAllMaps()

    override fun getMapsByType(type: Int): Flow<List<MapEntity>> =
        mapDao.getMapsByType(type)

    override suspend fun getMapById(id: Int): MapEntity? =
        withContext(ioDispatcher) {
            mapDao.getMapById(id)
        }

    override suspend fun getMapByName(name: String): MapEntity? =
        withContext(ioDispatcher) {
            mapDao.getMapByName(name)
        }

    override suspend fun insertMap(map: MapEntity): Long =
        withContext(ioDispatcher) {
            mapDao.insert(map)
        }

    override suspend fun insertMaps(maps: List<MapEntity>) =
        withContext(ioDispatcher) {
            mapDao.insertAll(maps)
        }

    override suspend fun updateMap(map: MapEntity) =
        withContext(ioDispatcher) {
            mapDao.update(map)
        }

    override suspend fun deleteMap(map: MapEntity) =
        withContext(ioDispatcher) {
            mapDao.delete(map)
        }

    override suspend fun deleteMapById(id: Int) =
        withContext(ioDispatcher) {
            mapDao.deleteById(id)
        }

    override suspend fun deleteAllMaps() =
        withContext(ioDispatcher) {
            mapDao.deleteAll()
        }

    override suspend fun deleteMapsByType(type: Int) =
        withContext(ioDispatcher) {
            mapDao.deleteByType(type)
        }
}
