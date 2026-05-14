package com.sm.maps.applib.presentation.ui.map

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sm.maps.applib.presentation.state.LocationPrefs
import com.sm.maps.applib.presentation.state.LocationState
import com.sm.maps.applib.presentation.state.MapEvent
import com.sm.maps.applib.presentation.viewmodel.LocationViewModel
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationTrackingFragment : Fragment() {

    val locationViewModel: LocationViewModel by viewModels()
    val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val uiState = requireActivity().getPreferences(Activity.MODE_PRIVATE)

        locationViewModel.initialize(
            LocationPrefs(
                fastUpdate = defaultPrefs.getBoolean("pref_gpsfastupdate", true),
                drivingDirectionUp = defaultPrefs.getBoolean("pref_drivingdirectionup", true),
                northDirectionUp = defaultPrefs.getBoolean("pref_northdirectionup", true)
            )
        )

        locationViewModel.setAutoFollow(uiState.getBoolean("AutoFollow", true))
        locationViewModel.setCompassEnabled(uiState.getBoolean("CompassEnabled", false))

        observeLocationState()
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationViewModel.stopLocationUpdates()
    }

    private fun observeLocationState() {
        var prevState: LocationState? = null

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.locationState.collect { state ->
                    val prev = prevState

                    if (prev == null || state.currentLocation != prev.currentLocation) {
                        state.currentLocation?.let {
                            coordinatorViewModel.emitEvent(MapEvent.LocationUpdated(it))
                        }
                    }

                    if (prev == null || state.bearing != prev.bearing) {
                        coordinatorViewModel.emitEvent(MapEvent.BearingChanged(state.bearing))
                    }

                    if (prev == null || state.isFollowingLocation != prev.isFollowingLocation) {
                        coordinatorViewModel.emitEvent(MapEvent.AutoFollowChanged(state.isFollowingLocation))
                    }

                    prevState = state
                }
            }
        }
    }
}
