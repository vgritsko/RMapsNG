package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.MapEntity
import com.sm.maps.applib.domain.model.MapData

/**
 * Extension function to convert MapEntity to MapData domain model
 */
fun MapEntity.toDomain(): MapData {
    return MapData(
        id = id,
        name = name,
        type = type,
        params = params
    )
}

/**
 * Extension function to convert MapData domain model to MapEntity
 */
fun MapData.toEntity(): MapEntity {
    return MapEntity(
        id = id,
        name = name,
        type = type,
        params = params
    )
}

/**
 * Extension function to convert list of MapEntity to list of MapData
 */
fun List<MapEntity>.toDomain(): List<MapData> = map { it.toDomain() }

/**
 * Extension function to convert list of MapData to list of MapEntity
 */
fun List<MapData>.toEntity(): List<MapEntity> = map { it.toEntity() }
