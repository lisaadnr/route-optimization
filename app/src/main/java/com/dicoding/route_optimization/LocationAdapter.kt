package com.dicoding.route_optimization

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.route_optimization.databinding.ItemLocationBinding


class LocationAdapter(
    private val onLocationCheckedChange: (UserLocation) -> Unit,
    private val onDelete: (UserLocation) -> Unit
): ListAdapter<UserLocation, LocationAdapter.LocationViewHolder>(LocationDiffCallback) {
    class LocationViewHolder(private val binding: ItemLocationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(userLocation: UserLocation, onLocationCheckedChange: (UserLocation) -> Unit, onDelete: (UserLocation) -> Unit) {
            binding.apply {
                cbLocation.isChecked = userLocation.isChecked
                tvLocation.text = userLocation.locName

                cbLocation.setOnCheckedChangeListener { _, isChecked ->
                    onLocationCheckedChange(userLocation.copy(isChecked = isChecked))
                }

                icDelete.setOnClickListener {
                    onDelete(userLocation)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position), onLocationCheckedChange, onDelete)
    }

    object LocationDiffCallback: DiffUtil.ItemCallback<UserLocation>() {
        override fun areItemsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserLocation, newItem: UserLocation): Boolean {
            return oldItem.id == newItem.id
        }

    }
}