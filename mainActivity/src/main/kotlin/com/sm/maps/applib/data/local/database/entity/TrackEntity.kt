package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(
    tableName = "tracks",
    indices = [
        Index("show"),
        Index("date"),
        Index("activity")
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trackid")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "descr")
    val description: String = "",

    @ColumnInfo(name = "date")
    val date: Long = 0L,

    @ColumnInfo(name = "show", defaultValue = "0")
    val show: Int = 0,

    @ColumnInfo(name = "cnt", defaultValue = "0")
    val pointCount: Int = 0,

    @ColumnInfo(name = "duration", defaultValue = "0")
    val duration: Int = 0,

    @ColumnInfo(name = "distance", defaultValue = "0")
    val distance: Int = 0,

    @ColumnInfo(name = "categoryid", defaultValue = "0")
    val categoryId: Int = 0,

    @ColumnInfo(name = "activity", defaultValue = "0")
    val activity: Int = 0,

    @ColumnInfo(name = "style")
    val style: String? = null
)
