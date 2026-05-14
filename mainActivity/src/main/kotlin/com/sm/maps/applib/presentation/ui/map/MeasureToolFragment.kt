package com.sm.maps.applib.presentation.ui.map

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
import com.sm.maps.applib.presentation.state.MeasureState
import com.sm.maps.applib.presentation.viewmodel.MapCoordinatorViewModel
import com.sm.maps.applib.presentation.viewmodel.MeasureToolViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MeasureToolFragment : Fragment() {

    val viewModel: MeasureToolViewModel by viewModels()
    val coordinatorViewModel: MapCoordinatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        viewModel.initialize(
            showInfoBubble = prefs.getBoolean("pref_show_measure_info", true),
            showLineInfo = prefs.getBoolean("pref_show_measure_line_info", true)
        )

        observeMeasureState()
    }

    private fun observeMeasureState() {
        var prevState: MeasureState? = null

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measureState.collect { state ->
                    val prev = prevState

                    if (prev == null || state.isActive != prev.isActive) {
                        if (state.isActive) {
                            coordinatorViewModel.emitEvent(MapEvent.MeasureToolOpen)
                        } else if (prev != null) {
                            coordinatorViewModel.emitEvent(MapEvent.MeasureToolClose)
                        }
                    }

                    if (prev != null && state.showInfoBubble != prev.showInfoBubble) {
                        coordinatorViewModel.emitEvent(MapEvent.MeasureShowInfoBubble(state.showInfoBubble))
                    }

                    if (prev != null && state.showLineInfo != prev.showLineInfo) {
                        coordinatorViewModel.emitEvent(MapEvent.MeasureShowLineInfo(state.showLineInfo))
                    }

                    prevState = state
                }
            }
        }
    }
}
