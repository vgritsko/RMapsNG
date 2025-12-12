package com.sm.maps.applib.data.local.database.dao

import androidx.room.*
import com.sm.maps.applib.data.local.database.entity.MapEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Map entities
 * Provides CRUD operations for map data
 */
@Dao
interface MapDao {

    /**
     * Get all maps ordered by name
     */
    @Query("SELECT * FROM maps ORDER BY name")
    fun getAllMaps(): Flow<List<MapEntity>>

    /**
     * Get maps by type
     */
    @Query("SELECT * FROM maps WHERE type = :type ORDER BY name")
    fun getMapsByType(type: Int): Flow<List<MapEntity>>

    /**
     * Get a specific map by ID
     */
    @Query("SELECT * FROM maps WHERE mapid = :id")
    suspend fun getMapById(id: Int): MapEntity?

    /**
     * Get map by name
     */
    @Query("SELECT * FROM maps WHERE name = :name LIMIT 1")
    suspend fun getMapByName(name: String): MapEntity?

    /**
     * Insert a new map
     * @return the ID of the inserted map
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(map: MapEntity): Long

    /**
     * Insert multiple maps
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(maps: List<MapEntity>)

    /**
     * Update an existing map
     */
    @Update
    suspend fun update(map: MapEntity)

    /**
     * Delete a map
     */
    @Delete
    suspend fun delete(map: MapEntity)

    /**
     * Delete map by ID
     */
    @Query("DELETE FROM maps WHERE mapid = :id")
    suspend fun deleteById(id: Int)

    /**
     * Delete all maps
     */
    @Query("DELETE FROM maps")
    suspend fun deleteAll()

    /**
     * Delete maps by type
     */
    @Query("DELETE FROM maps WHERE type = :type")
    suspend fun deleteByType(type: Int)

    /**
     * Synchronous insert for data migration
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMapSync(map: MapEntity): Long
}
