package com.sm.maps.applib.data.local.database.dao

import androidx.room.*
import com.sm.maps.applib.data.local.database.entity.PoiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {
    @Query("SELECT * FROM points ORDER BY lat, lon")
    fun getAllPois(): Flow<List<PoiEntity>>

    @Query("""
        SELECT * FROM points
        WHERE hidden = 0
        AND lon BETWEEN :minLon AND :maxLon
        AND lat BETWEEN :minLat AND :maxLat
        ORDER BY lat, lon
    """)
    fun getPoisInBounds(
        minLon: Double,
        maxLon: Double,
        minLat: Double,
        maxLat: Double
    ): Flow<List<PoiEntity>>

    @Query("SELECT * FROM points WHERE pointid = :id")
    suspend fun getPoiById(id: Int): PoiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(poi: PoiEntity): Long

    @Update
    suspend fun update(poi: PoiEntity)

    @Delete
    suspend fun delete(poi: PoiEntity)

    @Query("DELETE FROM points WHERE pointid = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM points")
    suspend fun deleteAll()

    @Transaction
    suspend fun insertAll(pois: List<PoiEntity>) {
        pois.forEach { insert(it) }
    }
}
