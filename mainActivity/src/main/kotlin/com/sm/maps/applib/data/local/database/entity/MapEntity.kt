package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(tableName = "maps")
data class MapEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "mapid")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "type")
    val type: Int = 0,

    @ColumnInfo(name = "params")
    val params: String = ""
)
