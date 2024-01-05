package com.app.orderfood.util

import android.content.Context
import android.location.Geocoder
import java.util.Locale

class LocationHelper(private val context: Context) {

    fun getCityAndDistrictFromLatLng(latitude: Double, longitude: Double): Pair<String, String>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val cityName = addresses[0].adminArea ?: ""
                val districtName = addresses[0].subAdminArea ?: ""
                return Pair(cityName, districtName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}