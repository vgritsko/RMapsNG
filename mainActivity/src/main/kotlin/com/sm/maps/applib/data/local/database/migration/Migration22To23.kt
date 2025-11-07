package com.sm.maps.applib.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS index_points_categoryid ON points(categoryid)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_points_coords ON points(lat, lon)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_points_hidden ON points(hidden)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_trackpoints_trackid ON trackpoints(trackid)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_trackpoints_date ON trackpoints(date)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_tracks_show ON tracks(show)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_tracks_date ON tracks(date)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_category_name ON category(name)")
    }
}
