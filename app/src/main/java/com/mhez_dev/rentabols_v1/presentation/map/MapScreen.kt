package com.mhez_dev.rentabols_v1.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.ui.components.RentabolsBottomNavigation
import com.mhez_dev.rentabols_v1.utils.MapUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToItemDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToProfile: () -> Unit,
    currentRoute: String,
    viewModel: MapViewModel = koinViewModel(),
    getCurrentUserUseCase: GetCurrentUserUseCase = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Initialize camera position with default Zamboanga location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapUtils.ZAMBOANGA_CITY, MapUtils.DEFAULT_ZOOM)
    }
    
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Initialize map with device location or fall back to Zamboanga
    LaunchedEffect(Unit) {
        // Try to get the device's current location
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                
                location?.let {
                    // Only use the location if it's within the Philippines region
                    if (MapUtils.isWithinPhilippines(it.latitude, it.longitude)) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userLocation = latLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, MapUtils.DETAIL_ZOOM)
                        
                        // Search for items near user's location
                        viewModel.searchNearbyItems(
                            location = GeoPoint(latLng.latitude, latLng.longitude),
                            radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                        )
                    } else {
                        // If outside Philippines, center on Zamboanga
                        userLocation = MapUtils.ZAMBOANGA_CITY
                        viewModel.searchNearbyItems(
                            location = GeoPoint(MapUtils.ZAMBOANGA_CITY.latitude, MapUtils.ZAMBOANGA_CITY.longitude),
                            radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                        )
                    }
                    isLoading = false
                } ?: run {
                    // If location is null, use Zamboanga
                    userLocation = MapUtils.ZAMBOANGA_CITY
                    viewModel.searchNearbyItems(
                        location = GeoPoint(MapUtils.ZAMBOANGA_CITY.latitude, MapUtils.ZAMBOANGA_CITY.longitude),
                        radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                    )
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("MapScreen", "Error getting location", e)
                // On error, default to Zamboanga
                userLocation = MapUtils.ZAMBOANGA_CITY
                viewModel.searchNearbyItems(
                    location = GeoPoint(MapUtils.ZAMBOANGA_CITY.latitude, MapUtils.ZAMBOANGA_CITY.longitude),
                    radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                )
                isLoading = false
            }
        } else {
            // No location permission, default to Zamboanga
            userLocation = MapUtils.ZAMBOANGA_CITY
            viewModel.searchNearbyItems(
                location = GeoPoint(MapUtils.ZAMBOANGA_CITY.latitude, MapUtils.ZAMBOANGA_CITY.longitude),
                radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
            )
            isLoading = false
        }
        
        // Also try to get the current user's saved location from profile
        getCurrentUserUseCase()
            .onEach { user ->
                user?.location?.let { locationStr ->
                    if (locationStr.isNotBlank() && userLocation == MapUtils.ZAMBOANGA_CITY) {
                        // If we're currently using the default location, try to use the profile location
                        MapUtils.getCoordinatesFromAddress(locationStr, context)?.let { coordinates ->
                            if (MapUtils.isWithinPhilippines(coordinates.latitude, coordinates.longitude)) {
                                userLocation = coordinates
                                // Only update camera if we're still at the default location
                                if (cameraPositionState.position.target == MapUtils.ZAMBOANGA_CITY) {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(coordinates, MapUtils.DETAIL_ZOOM)
                                    viewModel.searchNearbyItems(
                                        location = GeoPoint(coordinates.latitude, coordinates.longitude),
                                        radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                                    )
                                }
                            }
                        }
                    }
                }
            }
            .launchIn(scope)
    }
    
    // Listen for camera position changes to update search
    LaunchedEffect(cameraPositionState.position) {
        if (!isLoading) { // Only search when initial loading is complete
            // Avoid too many updates - only update if camera has moved significantly
            val center = cameraPositionState.position.target
            // Only search if we've moved significantly
            val lastSearch = viewModel.lastSearchLocation
            if (lastSearch == null || 
                MapUtils.calculateDistance(
                    lastSearch.latitude,
                    lastSearch.longitude,
                    center.latitude,
                    center.longitude
                ) > MapUtils.MIN_DISTANCE_FOR_SEARCH) {
                viewModel.searchNearbyItems(
                    location = GeoPoint(center.latitude, center.longitude),
                    radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                )
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Items") },
                actions = {
                    // My Location button
                    IconButton(
                        onClick = {
                            userLocation?.let { location ->
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, MapUtils.DETAIL_ZOOM)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location"
                        )
                    }
                    
                    // Button to return to Zamboanga City center
                    IconButton(
                        onClick = {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(MapUtils.ZAMBOANGA_CITY, MapUtils.DEFAULT_ZOOM)
                            viewModel.searchNearbyItems(
                                location = GeoPoint(MapUtils.ZAMBOANGA_CITY.latitude, MapUtils.ZAMBOANGA_CITY.longitude),
                                radiusInKm = MapUtils.DEFAULT_SEARCH_RADIUS
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Center on Zamboanga"
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToHome() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    maxZoomPreference = 20f,
                    minZoomPreference = 5f,
                    // Ensure map is centered on the Philippines region
                    latLngBoundsForCameraTarget = MapUtils.PHILIPPINES_BOUNDS
                ),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    zoomControlsEnabled = true,
                    // Disable the default My Location button since we handle it manually
                    myLocationButtonEnabled = false
                )
            ) {
                // Add a marker for Zamboanga City center with blue marker
                MarkerInfoWindowContent(
                    state = MarkerState(position = MapUtils.ZAMBOANGA_CITY),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                    title = "Zamboanga City"
                ) {
                    // Custom info window for city center
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Zamboanga City Center",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Default location marker",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Show rental items with colored markers and custom info windows
                state.items.forEach { item ->
                    val itemPosition = LatLng(
                        item.location.latitude,
                        item.location.longitude
                    )
                    
                    // Select marker color based on category
                    val markerColor = MapUtils.getCategoryMarkerColor(item.category)
                    
                    MarkerInfoWindowContent(
                        state = MarkerState(position = itemPosition),
                        icon = BitmapDescriptorFactory.defaultMarker(markerColor),
                        title = item.title,
                        snippet = item.category,
                        onClick = {
                            onNavigateToItemDetails(item.id)
                            false // Keep info window open when clicked
                        }
                    ) {
                        // Custom info window
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₱${item.pricePerDay}/day • ${item.category}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Availability section
                                Spacer(modifier = Modifier.height(4.dp))
                                if (item.metadata["isForPickup"] == true) {
                                    Text(
                                        text = "✓ Available for pickup",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (item.metadata["isForDelivery"] == true) {
                                    Text(
                                        text = "✓ Available for delivery",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                // Date availability
                                item.metadata["availableFrom"]?.let { from ->
                                    item.metadata["availableTo"]?.let { to ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Available: ${MapUtils.formatDate(from as Long)} - ${MapUtils.formatDate(to as Long)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                
                                // Click to view details text
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to view full details",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Show loading indicator when searching
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }

            // Show error message if any
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Legend card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .width(IntrinsicSize.Max),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Map Legend:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "🔵 City Center",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟣 Electronics",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟠 Furniture",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟡 Tools",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🔷 Vehicles",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟤 Clothing",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟢 Books",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🟪 Other Items",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Temporary replacement for rememberCoroutineScope until we fix dependency issues
@Composable
fun rememberCoroutineScope(): kotlinx.coroutines.CoroutineScope {
    return remember { kotlinx.coroutines.MainScope() }
}
