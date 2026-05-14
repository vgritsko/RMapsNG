package com.sm.maps.applib.presentation.ui.track

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sm.maps.applib.R
import com.sm.maps.applib.databinding.FragmentTrackListBinding
import com.sm.maps.applib.domain.model.Track
import com.sm.maps.applib.presentation.base.BaseFragment
import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.base.UiState
import com.sm.maps.applib.presentation.viewmodel.TrackListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackListFragment : BaseFragment(R.layout.fragment_track_list) {

    interface OnTrackSelectedListener {
        fun onTrackSelected(trackId: Int)
    }

    private var listener: OnTrackSelectedListener? = null

    private var _binding: FragmentTrackListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrackListViewModel by viewModels()
    private lateinit var adapter: TrackListAdapter

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnTrackSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupViews() {
        adapter = TrackListAdapter(
            onVisibilityToggle = { track -> viewModel.toggleTrackVisibility(track.id) },
            onItemClick = { track -> listener?.onTrackSelected(track.id) }
        )
        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val track = adapter.getItem(viewHolder.bindingAdapterPosition)
                viewModel.deleteTrack(track)
            }
        }).attachToRecyclerView(binding.rvTracks)

        binding.fabAddTrack.setOnClickListener {
            // TODO: navigate to add track screen
        }

        binding.btnRetry.setOnClickListener {
            // TODO: add explicit reload method to ViewModel when needed
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trackListState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Empty -> showEmpty()
                        is UiState.Error -> showError(
                            state.message ?: state.exception.message ?: "Ошибка"
                        )
                        is UiState.Success -> showSuccess(state.data)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvTracks.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.rvTracks.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvTracks.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
        binding.btnRetry.visibility = View.VISIBLE
    }

    private fun showSuccess(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.rvTracks.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        adapter.submitList(tracks)
    }
}
