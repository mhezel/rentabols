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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.ui.components.CategoryDropdown
import com.mhez_dev.rentabols_v1.ui.components.MapSelectionDialog
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import com.mhez_dev.rentabols_v1.ui.components.RentabolsTextField
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

sealed class EditItemState {
    object Initial : EditItemState()
    object Loading : EditItemState()
    data class Uploading(val progress: Int) : EditItemState()
    object Success : EditItemState()
    data class Error(val message: String) : EditItemState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: String,
    onItemUpdated: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailsViewModel = koinViewModel()
) {
    val item by viewModel.item.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isForPickup by remember { mutableStateOf(true) }
    var isForDelivery by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showMapDialog by remember { mutableStateOf(false) }
    
    // Availability dates
    var availableFrom by remember { mutableStateOf<Long?>(null) }
    var availableTo by remember { mutableStateOf<Long?>(null) }
    
    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    // Set initial values once we have the item
    LaunchedEffect(item) {
        item?.let {
            title = it.title
            description = it.description
            pricePerDay = it.pricePerDay.toString()
            category = it.category
            // Get availability dates from metadata
            availableFrom = it.metadata["availableFrom"] as? Long
            availableTo = it.metadata["availableTo"] as? Long
            // Availability options
            isForPickup = it.metadata["isForPickup"] as? Boolean ?: true
            isForDelivery = it.metadata["isForDelivery"] as? Boolean ?: false
            // Set location
            selectedLocation = LatLng(it.location.latitude, it.location.longitude)
        }
    }
    
    // Fetch the item details
    LaunchedEffect(itemId) {
        viewModel.getItemDetails(itemId)
    }
    
    // Used to show snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for the edit operation
    var editState by remember { mutableStateOf<EditItemState>(EditItemState.Initial) }
    
    // Handle edit state changes
    LaunchedEffect(editState) {
        when(editState) {
            is EditItemState.Success -> {
                onItemUpdated()
            }
            is EditItemState.Error -> {
                val errorMessage = (editState as EditItemState.Error).message
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
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        if (isSelectingStartDate) {
                            availableFrom = selectedDateMillis
                        } else {
                            availableTo = selectedDateMillis
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
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
            
            // Availability Dates
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Availability Period",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available From")
                            TextButton(
                                onClick = { 
                                    isSelectingStartDate = true
                                    showDatePicker = true 
                                }
                            ) {
                                Text(
                                    availableFrom?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available Until")
                            TextButton(
                                onClick = { 
                                    isSelectingStartDate = false
                                    showDatePicker = true 
                                }
                            ) {
                                Text(
                                    availableTo?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                    }
                }
            }
            
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
                    if (editState !is EditItemState.Loading && editState !is EditItemState.Uploading) {
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
                            if (item?.imageUrls?.isNotEmpty() == true) {
                                "${item?.imageUrls?.size} existing images"
                            } else {
                                "Add Images (Optional)"
                            }
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

            if (editState is EditItemState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (editState as EditItemState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (editState is EditItemState.Uploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uploading images: ${(editState as EditItemState.Uploading).progress}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = (editState as EditItemState.Uploading).progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (editState is EditItemState.Loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Updating item...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            RentabolsButton(
                text = when(editState) {
                    is EditItemState.Loading -> "Updating Item..."
                    is EditItemState.Uploading -> "Uploading Images..."
                    else -> "Update Item"
                },
                onClick = {
                    item?.let {
                        val priceValue = pricePerDay.toDoubleOrNull() ?: it.pricePerDay
                        val metadata = mapOf<String, Any>(
                            "isForPickup" to isForPickup,
                            "isForDelivery" to isForDelivery,
                            "availableFrom" to (availableFrom ?: 0L),
                            "availableTo" to (availableTo ?: 0L)
                        ).filterValues { value -> 
                            // Filter out zero timestamp values to handle optional dates
                            if (value is Long) {
                                return@filterValues value != 0L
                            }
                            true
                        }
                        
                        val location = selectedLocation?.let { latLng ->
                            GeoPoint(latLng.latitude, latLng.longitude)
                        } ?: it.location
                        
                        val updatedItem = it.copy(
                            title = title,
                            description = description,
                            category = category,
                            pricePerDay = priceValue,
                            location = location,
                            metadata = metadata
                        )
                        
                        editState = EditItemState.Loading
                        
                        // Update item first
                        viewModel.updateItem(updatedItem) { result ->
                            if (result.isSuccess) {
                                if (selectedImages.isNotEmpty()) {
                                    // If there are new images, upload them
                                    editState = EditItemState.Uploading(0)
                                    
                                    viewModel.uploadItemImages(
                                        itemId = updatedItem.id,
                                        images = selectedImages,
                                        onProgress = { progress ->
                                            editState = EditItemState.Uploading(progress)
                                        },
                                        onComplete = { uploadResult ->
                                            if (uploadResult.isSuccess) {
                                                editState = EditItemState.Success
                                            } else {
                                                editState = EditItemState.Error(
                                                    uploadResult.exceptionOrNull()?.message 
                                                        ?: "Failed to upload images"
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    // No new images, we're done
                                    editState = EditItemState.Success
                                }
                            } else {
                                // Update failed
                                editState = EditItemState.Error(
                                    result.exceptionOrNull()?.message 
                                        ?: "Failed to update item"
                                )
                            }
                        }
                    }
                },
                enabled = item != null && 
                         title.isNotBlank() && 
                         description.isNotBlank() && 
                         category.isNotBlank() && 
                         pricePerDay.toDoubleOrNull() != null &&
                         pricePerDay.toDoubleOrNull()!! > 0 &&
                         selectedLocation != null &&
                         editState !is EditItemState.Loading &&
                         editState !is EditItemState.Uploading,
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