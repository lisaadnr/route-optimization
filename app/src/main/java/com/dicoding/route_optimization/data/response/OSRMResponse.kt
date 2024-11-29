package com.dicoding.route_optimization.data.response

import com.google.gson.annotations.SerializedName

data class OSRMResponse(

	@field:SerializedName("routes")
	val routes: List<RoutesItem>,

	@field:SerializedName("code")
	val code: String,

	@field:SerializedName("waypoints")
	val waypoints: List<WaypointsItem>
)

data class LegsItem(

	@field:SerializedName("summary")
	val summary: String,

	@field:SerializedName("duration")
	val duration: Any,

	@field:SerializedName("distance")
	val distance: Double,

	@field:SerializedName("weight")
	val weight: Any,

	@field:SerializedName("steps")
	val steps: List<Any>
)

data class RoutesItem(

	@field:SerializedName("duration")
	val duration: Any,

	@field:SerializedName("distance")
	val distance: Double,

	@field:SerializedName("legs")
	val legs: List<LegsItem>,

	@field:SerializedName("weight_name")
	val weightName: String,

	@field:SerializedName("weight")
	val weight: Any,

	@field:SerializedName("geometry")
	val geometry: String
)

data class WaypointsItem(

	@field:SerializedName("distance")
	val distance: Any,

	@field:SerializedName("hint")
	val hint: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("location")
	val location: List<Any>
)
