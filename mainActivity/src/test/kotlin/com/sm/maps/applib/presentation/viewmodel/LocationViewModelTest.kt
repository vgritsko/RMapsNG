package com.sm.maps.applib.presentation.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.sm.maps.applib.presentation.state.LocationPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeLocationManager : ILocationManagerWrapper {
    private val _providers = mutableListOf<String>()
    private val _enabled = mutableMapOf<String, Boolean>()

    override val allProviders: List<String> get() = _providers

    fun addProvider(provider: String, enabled: Boolean = true) {
        _providers.add(provider)
        _enabled[provider] = enabled
    }

    override fun isProviderEnabled(provider: String) = _enabled[provider] ?: false
    override fun requestLocationUpdates(provider: String, minTimeMs: Long, minDistanceM: Float, listener: LocationListener) {}
    override fun removeUpdates(listener: LocationListener) {}
    override fun getLastKnownLocation(provider: String): Location? = null
}

private class FakeSensorManager : ISensorManagerWrapper {
    override fun registerListener(listener: SensorEventListener, sensor: Sensor?, rateUs: Int) = true
    override fun unregisterListener(listener: SensorEventListener) {}
    override fun getDefaultSensor(type: Int): Sensor? = null
}

private class TestApplication : Application()

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LocationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LocationViewModel(
            TestApplication(),
            testDispatcher,
            FakeLocationManager(),
            FakeSensorManager()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun setAutoFollow_true_updatesState() = runTest(testDispatcher) {
        viewModel.setAutoFollow(true)
        assertTrue(viewModel.locationState.value.isFollowingLocation)
    }

    @Test
    fun setAutoFollow_false_updatesState() = runTest(testDispatcher) {
        viewModel.setAutoFollow(true)
        viewModel.setAutoFollow(false)
        assertFalse(viewModel.locationState.value.isFollowingLocation)
    }

    @Test
    fun setCompassEnabled_updatesState() = runTest(testDispatcher) {
        viewModel.setCompassEnabled(true)
        assertTrue(viewModel.locationState.value.isCompassEnabled)
    }

    @Test
    fun setCompassEnabled_false_updatesState() = runTest(testDispatcher) {
        viewModel.setCompassEnabled(true)
        viewModel.setCompassEnabled(false)
        assertFalse(viewModel.locationState.value.isCompassEnabled)
    }

    @Test
    fun initialState_isFollowingLocation_isTrue() {
        assertTrue(viewModel.locationState.value.isFollowingLocation)
    }

    @Test
    fun initialState_isCompassEnabled_isFalse() {
        assertFalse(viewModel.locationState.value.isCompassEnabled)
    }
}

// updateBearing тестируется напрямую через BearingCalculator — чистый Kotlin, без Android-зависимостей
class BearingCalculatorTest {

    @Test
    fun updateBearing_largeDiff_returnsNewBearing() {
        // lastBearing = 0, newBearing = 100 → diff = 100 >= 90 → direct update
        val result = BearingCalculator.update(newBearing = 100f, lastBearing = 0f)
        assertEquals(100f, result, 0.01f)
    }

    @Test
    fun updateBearing_tinyDiff_returnsUnchanged() {
        // lastBearing = 100, newBearing = 100.4 → diff = 0.4 < 1 → unchanged
        val result = BearingCalculator.update(newBearing = 100.4f, lastBearing = 100f)
        assertEquals(100f, result, 0.001f)
    }

    @Test
    fun updateBearing_smallDiff_returnsSmoothedValue() {
        // lastBearing = 0, newBearing = 45 → diff = 45, 1 <= 45 < 90
        // result = 0 + 90 * 1 * (45/90)^2 = 22.5
        val result = BearingCalculator.update(newBearing = 45f, lastBearing = 0f)
        assertTrue("Expected smoothed between 0 and 45, got $result", result > 0f && result < 45f)
        assertEquals(22.5f, result, 0.01f)
    }

    @Test
    fun updateBearing_negativeDiff_smoothsBackward() {
        // lastBearing = 45, newBearing = 10 → diff = -35, 1 <= 35 < 90
        // result = 45 + 90 * (-1) * (35/90)^2 = 45 - 90 * 0.1512 = 45 - 13.6 ≈ 31.4
        val result = BearingCalculator.update(newBearing = 10f, lastBearing = 45f)
        assertTrue("Expected result between 10 and 45, got $result", result > 10f && result < 45f)
    }

    @Test
    fun updateBearing_exactlyNinetyDegrees_returnsNewBearing() {
        val result = BearingCalculator.update(newBearing = 90f, lastBearing = 0f)
        assertEquals(90f, result, 0.01f)
    }
}
