package com.mhez_dev.rentabols_v1.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import android.util.Log
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.core.content.ContextCompat
import com.mhez_dev.rentabols_v1.utils.MapUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenMapScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    // Simple state management to avoid complexity
    var itemPosition by remember { mutableStateOf<LatLng?>(null) }
    var itemTitle by remember { mutableStateOf<String?>(null) }
    var itemCategory by remember { mutableStateOf<String?>(null) }
    var isGettingLocation by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Initialize camera position with default position from MapUtils
    val defaultPosition = LatLng(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE)
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, MapUtils.DEFAULT_ZOOM_LEVEL)
    }
    
    // Function to get current location
    val getUserLocation = {
        isGettingLocation = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userPosition = LatLng(location.latitude, location.longitude)
                    
                    // Check if location is within Philippines
                    if (MapUtils.isWithinPhilippines(location.latitude, location.longitude)) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            userPosition, 
                            MapUtils.DEFAULT_ZOOM_LEVEL
                        )
                    } else {
                        // If not in Philippines, default to the map's item position
                        // or fall back to the default position
                        itemPosition?.let {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                it,
                                MapUtils.DEFAULT_ZOOM_LEVEL
                            )
                        } ?: run {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                defaultPosition,
                                MapUtils.DEFAULT_ZOOM_LEVEL
                            )
                        }
                    }
                }
                isGettingLocation = false
            }.addOnFailureListener {
                Log.e("FullScreenMap", "Error getting location: ${it.message}", it)
                isGettingLocation = false
            }
        } else {
            isGettingLocation = false
        }
    }
    
    // Load the specific item and center the map on it
    LaunchedEffect(itemId) {
        try {
            viewModel.getItemById(itemId)
        } catch (e: Exception) {
            Log.e("FullScreenMap", "Error loading item: ${e.message}", e)
        }
    }
    
    // Observe singleItemState for item details
    val singleItemState by viewModel.singleItemState.collectAsState()
    
    // Update UI when item is loaded
    LaunchedEffect(singleItemState.item) {
        singleItemState.item?.let { item ->
            try {
                val position = LatLng(item.location.latitude, item.location.longitude)
                itemPosition = position
                itemTitle = item.title
                itemCategory = item.category
                
                // Center the map on the item's location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    position, 
                    MapUtils.DEFAULT_ZOOM_LEVEL
                )
            } catch (e: Exception) {
                Log.e("FullScreenMap", "Error updating map position: ${e.message}", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(itemTitle ?: "Item Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Button to return to item location
                    IconButton(
                        onClick = {
                            itemPosition?.let { position ->
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    position, 
                                    MapUtils.DEFAULT_ZOOM_LEVEL
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Center on Item"
                        )
                    }
                }
            )
        },
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
                    isMyLocationEnabled = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED,
                    maxZoomPreference = 20f,
                    minZoomPreference = 5f
                ),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Simple marker for the main item
                itemPosition?.let { position ->
                    // Main item marker with different color
                    Marker(
                        state = MarkerState(position = position),
                        title = itemTitle,
                        snippet = "₱${singleItemState.item?.pricePerDay}/day • ${itemCategory}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            MapUtils.getCategoryColor(itemCategory ?: "Other")
                        )
                    )
                }
                
                // Show nearby items as simple markers with category-based colors
                singleItemState.nearbyItems.take(10).forEach { item ->
                    if (item.id != itemId) {
                        val nearbyPosition = LatLng(
                            item.location.latitude,
                            item.location.longitude
                        )
                        
                        val distanceText = if (itemPosition != null) {
                            val distance = MapUtils.calculateDistance(
                                itemPosition!!.latitude, itemPosition!!.longitude,
                                nearbyPosition.latitude, nearbyPosition.longitude
                            )
                            String.format("%.1f km away", distance)
                        } else {
                            ""
                        }
                        
                        Marker(
                            state = MarkerState(position = nearbyPosition),
                            title = item.title,
                            snippet = "₱${item.pricePerDay}/day • ${item.category} • $distanceText",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                MapUtils.getCategoryColor(item.category)
                            )
                        )
                    }
                }
            }

            // Add the floating action button inside the Box at the top-right position - now FIRST at top
            FloatingActionButton(
                onClick = { getUserLocation() },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 16.dp)
                    .size(48.dp)
            ) {
                if (isGettingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Show loading indicator when searching
            if (singleItemState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
            
            // Map Legend for marker colors - now BELOW the location button
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 8.dp)
                    .shadow(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "Legend",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Current item indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = Color(MapUtils.getCategoryColorInt(itemCategory ?: "Other")),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Current Item",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    // Other common categories
                    val commonCategories = listOf("Electronics", "Tools", "Furniture", "Vehicles", "Other")
                    commonCategories.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color(MapUtils.getCategoryColorInt(category)),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Show item details card at the bottom with extra padding to avoid map controls
            singleItemState.item?.let { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 72.dp, top = 16.dp) // Increased bottom padding
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₱${item.pricePerDay}/day • ${item.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        item.description?.let { description ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (description.length > 100) description.take(100) + "..." else description,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Show error message if any
            singleItemState.error?.let { error ->
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
        }
    }
} 