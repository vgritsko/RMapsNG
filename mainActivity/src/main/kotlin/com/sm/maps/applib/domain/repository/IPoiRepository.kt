package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.kml.PoiPoint
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for POI (Point of Interest) data operations
 * Follows clean architecture - defines contract for data layer
 */
interface IPoiRepository {
    /**
     * Get all POIs as a Flow
     */
    fun getAllPois(): Flow<List<PoiPoint>>

    /**
     * Get POIs within geographic bounds
     */
    fun getPoisInBounds(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): Flow<List<PoiPoint>>

    /**
     * Get a specific POI by ID
     */
    suspend fun getPoiById(id: Int): PoiPoint?

    /**
     * Insert a new POI
     * @return the ID of the inserted POI
     */
    suspend fun insertPoi(poi: PoiPoint): Long

    /**
     * Update an existing POI
     */
    suspend fun updatePoi(poi: PoiPoint)

    /**
     * Delete a POI
     */
    suspend fun deletePoi(poi: PoiPoint)

    /**
     * Delete a POI by ID
     */
    suspend fun deletePoiById(id: Int)

    /**
     * Delete all POIs
     */
    suspend fun deleteAllPois()
}
