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
import com.sm.maps.applib.presentation.state.MapSourceState
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import com.sm.maps.applib.presentation.viewmodel.MapSourceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapSourceSelectionFragment : Fragment() {

    val viewModel: MapSourceViewModel by viewModels()
    val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = requireActivity().getPreferences(Activity.MODE_PRIVATE)
        val mapId = prefs.getString("MapName", "mapnik") ?: "mapnik"
        val overlayId = prefs.getString("OverlayID", "") ?: ""
        val showOverlay = prefs.getBoolean("ShowOverlay", true)

        viewModel.initialize(mapId, overlayId, showOverlay)
        observeMapSourceState()
    }

    private fun observeMapSourceState() {
        var prevState: MapSourceState? = null

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mapSourceState.collect { state ->
                    val prev = prevState

                    if (prev != null && (state.mapId != prev.mapId ||
                            state.overlayId != prev.overlayId ||
                            state.showOverlay != prev.showOverlay)) {
                        coordinatorViewModel.emitEvent(
                            MapEvent.MapSourceChanged(state.mapId, state.overlayId, state.showOverlay)
                        )
                    }

                    prevState = state
                }
            }
        }
    }
}
