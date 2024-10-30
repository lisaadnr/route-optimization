package com.dicoding.route_optimization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.route_optimization.data.response.RouteResponse
import com.dicoding.route_optimization.data.retrofit.ApiService

class UserRepository private constructor(
    private val apiService: ApiService
) {
    private val _routeResponse = MutableLiveData<RouteResponse>()
    val routeResponse: LiveData<RouteResponse> = _routeResponse
}