package com.spmadrid.vrepo.domain.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spmadrid.vrepo.domain.interfaces.ILocationManager


class LocationManagerService(
    private val context: Context,
    private val locationManager: LocationManager
) : ILocationManager {

//    val activity = LocalContext.current as Activity

    override suspend fun getLocation() {

    }

//    fun checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
//        } else {
//
//        }
//    }
}