package com.mhez_dev.rentabols_v1.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mhez_dev.rentabols_v1.ui.components.RentabolsBottomNavigation
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToItemDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToProfile: () -> Unit,
    currentRoute: String,
    viewModel: MapViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val singapore = LatLng(1.3521, 103.8198) // Default center on Singapore
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }

    LaunchedEffect(cameraPositionState.position) {
        val center = cameraPositionState.position.target
        viewModel.searchNearbyItems(
            location = GeoPoint(center.latitude, center.longitude),
            radiusInKm = 10.0
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map View") }
            )
        },
        bottomBar = {
            RentabolsBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "items" -> onNavigateToItems()
                        "map" -> { /* Already on map */ }
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true)
        ) {
            state.items.forEach { item ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            item.location.latitude,
                            item.location.longitude
                        )
                    ),
                    title = item.title,
                    snippet = "₱${item.pricePerDay}/day",
                    onClick = {
                        onNavigateToItemDetails(item.id)
                        true
                    }
                )
            }
        }
    }
}
