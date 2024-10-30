package com.dicoding.route_optimization.data.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(

	@field:SerializedName("data")
	val data: List<DataItem>
)

data class DataItem(

	@field:SerializedName("latitude")
	val latitude: Any,

	@field:SerializedName("longitude")
	val longitude: Any
)
