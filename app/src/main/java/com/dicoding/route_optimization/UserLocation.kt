package com.dicoding.route_optimization

import org.osmdroid.views.overlay.Marker

data class UserLocation(
    val id: Int,
    val locName: String? = null,
    val latitude: Double,
    val longitude: Double,
    var isChecked: Boolean = false,
    var marker: Marker? = null
)