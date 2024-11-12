package com.dicoding.route_optimization.data.retrofit

import com.dicoding.route_optimization.data.response.RouteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/optimize")
    fun optimizeRoute(
        @Body requestData: RouteRequest)
    : Call<RouteResponse>
}

data class RouteRequest(
    val data: List<LocationData>
)

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

