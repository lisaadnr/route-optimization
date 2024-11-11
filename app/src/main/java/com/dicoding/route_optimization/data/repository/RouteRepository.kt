package com.dicoding.route_optimization.data.repository

import android.util.Log
import com.dicoding.route_optimization.data.response.RouteResponse
import com.dicoding.route_optimization.data.result.Result
import com.dicoding.route_optimization.data.retrofit.ApiService
import com.dicoding.route_optimization.data.retrofit.LocationData
import com.dicoding.route_optimization.data.retrofit.RouteRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse

class RouteRepository private constructor(
    private val apiService: ApiService
) {
    fun optimizeRoute(listLoc: List<LocationData>): Flow<Result<RouteResponse>> = flow {
        emit(Result.Loading)
        try {
            val routeRequest = RouteRequest(locations = listLoc)
            Log.d("TEST", routeRequest.toString())
            val response = apiService.optimizeRoute(routeRequest).awaitResponse()
            if (response.isSuccessful) {
                val optimized = response.body()
                if (optimized != null) {
                    emit(Result.Success(optimized))
                } else {
                    emit(Result.Error("No data found $response"))
                }
            } else {
                emit(Result.Error("Response not successful $response"))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error"))
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RouteRepository? = null
        fun getInstance(
            apiService: ApiService
        ): RouteRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RouteRepository(apiService)
            }.also { INSTANCE = it }
    }
}