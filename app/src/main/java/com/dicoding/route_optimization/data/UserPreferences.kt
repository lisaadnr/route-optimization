package com.dicoding.route_optimization.data

import android.content.Context
import android.content.SharedPreferences
import com.dicoding.route_optimization.data.retrofit.LocationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("route_optimization_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val LOCATIONS_KEY = "locations_key"
    }

    // Menyimpan data lokasi
    fun saveLocations(locations: List<LocationData>) {
        val json = gson.toJson(locations)
        sharedPreferences.edit().putString(LOCATIONS_KEY, json).apply()
    }

    // Mengambil data lokasi
    fun loadLocations(): List<LocationData> {
        val json = sharedPreferences.getString(LOCATIONS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<LocationData>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Menghapus data lokasi
    fun clearLocations() {
        sharedPreferences.edit().remove(LOCATIONS_KEY).apply()
    }
}