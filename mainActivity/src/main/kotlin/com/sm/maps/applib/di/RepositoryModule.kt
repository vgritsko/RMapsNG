package com.sm.maps.applib.di

import com.sm.maps.applib.data.repository.PoiRepository
import com.sm.maps.applib.data.repository.TrackRepository
import com.sm.maps.applib.domain.repository.IPoiRepository
import com.sm.maps.applib.domain.repository.ITrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository instances
 * Uses @Binds to bind repository implementations to their interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind TrackRepository implementation to ITrackRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindTrackRepository(
        trackRepository: TrackRepository
    ): ITrackRepository

    /**
     * Bind PoiRepository implementation to IPoiRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindPoiRepository(
        poiRepository: PoiRepository
    ): IPoiRepository
}
