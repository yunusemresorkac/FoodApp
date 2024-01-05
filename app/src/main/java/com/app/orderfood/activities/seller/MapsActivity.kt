package com.app.orderfood.activities.seller

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding : ActivityMapsBinding
    private var googleMap: GoogleMap? = null
    private var latitude : Double? = 0.0
    private var longitude : Double? = 0.0
    private lateinit var mapTitle : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapTitle = intent.getStringExtra("mapTitle")!!
        latitude = intent.getDoubleExtra("restaurantLatitude",0.0)
        longitude = intent.getDoubleExtra("restaurantLongitude",0.0)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        //restoran konumunu haritada göster
        showCurrentLocation()
    }


    private fun showCurrentLocation() {
        if (googleMap != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
            googleMap?.isMyLocationEnabled = true

            // Mevcut konumu al

            val currentLocation = LatLng(latitude!!, longitude!!)

            // Marker ekle
            googleMap?.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title(mapTitle)

            )

            // Kamerayı mevcut konuma hareket ettir
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11f))
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        showCurrentLocation()
    }
}