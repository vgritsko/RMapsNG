package com.sm.maps.applib.data.local.database.dao

import androidx.room.*
import com.sm.maps.applib.data.local.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Activity entities
 * Provides CRUD operations for activity data
 */
@Dao
interface ActivityDao {

    /**
     * Get all activities ordered by name
     */
    @Query("SELECT * FROM activity ORDER BY name")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    /**
     * Get a specific activity by ID
     */
    @Query("SELECT * FROM activity WHERE activityid = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?

    /**
     * Get activity by name
     */
    @Query("SELECT * FROM activity WHERE name = :name LIMIT 1")
    suspend fun getActivityByName(name: String): ActivityEntity?

    /**
     * Insert a new activity
     * @return the ID of the inserted activity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    /**
     * Insert multiple activities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>)

    /**
     * Update an existing activity
     */
    @Update
    suspend fun update(activity: ActivityEntity)

    /**
     * Delete an activity
     */
    @Delete
    suspend fun delete(activity: ActivityEntity)

    /**
     * Delete activity by ID
     */
    @Query("DELETE FROM activity WHERE activityid = :id")
    suspend fun deleteById(id: Int)

    /**
     * Delete all activities
     */
    @Query("DELETE FROM activity")
    suspend fun deleteAll()

    /**
     * Synchronous insert for data migration
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertActivitySync(activity: ActivityEntity): Long
}
