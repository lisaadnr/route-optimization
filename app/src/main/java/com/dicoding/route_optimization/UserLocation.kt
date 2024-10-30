package com.dicoding.route_optimization

import org.osmdroid.views.overlay.Marker

data class UserLocation (
    val id: Int,
    val locName: String? = null,
    var isChecked: Boolean = false,
    var marker: Marker? = null
)