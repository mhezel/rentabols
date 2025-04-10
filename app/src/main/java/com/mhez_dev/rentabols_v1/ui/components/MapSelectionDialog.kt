package com.mhez_dev.rentabols_v1.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

@Composable
fun MapSelectionDialog(
    onLocationSelected: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    // Default to Zamboanga City, Philippines
    val zamboanga = LatLng(6.9214, 122.0790)
    
    // Initialize camera position to Zamboanga
    LaunchedEffect(Unit) {
        if (cameraPositionState.position.target.latitude == 0.0) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(zamboanga, 12f)
        }
    }

    // Get current location if permission is granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location: Location? = fusedLocationClient.lastLocation.await()
                location?.let {
                    // Only use the location if it's within the Philippines region
                    if (isWithinPhilippines(it.latitude, it.longitude)) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        currentLocation = latLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    } else {
                        // If outside Philippines, center on Zamboanga
                        currentLocation = zamboanga
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(zamboanga, 12f)
                    }
                }
            } catch (e: Exception) {
                // On error, default to Zamboanga
                currentLocation = zamboanga
                cameraPositionState.position = CameraPosition.fromLatLngZoom(zamboanga, 12f)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        // Only allow selection within Philippines
                        if (isWithinPhilippines(latLng.latitude, latLng.longitude)) {
                            selectedLocation = latLng
                        }
                    },
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        maxZoomPreference = 20f,
                        minZoomPreference = 5f
                    )
                ) {
                    // Always show Zamboanga City marker
                    Marker(
                        state = MarkerState(position = zamboanga),
                        title = "Zamboanga City",
                        snippet = "City Center"
                    )
                    
                    // Show selected location marker if different from Zamboanga
                    selectedLocation?.let { location ->
                        if (location != zamboanga) {
                            Marker(
                                state = MarkerState(position = location),
                                title = "Selected Location"
                            )
                        }
                    }
                }

                // Center on Zamboanga button
                FloatingActionButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(zamboanga, 12f)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Center on Zamboanga"
                    )
                }

                // Bottom buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedLocation?.let {
                                onLocationSelected(it)
                                onDismiss()
                            }
                        },
                        enabled = selectedLocation != null
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}

private fun isWithinPhilippines(latitude: Double, longitude: Double): Boolean {
    // Rough bounding box for the Philippines
    return latitude in 4.0..21.0 && longitude in 116.0..127.0
}
