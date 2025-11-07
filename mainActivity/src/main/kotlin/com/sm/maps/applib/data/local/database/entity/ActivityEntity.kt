package com.sm.maps.applib.data.local.database.entity

import androidx.room.*

@Entity(tableName = "activity")
data class ActivityEntity(
    @PrimaryKey
    @ColumnInfo(name = "activityid")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String = ""
)
