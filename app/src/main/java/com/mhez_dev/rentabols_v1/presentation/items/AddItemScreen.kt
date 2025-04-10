package com.mhez_dev.rentabols_v1.presentation.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.ui.components.CategoryDropdown
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import com.mhez_dev.rentabols_v1.ui.components.RentabolsTextField
import org.koin.androidx.compose.koinViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.ui.components.MapSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onItemAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isForPickup by remember { mutableStateOf(true) }
    var isForDelivery by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMapDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    
    // Used to show snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle success or error states with a LaunchedEffect
    LaunchedEffect(state) {
        when(state) {
            is AddItemState.Success -> {
                onItemAdded()
            }
            is AddItemState.Error -> {
                val errorMessage = (state as AddItemState.Error).message
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
            }
            else -> { /* No action needed for other states */ }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RentabolsTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title"
            )

            RentabolsTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description"
            )

            RentabolsTextField(
                value = pricePerDay,
                onValueChange = { pricePerDay = it },
                label = "Price per Day"
            )

            CategoryDropdown(
                selectedCategory = category,
                onCategorySelected = { category = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Delivery options
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Availability Options",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isForPickup,
                            onCheckedChange = { isForPickup = it }
                        )
                        Text("Available for pickup")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isForDelivery,
                            onCheckedChange = { isForDelivery = it }
                        )
                        Text("Available for delivery")
                    }
                }
            }

            // Location Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showMapDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedLocation != null) {
                                "Location selected"
                            } else {
                                "Tap to select location"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Location"
                    )
                }
            }

            // Image Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { 
                    if (state !is AddItemState.Loading && state !is AddItemState.Uploading) {
                        imagePicker.launch("image/*") 
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedImages.isEmpty()) {
                            "Add Images (Optional)"
                        } else {
                            "${selectedImages.size} images selected"
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Images"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state is AddItemState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (state as AddItemState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (state is AddItemState.Uploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uploading images: ${(state as AddItemState.Uploading).progress}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = (state as AddItemState.Uploading).progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (state is AddItemState.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Creating item...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            RentabolsButton(
                text = when(state) {
                    is AddItemState.Loading -> "Creating Item..."
                    is AddItemState.Uploading -> "Uploading Images..."
                    else -> "Add Item"
                },
                onClick = {
                    val priceValue = pricePerDay.toDoubleOrNull() ?: 0.0
                    val metadata = mapOf(
                        "isForPickup" to isForPickup,
                        "isForDelivery" to isForDelivery
                    )
                    
                    val location = selectedLocation?.let { 
                        GeoPoint(it.latitude, it.longitude)
                    } ?: GeoPoint(0.0, 0.0)
                    
                    viewModel.createItem(
                        title = title,
                        description = description,
                        category = category,
                        pricePerDay = priceValue,
                        images = selectedImages,
                        location = location,
                        metadata = metadata
                    )
                },
                enabled = title.isNotBlank() && 
                         description.isNotBlank() && 
                         category.isNotBlank() && 
                         pricePerDay.toDoubleOrNull() != null &&
                         pricePerDay.toDoubleOrNull()!! > 0 &&
                         selectedLocation != null &&
                         state !is AddItemState.Loading &&
                         state !is AddItemState.Uploading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showMapDialog) {
        MapSelectionDialog(
            onLocationSelected = { latLng ->
                selectedLocation = latLng
                showMapDialog = false
            },
            onDismiss = { showMapDialog = false }
        )
    }
}
