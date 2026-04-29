package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.domain.model.TrackPoint
import kotlinx.coroutines.flow.Flow

interface ITrackRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun getVisibleTracksWithPoints(): Flow<List<Track>>
    suspend fun getTrackById(id: Int): Track?
    suspend fun insertTrackWithPoints(track: Track): Long
    suspend fun updateTrack(track: Track)
    suspend fun deleteTrack(track: Track)
    suspend fun toggleTrackVisibility(id: Int)
    suspend fun addPointToTrack(trackId: Int, point: TrackPoint)
}
