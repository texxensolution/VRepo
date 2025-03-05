package com.spmadrid.vrepo.domain.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.spmadrid.vrepo.domain.repositories.LocationRepository
import javax.inject.Inject


class LocationManagerService @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend fun getCurrentLocation(): Location? {
        return locationRepository.getCurrentLocation()
    }
}