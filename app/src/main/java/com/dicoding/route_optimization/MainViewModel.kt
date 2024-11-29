package com.dicoding.route_optimization

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.route_optimization.data.UserPreferences
import com.dicoding.route_optimization.data.repository.RouteRepository
import com.dicoding.route_optimization.data.response.RouteResponse
import com.dicoding.route_optimization.data.result.Result
import com.dicoding.route_optimization.data.retrofit.LocationData

class MainViewModel(private val routeRepository: RouteRepository) : ViewModel() {

    private val _locations = MutableLiveData<List<UserLocation>>()
    val locations: LiveData<List<UserLocation>> = _locations

    fun optimizeRoute(list: List<LocationData>): LiveData<Result<RouteResponse>> {
        return routeRepository.optimizeRoute(list).asLiveData()
    }



}