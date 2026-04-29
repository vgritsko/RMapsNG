package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.PoiEntity
import com.sm.maps.applib.domain.model.PoiPoint

fun PoiEntity.toDomain(): PoiPoint = PoiPoint(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    hidden = hidden == 1,
    categoryId = categoryId,
    pointSourceId = pointSourceId,
    iconId = iconId
)

fun PoiPoint.toEntity(): PoiEntity = PoiEntity(
    id = id,
    name = name,
    description = description,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    hidden = if (hidden) 1 else 0,
    categoryId = categoryId,
    pointSourceId = pointSourceId,
    iconId = iconId
)

fun List<PoiEntity>.toDomain(): List<PoiPoint> = map { it.toDomain() }
fun List<PoiPoint>.toEntity(): List<PoiEntity> = map { it.toEntity() }
