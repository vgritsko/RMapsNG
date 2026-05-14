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
import com.sm.maps.applib.presentation.state.MapEvent
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import com.sm.maps.applib.presentation.viewmodel.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapStateFragment : Fragment() {

    val viewModel: MapViewModel by viewModels()
    val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiState = requireActivity().getPreferences(Activity.MODE_PRIVATE)
        @Suppress("DEPRECATION")
        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        viewModel.initialize(
            latE6 = uiState.getInt("Latitude", 0),
            lonE6 = uiState.getInt("Longitude", 0),
            zoomLevel = uiState.getInt("ZoomLevel", 0),
            autoFollow = uiState.getBoolean("AutoFollow", true),
            drivingDirectionUp = pref.getBoolean("pref_drivingdirectionup", true),
            northDirectionUp = pref.getBoolean("pref_northdirectionup", true)
        )

        observeEvents()
    }

    override fun onStop() {
        super.onStop()
        val state = viewModel.state.value
        requireActivity().getPreferences(Activity.MODE_PRIVATE).edit()
            .putInt("Latitude", state.latE6)
            .putInt("Longitude", state.lonE6)
            .putInt("ZoomLevel", state.zoomLevel)
            .putBoolean("AutoFollow", state.autoFollow)
            .apply()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                coordinatorViewModel.mapEvents.collect { event ->
                    when (event) {
                        is MapEvent.MapViewportChanged -> viewModel.updateViewport(event.latE6, event.lonE6, event.zoom)
                        is MapEvent.RotationChanged -> viewModel.updateBearing(event.bearing)
                        is MapEvent.AutoFollowChanged -> viewModel.updateAutoFollow(event.enabled)
                        else -> Unit
                    }
                }
            }
        }
    }
}
