package com.sm.maps.applib.presentation.ui.map

import android.app.Activity
import android.os.Bundle
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
import com.sm.maps.applib.presentation.state.OverlayControlState
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import com.sm.maps.applib.presentation.viewmodel.OverlayControlViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayControlFragment : Fragment() {

    val viewModel: OverlayControlViewModel by viewModels()
    val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = requireActivity().getPreferences(Activity.MODE_PRIVATE)
        val compassEnabled = prefs.getBoolean("CompassEnabled", false)

        viewModel.initialize(compassEnabled)
        observeState()
        observeRefreshEvents()
    }

    private fun observeState() {
        var prevState: OverlayControlState? = null

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val prev = prevState

                    if (prev != null && state.compassEnabled != prev.compassEnabled) {
                        coordinatorViewModel.emitEvent(MapEvent.CompassToggled(state.compassEnabled))
                    }

                    prevState = state
                }
            }
        }
    }

    private fun observeRefreshEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshEvents.collect {
                    coordinatorViewModel.emitEvent(MapEvent.OverlayRefreshRequested)
                }
            }
        }
    }
}
