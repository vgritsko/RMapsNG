package com.sm.maps.applib.data.migration

import android.database.Cursor
import com.sm.maps.applib.data.local.database.entity.*

/**
 * Extension functions to convert legacy SQLite Cursor results to Room entities
 */

/**
 * Convert cursor from legacy 'points' table to PoiEntity
 * Legacy schema: pointid, name, descr, lat, lon, alt, hidden, categoryid, pointsourceid, iconid
 */
fun Cursor.toPoiEntity(): PoiEntity {
    return PoiEntity(
        id = getInt(getColumnIndexOrThrow("pointid")),
        name = getString(getColumnIndexOrThrow("name")) ?: "",
        description = getString(getColumnIndexOrThrow("descr")) ?: "",
        latitude = getDouble(getColumnIndexOrThrow("lat")),
        longitude = getDouble(getColumnIndexOrThrow("lon")),
        altitude = getDouble(getColumnIndexOrThrow("alt")),
        hidden = getInt(getColumnIndexOrThrow("hidden")),
        categoryId = getInt(getColumnIndexOrThrow("categoryid")),
        pointSourceId = getInt(getColumnIndexOrThrow("pointsourceid")),
        iconId = if (isNull(getColumnIndexOrThrow("iconid"))) null
                 else getInt(getColumnIndexOrThrow("iconid"))
    )
}

/**
 * Convert cursor from legacy 'category' table to CategoryEntity
 * Legacy schema: categoryid, name, hidden, iconid, minzoom
 */
fun Cursor.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = getInt(getColumnIndexOrThrow("categoryid")),
        name = getString(getColumnIndexOrThrow("name")) ?: "",
        hidden = getInt(getColumnIndexOrThrow("hidden")),
        iconId = if (isNull(getColumnIndexOrThrow("iconid"))) null
                 else getInt(getColumnIndexOrThrow("iconid")),
        minZoom = getInt(getColumnIndexOrThrow("minzoom"))
    )
}

/**
 * Convert cursor from legacy 'tracks' table to TrackEntity
 * Legacy schema: trackid, name, descr, date, show, cnt, duration, distance, categoryid, activity, style
 */
fun Cursor.toTrackEntity(): TrackEntity {
    return TrackEntity(
        id = getInt(getColumnIndexOrThrow("trackid")),
        name = getString(getColumnIndexOrThrow("name")) ?: "",
        description = getString(getColumnIndexOrThrow("descr")) ?: "",
        date = getLong(getColumnIndexOrThrow("date")),
        show = getInt(getColumnIndexOrThrow("show")),
        pointCount = getInt(getColumnIndexOrThrow("cnt")),
        duration = if (isNull(getColumnIndexOrThrow("duration"))) 0
                   else getInt(getColumnIndexOrThrow("duration")),
        distance = if (isNull(getColumnIndexOrThrow("distance"))) 0
                   else getInt(getColumnIndexOrThrow("distance")),
        categoryId = if (isNull(getColumnIndexOrThrow("categoryid"))) 0
                     else getInt(getColumnIndexOrThrow("categoryid")),
        activity = if (isNull(getColumnIndexOrThrow("activity"))) 0
                   else getInt(getColumnIndexOrThrow("activity")),
        style = getString(getColumnIndexOrThrow("style"))
    )
}

/**
 * Convert cursor from legacy 'trackpoints' table to TrackPointEntity
 * Legacy schema: trackid, id, lat, lon, alt, speed, date
 */
fun Cursor.toTrackPointEntity(): TrackPointEntity {
    return TrackPointEntity(
        id = getInt(getColumnIndexOrThrow("id")),
        trackId = getInt(getColumnIndexOrThrow("trackid")),
        latitude = getDouble(getColumnIndexOrThrow("lat")),
        longitude = getDouble(getColumnIndexOrThrow("lon")),
        altitude = getDouble(getColumnIndexOrThrow("alt")),
        speed = if (isNull(getColumnIndexOrThrow("speed"))) 0.0
                else getDouble(getColumnIndexOrThrow("speed")),
        date = getLong(getColumnIndexOrThrow("date"))
    )
}

/**
 * Convert cursor from legacy 'activity' table to ActivityEntity
 * Legacy schema: activityid, name
 */
fun Cursor.toActivityEntity(): ActivityEntity {
    return ActivityEntity(
        id = getInt(getColumnIndexOrThrow("activityid")),
        name = getString(getColumnIndexOrThrow("name")) ?: ""
    )
}

/**
 * Convert cursor from legacy 'maps' table to MapEntity
 * Legacy schema: mapid, name, type, params
 */
fun Cursor.toMapEntity(): MapEntity {
    return MapEntity(
        id = getInt(getColumnIndexOrThrow("mapid")),
        name = getString(getColumnIndexOrThrow("name")) ?: "",
        type = getInt(getColumnIndexOrThrow("type")),
        params = getString(getColumnIndexOrThrow("params")) ?: ""
    )
}

/**
 * Helper function to safely process all rows in a cursor
 */
inline fun <T> Cursor.mapRows(transform: (Cursor) -> T): List<T> {
    val results = mutableListOf<T>()
    use {
        while (moveToNext()) {
            results.add(transform(this))
        }
    }
    return results
}
