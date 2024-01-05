package com.app.orderfood.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {

    companion object{
        val permissionsForLocations = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    //konum izni iste
     fun requestLocationPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity,
            permissionsForLocations, 1)
    }

    //konum izni var mı kontrol et
    fun hasLocationPermission(context: Context) : Boolean{
        for(permission in permissionsForLocations){
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                )!=PackageManager.PERMISSION_GRANTED
            ){
                return false
            }
        }
        return true
    }

    //gps açık mı
    fun isLocationEnabled(context: Context?): Boolean {
        val locManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    //gps açmaya yönlendir
    fun openGPSSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }






}