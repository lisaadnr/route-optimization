package com.dicoding.route_optimization.di

import android.content.Context
import com.dicoding.route_optimization.data.repository.RouteRepository
import com.dicoding.route_optimization.data.retrofit.ApiConfig

object Injection {
    fun provideOptimizeRepository(context: Context): RouteRepository {
        val apiService = ApiConfig.getApiService()
        return RouteRepository.getInstance(apiService)
    }
}