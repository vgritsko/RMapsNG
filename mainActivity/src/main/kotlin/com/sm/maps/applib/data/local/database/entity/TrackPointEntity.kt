package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(
    tableName = "trackpoints",
    indices = [
        Index("trackid"),
        Index("date")
    ]
)
data class TrackPointEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "trackid")
    val trackId: Int = 0,

    @ColumnInfo(name = "lat")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "lon")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "alt", defaultValue = "0.0")
    val altitude: Double = 0.0,

    @ColumnInfo(name = "speed", defaultValue = "0.0")
    val speed: Double = 0.0,

    @ColumnInfo(name = "date")
    val date: Long = 0L
)
