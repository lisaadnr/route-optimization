package com.dicoding.route_optimization.data.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(
	@SerializedName("info")
	val info: Info,

	@SerializedName("data")
	val data: List<Double>
)

data class Info(
	val status: Boolean,
	val meta: Any?,
	val message: String
)