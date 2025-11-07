package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(
    tableName = "points",
    indices = [
        Index("categoryid"),
        Index("lat", "lon"),
        Index("hidden")
    ]
)
data class PoiEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pointid")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "descr")
    val description: String = "",

    @ColumnInfo(name = "lat")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "lon")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "alt", defaultValue = "0.0")
    val altitude: Double = 0.0,

    @ColumnInfo(name = "hidden", defaultValue = "0")
    val hidden: Int = 0,

    @ColumnInfo(name = "categoryid", defaultValue = "0")
    val categoryId: Int = 0,

    @ColumnInfo(name = "pointsourceid", defaultValue = "0")
    val pointSourceId: Int = 0,

    @ColumnInfo(name = "iconid")
    val iconId: Int? = null
)
