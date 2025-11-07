package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(
    tableName = "category",
    indices = [Index("name")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "categoryid")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "hidden", defaultValue = "0")
    val hidden: Int = 0,

    @ColumnInfo(name = "iconid")
    val iconId: Int? = null,

    @ColumnInfo(name = "minzoom", defaultValue = "14")
    val minZoom: Int = 14
)
