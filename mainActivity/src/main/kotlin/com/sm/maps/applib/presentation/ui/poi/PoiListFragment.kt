package com.sm.maps.applib.presentation.ui.poi

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
import com.sm.maps.applib.databinding.FragmentPoiListBinding
import com.sm.maps.applib.domain.model.PoiPoint
import com.sm.maps.applib.presentation.base.BaseFragment
import com.sm.maps.applib.presentation.base.BaseViewModel
import com.sm.maps.applib.presentation.base.UiState
import com.sm.maps.applib.presentation.viewmodel.PoiListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PoiListFragment : BaseFragment(R.layout.fragment_poi_list) {

    private var _binding: FragmentPoiListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PoiListViewModel by viewModels()
    private lateinit var adapter: PoiListAdapter

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoiListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setupViews() {
        adapter = PoiListAdapter(onVisibilityToggle = { poi -> viewModel.togglePoiVisibility(poi) })
        binding.rvPois.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPois.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val poi = adapter.getItem(viewHolder.bindingAdapterPosition)
                viewModel.deletePoi(poi)
            }
        }).attachToRecyclerView(binding.rvPois)

        binding.fabAddPoi.setOnClickListener {
            // TODO: navigate to add POI screen
        }

        binding.btnRetry.setOnClickListener {
            // TODO: add explicit reload method to ViewModel when needed
        }
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.poiListState.collect { state ->
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
        binding.rvPois.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.rvPois.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvPois.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message
        binding.btnRetry.visibility = View.VISIBLE
    }

    private fun showSuccess(pois: List<PoiPoint>) {
        binding.progressBar.visibility = View.GONE
        binding.rvPois.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        binding.tvError.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        adapter.submitList(pois)
    }
}
