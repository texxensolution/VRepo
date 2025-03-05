package com.spmadrid.vrepo.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.spmadrid.vrepo.domain.services.LocationManagerService
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OpenStreetMapView(locationManagerService: LocationManagerService) {
    val context = LocalContext.current
    var currentLocation by remember {
        mutableStateOf<GeoPoint?>(
            null
        )
    }

    val currentLocationState by rememberUpdatedState(currentLocation) // Ensure the latest value is used

    LaunchedEffect(Unit) {
        while (true) {
            val location = locationManagerService.getCurrentLocation()
            if (location != null) {
                Log.d("Current Location", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                currentLocation = GeoPoint(location.latitude, location.longitude)
            }
            delay(5000L)
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val voyagerDark = XYTileSource(
                    "CartoVoyager",
                    1, 20, 256, ".png",
                    arrayOf("https://a.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                        "https://b.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                        "https://c.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                        "https://d.basemaps.cartocdn.com/rastertiles/voyager_nolabels/")
                )

                setTileSource(voyagerDark)
                setMultiTouchControls(true)
            }
        },
        update = { mapView ->
                val mapController = mapView.controller

                mapView.overlays.clear()

                if (currentLocation != null) {
                    val startPoint = currentLocation // Example: Manila
                    mapController.setZoom(21.0)
                    mapController.setCenter(startPoint)

                    val marker = addCircularMarker(context, mapView, currentLocation!!)
                    marker.title = "Your Location"
                }
                 },
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    )
}

fun addCircularMarker(context: Context, mapView: MapView, geoPoint: GeoPoint): Marker {
    val marker = Marker(mapView)
    marker.position = geoPoint
    marker.icon = getCircularDrawable(context, 50) // Change color & size
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER) // Center it
    mapView.overlays.add(marker)
    mapView.invalidate()
    return marker
}

fun getCircularDrawable(context: Context, size: Int): BitmapDrawable {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val borderWidth = 8.0f

    val radius = size / 2f
    val paintFill = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val paintBorder = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = borderWidth
    }

    // Draw filled circle
    canvas.drawCircle(radius, radius, radius - borderWidth / 2, paintFill)

    // Draw border circle
    canvas.drawCircle(radius, radius, radius - borderWidth / 2, paintBorder)

    return BitmapDrawable(context.resources, bitmap)
}