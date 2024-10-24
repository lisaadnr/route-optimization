package com.dicoding.route_optimization

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.route_optimization.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
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
    private lateinit var mapIcon: ImageView
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var locationListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationListLayout = findViewById(R.id.locationListLayout)
        binding.btnOptimizeRoute.isEnabled = false

        autoCompleteTextView = findViewById(R.id.search_autocomplete)

        // TextWatcher input pengguna
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    searchLocationSuggestions(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Optional: Set how many characters needed before suggestions appear
        autoCompleteTextView.threshold = 1  // Suggest after 1 character is typed

        // Konfigurasi Map
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        mapIcon = binding.mapIcon

        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val mapController = map.controller
        mapController.setZoom(12.5)
        val startPoint = GeoPoint(-6.21462, 106.84513)
        mapController.setCenter(startPoint)

        // last loc
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        // *temp button
        binding.btnOptimizeRoute.setOnClickListener {
           // searchLocationSuggestions(String.toString())
        }

        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedLocation = parent.getItemAtPosition(position) as String
            Log.d("coba", "Selected location: $selectedLocation")
            addLocationToList(selectedLocation)

            val geoCoder = Geocoder(this)
            var addressList: List<Address>? = null
            try {
                addressList = geoCoder.getFromLocationName(selectedLocation, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val addressLoc = GeoPoint(address.latitude, address.longitude)

                // Tambahkan marker di peta
                val marker = Marker(map)
                marker.position = addressLoc
                marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
                map.overlays.add(marker)
                map.controller.animateTo(addressLoc, 15.0, 2500)

                // Tambahkan lokasi ke daftar (LinearLayout)
                addLocationToList(selectedLocation)

            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchLocationSuggestions(query: String) {
        val geocoder = Geocoder(this)
        var addressList: List<Address>? = null
        try {
            // Cari lokasi berdasarkan input pengguna
            addressList = geocoder.getFromLocationName(query, 10) // Maksimum 10 hasil
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (addressList != null) {
            val suggestions = mutableListOf<String>()
            for (address in addressList) {
                val addressText = address.featureName + ", " + address.locality + ", " + address.countryName
                suggestions.add(addressText)
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
            autoCompleteTextView.setAdapter(adapter)
            autoCompleteTextView.showDropDown()

            autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
                val selectedAddress = addressList[position]
                val addressLoc = GeoPoint(selectedAddress.latitude, selectedAddress.longitude)

                addMarkerToMap(addressLoc, selectedAddress.featureName)
            }
        }
    }

    private fun addMarkerToMap(geoPoint: GeoPoint, title: String?) {
        val marker = Marker(map)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        marker.title = title ?: "Selected Location"
        map.overlays.add(marker)

        map.controller.animateTo(geoPoint, 15.0, 2500)
        map.invalidate() // Refresh peta
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getLastLocation()
                }
                else -> {

                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    showStartMarker(location)
                    moveCameraToMarker(location)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Location is not found. Try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Marker
    private fun showStartMarker(location: Location) {
        val startLocation = GeoPoint(location.latitude, location.longitude)
        val marker = Marker(map)
        marker.position = startLocation

        marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        marker.title = "You're here"

        map.overlays.add(marker)
        map.invalidate()
    }

    private fun moveCameraToMarker(location: Location) {
        val startLocation = GeoPoint(location.latitude, location.longitude)
        mapIcon.setOnClickListener{
            map.controller.animateTo(startLocation, 15.0, 2500)
        }
    }

    private fun addLocationToList(locationName: String) {
        Log.d("nyari", "addLocationToList called with location: $locationName")
        val checkBox = CheckBox(this).apply {
            text = locationName
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Handle when checkbox is checked (optional)
                }
            }
        }
        locationListLayout.addView(checkBox)
        Log.d("coba", "Added checkbox with location: $locationName")
        Log.d("cobacoba", "Current child count: ${locationListLayout.childCount}")
        updateOptimizeButtonState()  // Panggil saat menambahkan
    }

    private fun updateOptimizeButtonState() {
        val checkBoxCount = locationListLayout.childCount

        binding.btnOptimizeRoute.isEnabled = checkBoxCount >= 3
    }

}
