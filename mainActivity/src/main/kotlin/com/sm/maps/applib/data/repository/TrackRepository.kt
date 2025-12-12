package com.sm.maps.applib.data.repository

import com.sm.maps.applib.data.local.database.dao.TrackDao
import com.sm.maps.applib.data.mapper.*
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.domain.repository.ITrackRepository
import com.sm.maps.applib.kml.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Track repository
 * Handles data operations for tracks using Room database
 */
@Singleton
class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ITrackRepository {
    override fun getAllTracks(): Flow<List<Track>> =
        trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVisibleTracksWithPoints(): Flow<List<Track>> =
        trackDao.getVisibleTracks().map { entities ->
            entities.map { trackEntity ->
                val points = trackDao.getTrackPoints(trackEntity.id)
                trackEntity.toDomainWithPoints(points)
            }
        }

    override suspend fun getTrackById(id: Int): Track? = withContext(ioDispatcher) {
        val trackEntity = trackDao.getTrackById(id) ?: return@withContext null
        val points = trackDao.getTrackPoints(id)
        trackEntity.toDomainWithPoints(points)
    }

    override suspend fun insertTrackWithPoints(track: Track): Long = withContext(ioDispatcher) {
        val trackEntity = track.toEntity()
        val trackId = trackDao.insertTrack(trackEntity)

        track.getPoints().forEach { point ->
            trackDao.insertTrackPoint(point.toEntity(trackId.toInt()))
        }

        trackId
    }

    override suspend fun updateTrack(track: Track) = withContext(ioDispatcher) {
        trackDao.updateTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: Track) = withContext(ioDispatcher) {
        trackDao.deleteTrack(track.toEntity())
    }

    override suspend fun toggleTrackVisibility(id: Int) = withContext(ioDispatcher) {
        trackDao.toggleTrackVisibility(id)
    }

    override suspend fun addPointToTrack(trackId: Int, point: Track.TrackPoint): Unit = withContext(ioDispatcher) {
        trackDao.insertTrackPoint(point.toEntity(trackId))
        Unit
    }
}
