package com.sm.maps.applib.data.local.database.dao

import androidx.room.*
import com.sm.maps.applib.data.local.database.entity.TrackEntity
import com.sm.maps.applib.data.local.database.entity.TrackPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY trackid DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE show = 1")
    fun getVisibleTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE trackid = :id")
    suspend fun getTrackById(id: Int): TrackEntity?

    @Query("SELECT * FROM trackpoints WHERE trackid = :trackId ORDER BY id")
    suspend fun getTrackPoints(trackId: Int): List<TrackPointEntity>

    @Query("SELECT * FROM trackpoints WHERE trackid = :trackId ORDER BY id")
    fun getTrackPointsFlow(trackId: Int): Flow<List<TrackPointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoint(point: TrackPointEntity): Long

    @Insert
    suspend fun insertTrackPoints(points: List<TrackPointEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE trackid = :id")
    suspend fun deleteTrackById(id: Int)

    @Query("UPDATE tracks SET show = 1 - show WHERE trackid = :id")
    suspend fun toggleTrackVisibility(id: Int)

    @Transaction
    suspend fun insertTrackWithPoints(
        track: TrackEntity,
        points: List<TrackPointEntity>
    ): Long {
        val trackId = insertTrack(track)
        val pointsWithTrackId = points.map { it.copy(trackId = trackId.toInt()) }
        insertTrackPoints(pointsWithTrackId)
        return trackId
    }

    // Synchronous methods for data migration
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackSync(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrackPointSync(point: TrackPointEntity): Long
}
