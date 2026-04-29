package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.ActivityEntity
import com.sm.maps.applib.domain.model.Activity

/**
 * Extension function to convert ActivityEntity to Activity domain model
 */
fun ActivityEntity.toDomain(): Activity {
    return Activity(
        id = id,
        name = name
    )
}

/**
 * Extension function to convert Activity domain model to ActivityEntity
 */
fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        name = name
    )
}

/**
 * Extension function to convert list of ActivityEntity to list of Activity
 */
fun List<ActivityEntity>.toDomain(): List<Activity> = map { it.toDomain() }

/**
 * Extension function to convert list of Activity to list of ActivityEntity
 */
fun List<Activity>.toEntity(): List<ActivityEntity> = map { it.toEntity() }
