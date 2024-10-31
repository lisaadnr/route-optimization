package com.dicoding.route_optimization

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.route_optimization.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationAdapter: LocationAdapter
    private val locations = mutableListOf<UserLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        initMap()
        initLocationClient()
        setupSearchAutoComplete()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter(
            onLocationCheckedChange = { updatedLocation ->
                updateUserLocation(updatedLocation)
            },
            onDelete = { location ->
                deleteLocation(location)
            }
        )
        binding.rvListLocation.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = locationAdapter
        }
    }

    private fun initMap() {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        map = binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.apply {
                setCenter(GeoPoint(-6.21462, 106.84513))
                setZoom(12.5)
            }
        }
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        binding.mapIcon.setOnClickListener {
            fetchLastLocation()
        }
    }

    private fun setupSearchAutoComplete() {
        binding.searchAutocomplete.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    searchLocationSuggestions(s.toString())
                } else {
                    binding.searchAutocomplete.dismissDropDown()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * search, get, display -> untuk menampilkan suggestion di searchAutoCompleteBar
     */
    private fun searchLocationSuggestions(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val suggestions = getLocationSuggestions(query)
            withContext(Dispatchers.Main) {
                displayLocationSuggestions(suggestions)
            }
        }
    }

    private fun getLocationSuggestions(query: String): List<String> {
        val geoCoder = Geocoder(this)
        return try {
            val addressList = geoCoder.getFromLocationName(query, 10) ?: emptyList() // Deprecated
            addressList.map { "${it.featureName}, ${it.locality}, ${it.countryName}" }
        } catch (e: Exception) {
            Log.e("Search", "Error fetching location suggestions", e)
            emptyList()
        }
    }

    private fun displayLocationSuggestions(suggestions: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        binding.searchAutocomplete.apply {
            setAdapter(adapter)
            showDropDown()
            setOnItemClickListener { parent, _, position, _ ->
                val selectedLocation = parent.getItemAtPosition(position) as String
                addLocationToList(selectedLocation)
                text.clear()
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    fetchLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    fetchLastLocation()
                }
                else -> {
                    Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun fetchLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    showStartMarker(location)
                }
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun showStartMarker(location: Location) {
        val startLocation = GeoPoint(location.latitude, location.longitude)
        val marker = Marker(map).apply {
            position = startLocation
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
            title = "You're here"
        }

        map.overlays.add(marker)
        map.controller.animateTo(startLocation, 15.0, 2500)
        map.invalidate()
    }

    private fun addLocationToList(locationName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geoPoint = getLocationFromName(locationName)
            geoPoint?.let {
                val marker = addMarkerToMap(it, locationName)
                val location = UserLocation(
                    id = locations.size + 1,
                    locName = locationName,
                    isChecked = true,
                    latitude = marker.position.latitude,
                    longitude = marker.position.longitude,
                    marker = marker
                )
                Log.d("addUserLocation", location.toString())
                locations.add(location)

                withContext(Dispatchers.Main) {
                    locationAdapter.submitList(locations.toList())
                    updateOptimizeButtonState()
                }
            }
        }
    }

    private fun addMarkerToMap(geoPoint: GeoPoint, title: String?): Marker {
        val marker = Marker(map).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
            this.title = title ?: "Selected Location"
        }

        CoroutineScope(Dispatchers.Main).launch {
            map.overlays.add(marker)
            map.controller.animateTo(geoPoint, 15.0, 2500)
            map.invalidate()
        }
        return marker
    }

    private fun deleteMarkerFromMap(marker: Marker) {
        map.overlays.remove(marker)
        map.invalidate()
    }

    private fun getLocationFromName(locationName: String): GeoPoint? {
        val geoCoder = Geocoder(this)
        return try {
            val addressList = geoCoder.getFromLocationName(locationName, 1)
            if (addressList!!.isNotEmpty()) {
                val address: Address = addressList[0]
                GeoPoint(address.latitude, address.longitude)
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Error fetching location from name", e)
            null
        }
    }

    /**
     * BUG: 1. Jika ada dua lokasi sama dengan kondisi isChecked = null, lalu id ke (2) dengan berubah kondisi isChecked true, id ke (1) juga ikut (SOLVE)
     *      2. Bisakah recyclerview dibuat lazy state? jadi jika salah satu itemnya berubah maka list adapter tidak merubah state semuanya (SOLVE)
     *      3. Item recyclerview kedip-kedip jika diupdate
     */

    private fun updateUserLocation(updatedLocation: UserLocation) {
        Log.d("UpdateUserLocation", "Updated location received: $updatedLocation")

        val index = locations.indexOfFirst { it.id == updatedLocation.id }
        if (index != -1) {
            val currentLocation = locations[index]
            if (currentLocation.isChecked != updatedLocation.isChecked) {
                locations[index] = updatedLocation
                locationAdapter.notifyItemChanged(index)
                if (updatedLocation.isChecked) {
                    val geoPoint = GeoPoint(updatedLocation.latitude, updatedLocation.longitude)
                    geoPoint.let {
                        if (currentLocation.marker == null) {
                            val marker = addMarkerToMap(it, updatedLocation.locName)
                            locations[index] = locations[index].copy(marker = marker)
                            Log.d("locations", locations.toString())

                        }
                    }
                } else {
                    updatedLocation.marker?.let {
                        deleteMarkerFromMap(it)
                        locations[index] = locations[index].copy(marker = null)
                        Log.d("locations", locations.toString())
                    }
                }

                locationAdapter.submitList(locations.toList())
            }
        }
    }

    private fun deleteLocation(location: UserLocation) {
        CoroutineScope(Dispatchers.IO).launch {
            locations.remove(location)

            location.marker?.let {
                withContext(Dispatchers.Main) {
                    map.overlays.remove(it)
                    map.invalidate()
                }
            }

            withContext(Dispatchers.Main) {
                locationAdapter.submitList(locations.toList())
                updateOptimizeButtonState()
            }
        }
    }

    private fun updateOptimizeButtonState() {
        // Implement your logic for updating the optimize button state here
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }
}
