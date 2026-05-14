package com.sm.maps.applib.presentation.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import com.sm.maps.applib.di.IoDispatcher
import com.sm.maps.applib.presentation.state.LocationPrefs
import com.sm.maps.applib.presentation.state.LocationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val locationManagerWrapper: ILocationManagerWrapper,
    private val sensorManagerWrapper: ISensorManagerWrapper
) : AndroidViewModel(application) {

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var prefs = LocationPrefs()
    private var lastBearing = 0f
    private var netListener: LocationListener? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location.provider == LocationManager.GPS_PROVIDER && netListener != null) {
                locationManagerWrapper.removeUpdates(netListener!!)
                netListener = null
                _locationState.update { it.copy(providerName = LocationManager.GPS_PROVIDER) }
            }

            val smoothedBearing = if (prefs.drivingDirectionUp && location.speed > 0.5f) {
                updateBearing(location.bearing)
            } else {
                _locationState.value.bearing
            }

            _locationState.update {
                it.copy(
                    currentLocation = location,
                    providerName = location.provider ?: "",
                    speed = location.speed,
                    bearing = smoothedBearing
                )
            }
        }

        override fun onProviderDisabled(provider: String) {
            if (provider.equals(LocationManager.GPS_PROVIDER, ignoreCase = true) && netListener != null) {
                _locationState.update { it.copy(providerName = LocationManager.NETWORK_PROVIDER) }
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER, ignoreCase = true) && netListener != null) {
                locationManagerWrapper.removeUpdates(netListener!!)
                netListener = null
                val newProvider = if (locationManagerWrapper.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    OFF_PROVIDER
                }
                _locationState.update { it.copy(providerName = newProvider) }
            } else {
                _locationState.update { it.copy(providerName = OFF_PROVIDER) }
            }
        }

        override fun onProviderEnabled(provider: String) {
            if (provider.equals(LocationManager.GPS_PROVIDER, ignoreCase = true) && netListener == null) {
                _locationState.update { it.copy(providerName = LocationManager.GPS_PROVIDER) }
            }
        }

        @Deprecated("Deprecated in API 29")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            val satCount = extras.getInt("satellites", -1)
            _locationState.update {
                it.copy(
                    providerName = provider,
                    providerStatus = status,
                    satelliteCount = satCount
                )
            }
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        private var screenOrientation = -1

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (screenOrientation < 0) {
                val wm = getApplication<Application>().getSystemService(WindowManager::class.java)
                @Suppress("DEPRECATION")
                screenOrientation = wm.defaultDisplay.orientation
            }

            val azimuth = event.values[0] + 90f * screenOrientation
            val state = _locationState.value

            if (state.isCompassEnabled && prefs.northDirectionUp &&
                (!prefs.drivingDirectionUp || state.speed == 0f)
            ) {
                val smoothed = updateBearing(event.values[0]) + 90f * screenOrientation
                _locationState.update { it.copy(compassAzimuth = azimuth, bearing = smoothed) }
            } else {
                _locationState.update { it.copy(compassAzimuth = azimuth) }
            }
        }
    }

    fun initialize(newPrefs: LocationPrefs) {
        prefs = newPrefs
    }

    fun startLocationUpdates() {
        val providers = locationManagerWrapper.allProviders
        val minTime = if (prefs.fastUpdate) 0L else 2000L
        val minDistance = if (prefs.fastUpdate) 0f else 20f

        locationManagerWrapper.removeUpdates(locationListener)
        netListener?.let { locationManagerWrapper.removeUpdates(it) }
        netListener = null

        _locationState.update { it.copy(providerName = OFF_PROVIDER) }

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationManagerWrapper.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener
            )
            if (locationManagerWrapper.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                _locationState.update { it.copy(providerName = LocationManager.GPS_PROVIDER) }
            }
            try {
                if (locationManagerWrapper.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    netListener = locationListener
                    locationManagerWrapper.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener
                    )
                    _locationState.update { it.copy(providerName = LocationManager.NETWORK_PROVIDER) }
                }
            } catch (e: Exception) {
                // Network provider may not be available on all devices
            }
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER) &&
            locationManagerWrapper.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            locationManagerWrapper.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener
            )
            _locationState.update { it.copy(providerName = LocationManager.NETWORK_PROVIDER) }
        }

        if (_locationState.value.isCompassEnabled) {
            registerSensor()
        }
    }

    fun stopLocationUpdates() {
        locationManagerWrapper.removeUpdates(locationListener)
        netListener?.let {
            locationManagerWrapper.removeUpdates(it)
            netListener = null
        }
        sensorManagerWrapper.unregisterListener(sensorEventListener)
    }

    fun setAutoFollow(enabled: Boolean) {
        _locationState.update { it.copy(isFollowingLocation = enabled) }
    }

    fun setCompassEnabled(enabled: Boolean) {
        _locationState.update { it.copy(isCompassEnabled = enabled) }
        if (enabled) {
            registerSensor()
        } else {
            sensorManagerWrapper.unregisterListener(sensorEventListener)
        }
    }

    fun getLastKnownLocation(): Location? {
        val overlayLocation = _locationState.value.currentLocation
        if (overlayLocation != null) return overlayLocation

        val loc1 = locationManagerWrapper.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val loc2 = locationManagerWrapper.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        return when {
            loc1 == null && loc2 != null -> loc2
            loc1 != null && loc2 == null -> loc1
            loc1 == null && loc2 == null -> null
            else -> if (loc1!!.time > loc2!!.time) loc1 else loc2
        }
    }

    fun isGpsEnabled(): Boolean = locationManagerWrapper.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun isNetworkEnabled(): Boolean = locationManagerWrapper.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    private fun registerSensor() {
        sensorManagerWrapper.registerListener(
            sensorEventListener,
            sensorManagerWrapper.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    private fun updateBearing(newBearing: Float): Float {
        lastBearing = BearingCalculator.update(newBearing, lastBearing)
        return lastBearing
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    companion object {
        const val OFF_PROVIDER = "off"
    }
}
