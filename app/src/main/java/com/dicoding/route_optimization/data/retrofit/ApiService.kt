package com.dicoding.route_optimization.data.retrofit

import com.dicoding.route_optimization.data.response.OSRMResponse
import com.dicoding.route_optimization.data.response.RouteResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

interface OSRMService {
    @GET("route/v1/driving/{coordinates}")
    fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline"
    ): Call<OSRMResponse>
}
