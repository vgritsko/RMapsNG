package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.data.local.database.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Activity data operations
 * Follows clean architecture - defines contract for data layer
 */
interface IActivityRepository {

    /**
     * Get all activities as a Flow
     */
    fun getAllActivities(): Flow<List<ActivityEntity>>

    /**
     * Get a specific activity by ID
     */
    suspend fun getActivityById(id: Int): ActivityEntity?

    /**
     * Get activity by name
     */
    suspend fun getActivityByName(name: String): ActivityEntity?

    /**
     * Insert a new activity
     * @return the ID of the inserted activity
     */
    suspend fun insertActivity(activity: ActivityEntity): Long

    /**
     * Insert multiple activities
     */
    suspend fun insertActivities(activities: List<ActivityEntity>)

    /**
     * Update an existing activity
     */
    suspend fun updateActivity(activity: ActivityEntity)

    /**
     * Delete an activity
     */
    suspend fun deleteActivity(activity: ActivityEntity)

    /**
     * Delete activity by ID
     */
    suspend fun deleteActivityById(id: Int)

    /**
     * Delete all activities
     */
    suspend fun deleteAllActivities()
}
