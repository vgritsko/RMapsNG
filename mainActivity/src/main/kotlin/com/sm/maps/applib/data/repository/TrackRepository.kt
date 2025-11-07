package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.TrackDao
import com.sm.maps.applib.data.mapper.*
import com.sm.maps.applib.kml.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackRepository(
    private val trackDao: TrackDao
) {
    fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getVisibleTracksWithPoints(): Flow<List<Track>> =
        trackDao.getVisibleTracks().map { entities ->
            entities.map { trackEntity ->
                val points = trackDao.getTrackPoints(trackEntity.id)
                trackEntity.toDomainWithPoints(points)
            }
        }

    suspend fun getTrackById(id: Int): Track? = withContext(Dispatchers.IO) {
        val trackEntity = trackDao.getTrackById(id) ?: return@withContext null
        val points = trackDao.getTrackPoints(id)
        trackEntity.toDomainWithPoints(points)
    }

    suspend fun insertTrackWithPoints(track: Track): Long = withContext(Dispatchers.IO) {
        val trackEntity = track.toEntity()
        val trackId = trackDao.insertTrack(trackEntity)

        track.getPoints().forEach { point ->
            trackDao.insertTrackPoint(point.toEntity(trackId.toInt()))
        }

        trackId
    }

    suspend fun updateTrack(track: Track) = withContext(Dispatchers.IO) {
        trackDao.updateTrack(track.toEntity())
    }

    suspend fun deleteTrack(track: Track) = withContext(Dispatchers.IO) {
        trackDao.deleteTrack(track.toEntity())
    }

    suspend fun toggleTrackVisibility(id: Int) = withContext(Dispatchers.IO) {
        trackDao.toggleTrackVisibility(id)
    }

    suspend fun addPointToTrack(trackId: Int, point: Track.TrackPoint) = withContext(Dispatchers.IO) {
        trackDao.insertTrackPoint(point.toEntity(trackId))
    }
}
