package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.model.TrackPoint

fun TrackEntity.toDomain(): Track = Track(
    id = id,
    name = name,
    description = description,
    date = date * 1000L,
    visible = show == 1,
    pointCount = pointCount,
    duration = duration,
    distance = distance,
    categoryId = categoryId,
    activityId = activity,
    style = style
)

fun TrackEntity.toDomainWithPoints(points: List<TrackPointEntity>): Track =
    toDomain().copy(points = points.map { it.toDomain() })

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    name = name,
    description = description,
    date = date / 1000L,
    show = if (visible) 1 else 0,
    pointCount = pointCount,
    duration = duration,
    distance = distance,
    categoryId = categoryId,
    activity = activityId,
    style = style
)

fun TrackPointEntity.toDomain(): TrackPoint = TrackPoint(
    id = id,
    trackId = trackId,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    speed = speed,
    date = date * 1000L
)

fun TrackPoint.toEntity(trackId: Int): TrackPointEntity = TrackPointEntity(
    id = id,
    trackId = trackId,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    speed = speed,
    date = date / 1000L
)

fun List<TrackEntity>.toDomain(): List<Track> = map { it.toDomain() }
