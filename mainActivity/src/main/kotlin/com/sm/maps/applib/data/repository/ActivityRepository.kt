package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.ActivityDao
import com.sm.maps.applib.data.local.database.entity.ActivityEntity
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.repository.IActivityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Activity repository
 * Handles data operations for activities using Room database
 */
@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : IActivityRepository {

    override fun getAllActivities(): Flow<List<ActivityEntity>> =
        activityDao.getAllActivities()

    override suspend fun getActivityById(id: Int): ActivityEntity? =
        withContext(ioDispatcher) {
            activityDao.getActivityById(id)
        }

    override suspend fun getActivityByName(name: String): ActivityEntity? =
        withContext(ioDispatcher) {
            activityDao.getActivityByName(name)
        }

    override suspend fun insertActivity(activity: ActivityEntity): Long =
        withContext(ioDispatcher) {
            activityDao.insert(activity)
        }

    override suspend fun insertActivities(activities: List<ActivityEntity>) =
        withContext(ioDispatcher) {
            activityDao.insertAll(activities)
        }

    override suspend fun updateActivity(activity: ActivityEntity) =
        withContext(ioDispatcher) {
            activityDao.update(activity)
        }

    override suspend fun deleteActivity(activity: ActivityEntity) =
        withContext(ioDispatcher) {
            activityDao.delete(activity)
        }

    override suspend fun deleteActivityById(id: Int) =
        withContext(ioDispatcher) {
            activityDao.deleteById(id)
        }

    override suspend fun deleteAllActivities() =
        withContext(ioDispatcher) {
            activityDao.deleteAll()
        }
}
