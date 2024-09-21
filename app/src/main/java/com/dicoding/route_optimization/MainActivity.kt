package com.dicoding.route_optimization

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.dicoding.route_optimization.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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

    }
}