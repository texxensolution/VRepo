package com.spmadrid.vrepo.presentation.components

import com.spmadrid.vrepo.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.spmadrid.vrepo.domain.services.LocationManagerService
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.spmadrid.vrepo.presentation.ui.theme.Gray600
import com.spmadrid.vrepo.presentation.ui.theme.Gray800
import com.spmadrid.vrepo.presentation.ui.theme.Gray900

@Composable
fun OpenStreetMapView(locationManagerService: LocationManagerService) {
    val context = LocalContext.current
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var currentMapZoom: Double by remember { mutableDoubleStateOf(17.0) }
    var hasCenteredOnce by remember { mutableStateOf(false) }

    // Launch to get the location every 5 secondsCurren
    LaunchedEffect(Unit) {
        locationManagerService.observeLocationUpdates().collect { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            currentLocation = geoPoint
            Log.d("LIVE_LOCATION", "Lat: ${location.latitude}, Lon: ${location.longitude}, Acc: ${location.accuracy}")
        }
    }

    // Use rememberUpdatedState to ensure currentLocation is always up-to-date in the update block
    val updatedLocation by rememberUpdatedState(currentLocation)
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Use AndroidView to show OpenStreetMap
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
                        arrayOf(
                            "https://a.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                            "https://b.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                            "https://c.basemaps.cartocdn.com/rastertiles/voyager_nolabels/",
                            "https://d.basemaps.cartocdn.com/rastertiles/voyager_nolabels/"
                        )
                    )

                    setTileSource(voyagerDark)
                    setMultiTouchControls(true)

                    // set the zoom
                    controller.setZoom(currentMapZoom)
                }
            },
            update = { mapView ->
                val mapController = mapView.controller

                currentMapZoom = mapView.zoomLevelDouble

                mapView.overlays.clear() // Clear old overlays to prevent multiple markers

                // Only update if updatedLocation is not null
                updatedLocation?.let { location ->

                    if (!hasCenteredOnce) {
                        mapController.setZoom(currentMapZoom)
                        mapController.setCenter(location)
                        hasCenteredOnce = true
                    }

                    // Add or update marker at the current location
                    val marker = addCircularMarker(context, mapView, location)
                    marker.title = "Your Location"
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Overlay text on top of the map
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(1f)
                .padding(bottom = 105.dp, start = 24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "Version: ${context.getString(R.string.app_version)}",
                color = Gray800.copy(alpha = 0.50f),
                fontSize = 12.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Gray800.copy(alpha = 0.3f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
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
    val bitmap = createBitmap(size, size)
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

    return bitmap.toDrawable(context.resources)
}