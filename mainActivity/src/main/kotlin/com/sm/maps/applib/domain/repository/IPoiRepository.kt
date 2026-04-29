package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.domain.model.PoiPoint
import kotlinx.coroutines.flow.Flow

interface IPoiRepository {
    fun getAllPois(): Flow<List<PoiPoint>>
    fun getPoisInBounds(minLon: Double, maxLon: Double, minLat: Double, maxLat: Double): Flow<List<PoiPoint>>
    suspend fun getPoiById(id: Int): PoiPoint?
    suspend fun insertPoi(poi: PoiPoint): Long
    suspend fun updatePoi(poi: PoiPoint)
    suspend fun deletePoi(poi: PoiPoint)
    suspend fun deletePoiById(id: Int)
    suspend fun deleteAllPois()
}
