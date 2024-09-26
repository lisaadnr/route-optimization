package com.dicoding.route_optimization

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.ImageView
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mapIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reference to AutoCompleteTextView from the layout
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.search_autocomplete)

        // Adapter to set data into AutoCompleteTextView (contoh pake array disini)
//        val adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_dropdown_item_1line, // layout for each item in suggestions
//            addresses
//        )

        // Set adapter to AutoCompleteTextView
//        autoCompleteTextView.setAdapter(adapter)

        // Optional: Set how many characters needed before suggestions appear
        autoCompleteTextView.threshold = 1  // Suggest after 1 character is typed

        // konfigurasi map
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
            searchLocation()
        }
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

    private fun searchLocation(){
        val locationSearch = binding.searchAutocomplete
        var location: String = locationSearch.text.toString().trim()
        var addressList: List<Address>? = null

        if (location != null) {
            val geoCoder = Geocoder(this)
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val address = addressList!![0]
            val addressLoc = GeoPoint(address.latitude, address.longitude)

            val addressMarker = Marker(map)
            addressMarker.position = addressLoc
            addressMarker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)

            map.overlays.add(addressMarker)
            map.controller.animateTo(addressLoc, 15.0, 2500)
        }
    }
}
