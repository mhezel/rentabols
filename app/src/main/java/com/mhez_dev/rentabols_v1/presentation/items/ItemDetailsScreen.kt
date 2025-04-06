package com.mhez_dev.rentabols_v1.presentation.items

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mhez_dev.rentabols_v1.ui.components.RentabolsButton
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    viewModel: ItemDetailsViewModel = koinViewModel()
) {
    val item by viewModel.item.collectAsState()
    val requestState by viewModel.requestState.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        text = item?.address ?: "",
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

                // Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start Date")
                        TextButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Text(
                                startDate?.let { 
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                } ?: "Select Date"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End Date")
                        TextButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Text(
                                endDate?.let { 
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
                                } ?: "Select Date"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                RentabolsButton(
                    text = "Request to Rent",
                    onClick = {
                        // TODO: Get current user ID
                        startDate?.let { start ->
                            endDate?.let { end ->
                                viewModel.createRentalRequest(
                                    renterId = "current_user_id",
                                    startDate = start,
                                    endDate = end
                                )
                            }
                        }
                    },
                    enabled = startDate != null && endDate != null && 
                            requestState !is RentalRequestState.Loading
                )

                when (requestState) {
                    is RentalRequestState.Success -> {
                        Snackbar(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Rental request sent successfully!")
                        }
                    }
                    is RentalRequestState.Error -> {
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
