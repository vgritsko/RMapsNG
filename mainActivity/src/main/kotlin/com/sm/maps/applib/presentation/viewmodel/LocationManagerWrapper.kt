package com.sm.maps.applib.presentation.viewmodel

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

interface ILocationManagerWrapper {
    val allProviders: List<String>
    fun isProviderEnabled(provider: String): Boolean
    fun requestLocationUpdates(provider: String, minTimeMs: Long, minDistanceM: Float, listener: LocationListener)
    fun removeUpdates(listener: LocationListener)
    fun getLastKnownLocation(provider: String): Location?
}

class LocationManagerWrapper(private val lm: LocationManager) : ILocationManagerWrapper {
    override val allProviders: List<String> get() = lm.allProviders
    override fun isProviderEnabled(provider: String) = lm.isProviderEnabled(provider)
    override fun requestLocationUpdates(provider: String, minTimeMs: Long, minDistanceM: Float, listener: LocationListener) =
        lm.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)
    override fun removeUpdates(listener: LocationListener) = lm.removeUpdates(listener)
    override fun getLastKnownLocation(provider: String): Location? =
        try { lm.getLastKnownLocation(provider) } catch (e: SecurityException) { null }
}
