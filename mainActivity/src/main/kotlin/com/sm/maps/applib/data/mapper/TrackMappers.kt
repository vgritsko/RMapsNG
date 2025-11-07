package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import com.sm.maps.applib.kml.Track
import java.util.Date

fun TrackEntity.toDomain(): Track {
    val track = Track()
    track.Name = name
    track.Descr = description
    track.Show = show == 1
    track.Cnt = pointCount
    track.Distance = distance.toDouble()
    track.Duration = duration.toDouble()
    track.Category = categoryId
    track.Activity = activity
    track.Date = Date(date * 1000)
    track.Style = style ?: ""
    return track
}

fun TrackEntity.toDomainWithPoints(points: List<TrackPointEntity>): Track {
    return toDomain().apply {
        points.forEach { pointEntity ->
            AddTrackPoint()
            LastTrackPoint.lat = pointEntity.latitude
            LastTrackPoint.lon = pointEntity.longitude
            LastTrackPoint.alt = pointEntity.altitude
            LastTrackPoint.speed = pointEntity.speed
            LastTrackPoint.date = Date(pointEntity.date * 1000)
        }
    }
}

fun Track.toEntity(): TrackEntity {
    return TrackEntity(
        id = 0,
        name = Name,
        description = Descr,
        date = Date.time / 1000,
        show = if (Show) 1 else 0,
        pointCount = Cnt,
        duration = Duration.toInt(),
        distance = Distance.toInt(),
        categoryId = Category,
        activity = Activity,
        style = Style
    )
}

fun Track.TrackPoint.toEntity(trackId: Int): TrackPointEntity {
    return TrackPointEntity(
        id = 0,
        trackId = trackId,
        latitude = lat,
        longitude = lon,
        altitude = alt,
        speed = speed,
        date = date.time / 1000
    )
}

fun TrackPointEntity.toDomain(track: Track): Track.TrackPoint {
    track.AddTrackPoint()
    track.LastTrackPoint.lat = latitude
    track.LastTrackPoint.lon = longitude
    track.LastTrackPoint.alt = altitude
    track.LastTrackPoint.speed = speed
    track.LastTrackPoint.date = Date(date * 1000)
    return track.LastTrackPoint
}

fun List<TrackEntity>.toDomain(): List<Track> = map { it.toDomain() }
