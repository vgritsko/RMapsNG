package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.data.local.database.entity.MapEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Map data operations
 * Follows clean architecture - defines contract for data layer
 */
interface IMapRepository {

    /**
     * Get all maps as a Flow
     */
    fun getAllMaps(): Flow<List<MapEntity>>

    /**
     * Get maps by type
     */
    fun getMapsByType(type: Int): Flow<List<MapEntity>>

    /**
     * Get a specific map by ID
     */
    suspend fun getMapById(id: Int): MapEntity?

    /**
     * Get map by name
     */
    suspend fun getMapByName(name: String): MapEntity?

    /**
     * Insert a new map
     * @return the ID of the inserted map
     */
    suspend fun insertMap(map: MapEntity): Long

    /**
     * Insert multiple maps
     */
    suspend fun insertMaps(maps: List<MapEntity>)

    /**
     * Update an existing map
     */
    suspend fun updateMap(map: MapEntity)

    /**
     * Delete a map
     */
    suspend fun deleteMap(map: MapEntity)

    /**
     * Delete map by ID
     */
    suspend fun deleteMapById(id: Int)

    /**
     * Delete all maps
     */
    suspend fun deleteAllMaps()

    /**
     * Delete maps by type
     */
    suspend fun deleteMapsByType(type: Int)
}
