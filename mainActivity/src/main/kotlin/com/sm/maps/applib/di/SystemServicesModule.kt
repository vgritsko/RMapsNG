package com.sm.maps.applib.di

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import com.sm.maps.applib.presentation.viewmodel.ILocationManagerWrapper
import com.sm.maps.applib.presentation.viewmodel.ISensorManagerWrapper
import com.sm.maps.applib.presentation.viewmodel.LocationManagerWrapper
import com.sm.maps.applib.presentation.viewmodel.SensorManagerWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemServicesModule {

    @Provides
    @Singleton
    fun provideLocationManagerWrapper(@ApplicationContext context: Context): ILocationManagerWrapper =
        LocationManagerWrapper(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    @Provides
    @Singleton
    fun provideSensorManagerWrapper(@ApplicationContext context: Context): ISensorManagerWrapper =
        SensorManagerWrapper(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
}
