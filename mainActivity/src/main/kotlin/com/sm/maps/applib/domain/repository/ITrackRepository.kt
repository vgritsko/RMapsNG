package com.sm.maps.applib.domain.repository

import com.sm.maps.applib.kml.Track
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Track data operations
 * Follows clean architecture - defines contract for data layer
 */
interface ITrackRepository {
    /**
     * Get all tracks as a Flow
     */
    fun getAllTracks(): Flow<List<Track>>

    /**
     * Get only visible tracks with their points
     */
    fun getVisibleTracksWithPoints(): Flow<List<Track>>

    /**
     * Get a specific track by ID
     */
    suspend fun getTrackById(id: Int): Track?

    /**
     * Insert a track with its points
     * @return the ID of the inserted track
     */
    suspend fun insertTrackWithPoints(track: Track): Long

    /**
     * Update an existing track
     */
    suspend fun updateTrack(track: Track)

    /**
     * Delete a track
     */
    suspend fun deleteTrack(track: Track)

    /**
     * Toggle track visibility (show/hide)
     */
    suspend fun toggleTrackVisibility(id: Int)

    /**
     * Add a point to an existing track
     */
    suspend fun addPointToTrack(trackId: Int, point: Track.TrackPoint)
}
