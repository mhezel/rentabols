package com.mhez_dev.rentabols_v1.presentation.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    viewModel: ItemDetailsViewModel = koinViewModel()
) {
    val item by viewModel.item.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val ownerProfilePic by viewModel.ownerProfilePic.collectAsState()
    val requestState by viewModel.requestState.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var isStartDateSelection by remember { mutableStateOf(true) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerAmount by remember { mutableStateOf("") }
    
    // Contact seller dialog
    var showContactDialog by remember { mutableStateOf(false) }
    var contactMessage by remember { mutableStateOf("") }
    
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

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text("Make an Offer") },
            text = {
                Column {
                    Text("Current price: ₱${item?.pricePerDay}/day")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = offerAmount,
                        onValueChange = { offerAmount = it },
                        label = { Text("Your offer (per day)") },
                        prefix = { Text("₱") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Handle the offer submission
                        showOfferDialog = false
                    }
                ) {
                    Text("Submit Offer")
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
                                    renterId = "current_user_id", // TODO: Get actual user ID
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
                            contentDescription = "Seller profile picture",
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
                                text = ownerName ?: "Unknown Seller",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "View seller profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { showContactDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Contact Seller"
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
                        text = item?.address ?: "No location specified",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                    text = "Rent it Now",
                    onClick = {
                        // Open contact dialog to proceed with rental
                        showContactDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showOfferDialog = true },
                        modifier = Modifier.weight(1f)
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
