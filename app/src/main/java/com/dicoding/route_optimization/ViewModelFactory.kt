package com.dicoding.route_optimization

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.route_optimization.data.repository.RouteRepository
import com.dicoding.route_optimization.di.Injection

class ViewModelFactory(
    private var routeRepository: RouteRepository
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(routeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    Injection.provideOptimizeRepository(context)
                )
            }.also { instance = it }
    }
}