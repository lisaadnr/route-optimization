package com.dicoding.route_optimization

import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dicoding.route_optimization.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var map: MapView

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

        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val mapController = map.controller
        mapController.setZoom(12.5)
        val startPoint = GeoPoint(-6.21462, 106.84513)
        mapController.setCenter(startPoint)

        // marker
        val marker = Marker(map)
        marker.position = startPoint

        marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        marker.title = "You're here"

        map.overlays.add(marker)
        map.invalidate()
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
