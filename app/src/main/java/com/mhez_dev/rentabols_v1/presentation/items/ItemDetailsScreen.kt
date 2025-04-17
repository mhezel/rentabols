package com.mhez_dev.rentabols_v1.presentation.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToFullScreenMap: (String) -> Unit = {},
    viewModel: ItemDetailsViewModel = koinViewModel()
) {
    val item by viewModel.item.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val ownerProfilePic by viewModel.ownerProfilePic.collectAsState()
    val requestState by viewModel.requestState.collectAsState()
    
    // Check availability status
    val isAvailable = remember(item) {
        val availableFrom = item?.metadata?.get("availableFrom") as? Long
        val availableTo = item?.metadata?.get("availableTo") as? Long
        
        when {
            availableFrom == null && availableTo == null -> false // No dates set, item is on hold
            availableFrom != null && System.currentTimeMillis() < availableFrom -> false // Not yet available
            availableTo != null && System.currentTimeMillis() > availableTo -> false // No longer available
            else -> true // Available
        }
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var isStartDateSelection by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerAmount by remember { mutableStateOf("") }
    
    // New states for offer dialog
    var offerStartDate by remember { mutableStateOf<Long?>(null) }
    var offerEndDate by remember { mutableStateOf<Long?>(null) }
    var offerDatePickerVisible by remember { mutableStateOf(false) }
    var isOfferStartDateSelection by remember { mutableStateOf(true) }
    var offerDeliveryOption by remember { mutableStateOf<String?>(null) }
    var showOfferSummary by remember { mutableStateOf(false) }
    var offerMessage by remember { mutableStateOf("") }
    
    // Contact seller dialog
    var showContactDialog by remember { mutableStateOf(false) }
    var contactMessage by remember { mutableStateOf("") }
    
    // Add camera position state for the mini-map
    val itemLocation = item?.location?.let { 
        LatLng(it.latitude, it.longitude) 
    } ?: LatLng(6.9214, 122.0790) // Default to Zamboanga if no location
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(itemLocation, 15f)
    }
    
    // Success notification dialog
    val showSuccessDialog = remember { mutableStateOf(false) }
    
    // Error notification dialog
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }
    
    LaunchedEffect(key1 = itemId) {
        viewModel.getItemDetails(itemId)
    }

    // Date picker dialog
    if (showDatePicker) {
        // Initialize datePickerState with a sensible initial value
        val initialDateMillis = when {
            isStartDateSelection -> item?.metadata?.get("availableFrom") as? Long
                ?: System.currentTimeMillis()
            else -> startDate ?: System.currentTimeMillis()
        }
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        
        // Get availability constraints from item metadata
        val availableFrom = item?.metadata?.get("availableFrom") as? Long
        val availableTo = item?.metadata?.get("availableTo") as? Long
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        if (isStartDateSelection) {
                            startDate = selectedDateMillis
                            // If only selecting start date, also move to end date selection
                            isStartDateSelection = false
                            showDatePicker = true // Keep dialog open but switch to end date
                        } else {
                            endDate = selectedDateMillis
                            showDatePicker = false
                        }
                    } else {
                        showDatePicker = false
                    }
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
            DatePicker(
                state = datePickerState,
                dateValidator = { timestamp -> 
                    val start = startDate
                    val fromDate = availableFrom
                    val toDate = availableTo
                    when {
                        isStartDateSelection && fromDate != null -> 
                            timestamp >= fromDate
                        isStartDateSelection && toDate != null -> 
                            timestamp <= toDate
                        !isStartDateSelection && start != null -> 
                            timestamp >= start
                        !isStartDateSelection && toDate != null -> 
                            timestamp <= toDate
                        else -> true
                    }
                }
            )
        }
    }

    if (offerDatePickerVisible) {
        // Initialize datePickerState with a sensible initial value
        val initialDateMillis = when {
            isOfferStartDateSelection -> item?.metadata?.get("availableFrom") as? Long
                ?: System.currentTimeMillis()
            else -> offerStartDate ?: System.currentTimeMillis()
        }
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        
        // Get availability constraints from item metadata
        val availableFrom = item?.metadata?.get("availableFrom") as? Long
        val availableTo = item?.metadata?.get("availableTo") as? Long
        
        DatePickerDialog(
            onDismissRequest = { offerDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        if (isOfferStartDateSelection) {
                            offerStartDate = selectedDateMillis
                            // If only selecting start date, also move to end date selection
                            isOfferStartDateSelection = false
                            offerDatePickerVisible = true // Keep dialog open but switch to end date
                        } else {
                            offerEndDate = selectedDateMillis
                            offerDatePickerVisible = false
                        }
                    } else {
                        offerDatePickerVisible = false
                    }
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { offerDatePickerVisible = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { timestamp -> 
                    val start = offerStartDate
                    val fromDate = availableFrom
                    val toDate = availableTo
                    when {
                        isOfferStartDateSelection && fromDate != null -> 
                            timestamp >= fromDate
                        isOfferStartDateSelection && toDate != null -> 
                            timestamp <= toDate
                        !isOfferStartDateSelection && start != null -> 
                            timestamp >= start
                        !isOfferStartDateSelection && toDate != null -> 
                            timestamp <= toDate
                        else -> true
                    }
                }
            )
        }
    }

    // Success notification dialog
    if (showSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog.value = false },
            icon = { 
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            title = { 
                Text(
                    text = "Success!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    text = "Offer Request Submitted",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog.value = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showErrorDialog.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialog.value = false },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                ) 
            },
            title = { 
                Text(
                    text = "Cannot Submit Offer",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    text = errorMessage.value,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog.value = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Handle state changes
    LaunchedEffect(requestState) {
        when (requestState) {
            is RentalRequestState.Success -> {
                showSuccessDialog.value = true
            }
            is RentalRequestState.Error -> {
                errorMessage.value = (requestState as RentalRequestState.Error).message
                showErrorDialog.value = true
            }
            else -> {}
        }
    }

    // Offer Summary Dialog
    if (showOfferSummary) {
        AlertDialog(
            onDismissRequest = { showOfferSummary = false },
            title = { Text("Offer Summary") },
            text = {
                Column {
                    Text(
                        text = "Review Your Offer",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Item details
                    Text(
                        text = item?.title ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Price details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Your Offer:")
                        Text(
                            "₱${offerAmount}/day",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calculate total days and price
                    if (offerStartDate != null && offerEndDate != null) {
                        val days = ((offerEndDate!! - offerStartDate!!) / (1000 * 60 * 60 * 24)).toInt() + 1
                        val totalPrice = days * (offerAmount.toDoubleOrNull() ?: 0.0)
                        
                        // Rental period
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(
                            text = "Rental Period:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${dateFormat.format(Date(offerStartDate!!))} to ${dateFormat.format(Date(offerEndDate!!))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Total days and price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Rental Days:")
                            Text("$days days")
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Price:")
                            Text(
                                "₱${String.format("%.2f", totalPrice)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Delivery option
                    Text(
                        text = "Preferred Collection Method:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = offerDeliveryOption ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Show message if provided
                    if (offerMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Message:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = offerMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "By submitting this offer, you agree to the terms and conditions of Rentabols.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Handle offer submission logic
                        offerStartDate?.let { start ->
                            offerEndDate?.let { end ->
                                val offerPriceValue = offerAmount.toDoubleOrNull()
                                
                                // Use the same createRentalRequest method to submit the offer
                                viewModel.createRentalRequest(
                                    startDate = start,
                                    endDate = end,
                                    offerPrice = offerPriceValue,
                                    deliveryOption = offerDeliveryOption,
                                    message = offerMessage
                                )
                                showOfferSummary = false
                                showOfferDialog = false
                            }
                        }
                    }
                ) {
                    Text("Submit Offer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Go back to edit the offer
                        showOfferSummary = false
                    }
                ) {
                    Text("Edit")
                }
            }
        )
    }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text("Make an Offer") },
            text = {
                Column {
                    Text("Current price: ₱${item?.pricePerDay}/day")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Offer amount
                    TextField(
                        value = offerAmount,
                        onValueChange = { offerAmount = it },
                        label = { Text("Your offer (per day)") },
                        prefix = { Text("₱") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date selection
                    Text(
                        text = "Select Rental Period",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { 
                                    isOfferStartDateSelection = true
                                    offerDatePickerVisible = true
                                }
                            ) {
                                Text(
                                    offerStartDate?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { 
                                    isOfferStartDateSelection = false
                                    offerDatePickerVisible = true
                                }
                            ) {
                                Text(
                                    offerEndDate?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                    }
                    
                    // Show calculated price if dates are selected
                    if (offerStartDate != null && offerEndDate != null && offerAmount.isNotEmpty()) {
                        val days = ((offerEndDate!! - offerStartDate!!) / (1000 * 60 * 60 * 24)).toInt() + 1
                        val totalPrice = days * (offerAmount.toDoubleOrNull() ?: 0.0)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total for $days days:")
                            Text(
                                "₱${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pickup/delivery selection
                    Text(
                        text = "Collection Method",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Get available methods
                    val isForPickup = item?.metadata?.get("isForPickup") as? Boolean ?: true
                    val isForDelivery = item?.metadata?.get("isForDelivery") as? Boolean ?: false
                    
                    Column {
                        if (isForPickup) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { offerDeliveryOption = "Pickup" }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = offerDeliveryOption == "Pickup",
                                    onClick = { offerDeliveryOption = "Pickup" }
                                )
                                Text("Pickup", Modifier.padding(start = 8.dp))
                            }
                        }
                        
                        if (isForDelivery) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { offerDeliveryOption = "Delivery" }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = offerDeliveryOption == "Delivery",
                                    onClick = { offerDeliveryOption = "Delivery" }
                                )
                                Text("Delivery", Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    
                    // Additional message to seller
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Additional Message to Seller (Optional)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = offerMessage,
                        onValueChange = { offerMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Enter your message to the seller") },
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Proceed to summary screen
                        if (offerAmount.isNotEmpty() && offerStartDate != null && 
                            offerEndDate != null && offerDeliveryOption != null) {
                            showOfferSummary = true
                        }
                    },
                    enabled = offerAmount.isNotEmpty() && offerStartDate != null && 
                             offerEndDate != null && offerDeliveryOption != null
                ) {
                    Text("Review Offer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact ${ownerName ?: "Seller"}") },
            text = {
                Column {
                    Text("Send a rental request to the seller for this item.")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Rental period selection
                    Text(
                        text = "Select Rental Period",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start Date", style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { 
                                    isStartDateSelection = true
                                    showDatePicker = true
                                }
                            ) {
                                Text(
                                    startDate?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("End Date", style = MaterialTheme.typography.bodySmall)
                            TextButton(
                                onClick = { 
                                    isStartDateSelection = false
                                    showDatePicker = true
                                }
                            ) {
                                Text(
                                    endDate?.let { 
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Select Date"
                                )
                            }
                        }
                    }
                    
                    // Calculate total price if dates are selected
                    if (startDate != null && endDate != null) {
                        val days = ((endDate!! - startDate!!) / (1000 * 60 * 60 * 24)).toInt() + 1
                        val totalPrice = days * (item?.pricePerDay ?: 0.0)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total for $days days:")
                            Text(
                                "₱${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Additional message to seller (optional):")
                    OutlinedTextField(
                        value = contactMessage,
                        onValueChange = { contactMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Process rental request
                        startDate?.let { start ->
                            endDate?.let { end ->
                                viewModel.createRentalRequest(
                                    startDate = start,
                                    endDate = end
                                )
                                showContactDialog = false
                            }
                        }
                    },
                    enabled = startDate != null && endDate != null
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add to wishlist functionality */ }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Add to Wishlist"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Carousel (simplified for now)
            AsyncImage(
                model = item?.imageUrls?.firstOrNull(),
                contentDescription = item?.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Item title and price
                Text(
                    text = item?.title ?: "",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₱${item?.pricePerDay}/day",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Seller information section
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            item?.ownerId?.let { ownerId ->
                                onNavigateToUserProfile(ownerId)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Seller profile picture
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(ownerProfilePic)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Lender profile picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = ownerName ?: "Unknown Lender",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "View lender profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { showContactDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Contact Lender"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Availability info - Display dates and delivery/pickup options
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Availability Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Show availability status first
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Error, 
                                contentDescription = null,
                                tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAvailable) "Available" else "On Hold",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Display availability dates if set
                        val availableFrom = item?.metadata?.get("availableFrom") as? Long
                        val availableTo = item?.metadata?.get("availableTo") as? Long
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        
                        if (availableFrom != null || availableTo != null) {
                            Text(
                                text = "Available: ${
                                    when {
                                        availableFrom != null && availableTo != null -> 
                                            "${dateFormat.format(Date(availableFrom))} to ${dateFormat.format(Date(availableTo))}"
                                        availableFrom != null -> 
                                            "From ${dateFormat.format(Date(availableFrom))}"
                                        availableTo != null -> 
                                            "Until ${dateFormat.format(Date(availableTo))}"
                                        else -> "Always available"
                                    }
                                }",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Display pickup/delivery options
                        val isForPickup = item?.metadata?.get("isForPickup") as? Boolean ?: true
                        val isForDelivery = item?.metadata?.get("isForDelivery") as? Boolean ?: false
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isForPickup) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isForPickup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Available for pickup")
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isForDelivery) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isForDelivery) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Available for delivery")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item?.description ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Item Location: ",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = item?.address ?: "No location specified",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Add mini-map
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isMyLocationEnabled = true,
                                maxZoomPreference = 20f,
                                minZoomPreference = 5f
                            ),
                            uiSettings = MapUiSettings(
                                compassEnabled = true,
                                zoomControlsEnabled = true
                            )
                        ) {
                            // Add marker for Zamboanga City center
                            val zamboanga = LatLng(6.9214, 122.0790)
                            
                            // City marker
                            Marker(
                                state = MarkerState(position = zamboanga),
                                title = "Zamboanga City",
                                snippet = "City Center"
                            )
                            
                            // Show item location marker if different from Zamboanga center
                            if (itemLocation != zamboanga) {
                                Marker(
                                    state = MarkerState(position = itemLocation),
                                    title = item?.title ?: "Item Location",
                                    snippet = "Item Location: ${item?.address ?: "No address specified"}"
                                )
                            }
                        }
                        
                        // Add a button to open full map
                        FloatingActionButton(
                            onClick = {
                                onNavigateToFullScreenMap(itemId)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OpenInFull,
                                contentDescription = "Open in full map"
                            )
                        }
                        
                        // Add a button to center on item location
                        FloatingActionButton(
                            onClick = {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                    itemLocation,
                                    15f
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Center on Item Location"
                            )
                        }
                    }
                }

                if (item?.rating != null && item?.rating!! > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1f", item?.rating),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = " (${item?.reviewCount} reviews)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                RentabolsButton(
                    text = if (isAvailable) "Rent it Now" else "Not Available",
                    onClick = {
                        // Open contact dialog to proceed with rental
                        if (isAvailable) {
                            showContactDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAvailable
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showOfferDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = isAvailable
                    ) {
                        Text("Make Offer")
                    }
                    
                    OutlinedButton(
                        onClick = { /* TODO: Add to wishlist functionality */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wishlist")
                        }
                    }
                }

                // Show availability explanation if not available
                if (!isAvailable) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val availableFrom = item?.metadata?.get("availableFrom") as? Long
                        val availableTo = item?.metadata?.get("availableTo") as? Long
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        
                        val message = when {
                            availableFrom == null && availableTo == null -> 
                                "This item is currently on hold. Rental dates have not been set by the owner."
                            availableFrom != null && System.currentTimeMillis() < availableFrom ->
                                "This item will be available from ${dateFormat.format(Date(availableFrom))}."
                            availableTo != null && System.currentTimeMillis() > availableTo ->
                                "This item is no longer available as of ${dateFormat.format(Date(availableTo))}."
                            else -> "This item is not available for rental at this time."
                        }
                        
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                when (requestState) {
                    is RentalRequestState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Rental request sent successfully!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    is RentalRequestState.Error -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (requestState as RentalRequestState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    return SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
}
