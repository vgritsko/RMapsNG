package com.sm.maps.applib.presentation.ui.poi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sm.maps.applib.databinding.ItemPoiBinding
import com.sm.maps.applib.domain.model.PoiPoint

class PoiListAdapter(
    private val onVisibilityToggle: (PoiPoint) -> Unit
) : ListAdapter<PoiPoint, PoiListAdapter.ViewHolder>(DiffCallback()) {

    public override fun getItem(position: Int): PoiPoint = super.getItem(position)

    inner class ViewHolder(private val binding: ItemPoiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(poi: PoiPoint) {
            binding.tvPoiName.text = poi.name
            binding.tvPoiCoords.text = "%.5f, %.5f".format(poi.latitude, poi.longitude)

            binding.cbVisible.setOnCheckedChangeListener(null)
            binding.cbVisible.isChecked = !poi.hidden
            binding.cbVisible.setOnCheckedChangeListener { _, _ ->
                onVisibilityToggle(poi)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPoiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<PoiPoint>() {
        override fun areItemsTheSame(oldItem: PoiPoint, newItem: PoiPoint) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PoiPoint, newItem: PoiPoint) =
            oldItem == newItem
    }
}
