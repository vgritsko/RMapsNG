package com.sm.maps.applib.presentation.viewmodel

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

interface ISensorManagerWrapper {
    fun registerListener(listener: SensorEventListener, sensor: Sensor?, rateUs: Int): Boolean
    fun unregisterListener(listener: SensorEventListener)
    fun getDefaultSensor(type: Int): Sensor?
}

class SensorManagerWrapper(private val sm: SensorManager) : ISensorManagerWrapper {
    override fun registerListener(listener: SensorEventListener, sensor: Sensor?, rateUs: Int) =
        sm.registerListener(listener, sensor, rateUs)
    override fun unregisterListener(listener: SensorEventListener) =
        sm.unregisterListener(listener)
    @Suppress("DEPRECATION")
    override fun getDefaultSensor(type: Int): Sensor? = sm.getDefaultSensor(type)
}
