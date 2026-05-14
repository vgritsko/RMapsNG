package com.sm.maps.applib.presentation.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sm.maps.applib.databinding.ItemTrackBinding
import com.sm.maps.applib.domain.model.Track
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackListAdapter(
    private val onVisibilityToggle: (Track) -> Unit,
    private val onItemClick: (Track) -> Unit = {}
) : ListAdapter<Track, TrackListAdapter.ViewHolder>(DiffCallback()) {

    public override fun getItem(position: Int): Track = super.getItem(position)

    inner class ViewHolder(private val binding: ItemTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        fun bind(track: Track) {
            binding.tvTrackName.text = track.name.ifEmpty { "Без названия" }
            binding.tvTrackDate.text = dateFormat.format(Date(track.date))
            binding.tvTrackDistance.text = formatDistance(track.distance)
            binding.tvTrackDuration.text = formatDuration(track.duration)

            binding.cbVisible.setOnCheckedChangeListener(null)
            binding.cbVisible.isChecked = track.visible
            binding.cbVisible.setOnCheckedChangeListener { _, _ ->
                onVisibilityToggle(track)
            }

            binding.root.setOnClickListener { onItemClick(track) }
        }

        private fun formatDistance(meters: Int): String =
            if (meters >= 1000) "%.1f км".format(meters / 1000f) else "$meters м"

        private fun formatDuration(seconds: Int): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return "%02d:%02d:%02d".format(h, m, s)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Track, newItem: Track) = oldItem == newItem
    }
}
