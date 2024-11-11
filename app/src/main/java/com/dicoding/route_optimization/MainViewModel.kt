package com.dicoding.route_optimization

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.route_optimization.data.repository.RouteRepository
import com.dicoding.route_optimization.data.response.RouteResponse
import com.dicoding.route_optimization.data.result.Result
import com.dicoding.route_optimization.data.retrofit.LocationData

class MainViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    fun optimizeRoute(list: List<LocationData>): LiveData<Result<RouteResponse>> {
        return routeRepository.optimizeRoute(list).asLiveData()
    }

}