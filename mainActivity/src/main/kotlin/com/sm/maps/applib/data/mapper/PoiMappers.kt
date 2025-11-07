package com.sm.maps.applib.data.mapper

import com.sm.maps.applib.data.local.database.entity.PoiEntity
import com.sm.maps.applib.kml.PoiPoint
import org.andnav.osm.util.GeoPoint

fun PoiEntity.toDomain(): PoiPoint {
    val poi = PoiPoint()
    poi.Title = name
    poi.Descr = description
    poi.GeoPoint = if (latitude != 0.0 || longitude != 0.0) {
        GeoPoint((latitude * 1E6).toInt(), (longitude * 1E6).toInt())
    } else {
        null
    }
    poi.IconId = iconId ?: com.sm.maps.applib.R.drawable.poi
    poi.CategoryId = categoryId
    poi.Alt = altitude
    poi.PointSourceId = pointSourceId
    poi.Hidden = hidden == 1
    return poi
}

fun PoiPoint.toEntity(): PoiEntity {
    return PoiEntity(
        id = id,
        name = Title,
        description = Descr,
        latitude = GeoPoint?.latitude?.div(1E6) ?: 0.0,
        longitude = GeoPoint?.longitude?.div(1E6) ?: 0.0,
        altitude = Alt,
        hidden = if (Hidden) 1 else 0,
        categoryId = CategoryId,
        pointSourceId = PointSourceId,
        iconId = IconId
    )
}

fun List<PoiEntity>.toDomain(): List<PoiPoint> = map { it.toDomain() }
fun List<PoiPoint>.toEntity(): List<PoiEntity> = map { it.toEntity() }
