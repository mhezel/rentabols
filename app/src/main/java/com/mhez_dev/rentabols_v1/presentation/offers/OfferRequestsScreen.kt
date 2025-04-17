package com.mhez_dev.rentabols_v1.presentation.offers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.ui.components.RentabolsBottomNavigation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRequestsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    currentRoute: String,
    viewModel: OfferRequestsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        "map" -> onNavigateToMap()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                state.offerRequests.isEmpty() -> {
                    Text(
                        text = "You haven't received any offer requests yet.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    OfferRequestsList(
                        offerRequests = state.offerRequests,
                        onAcceptOffer = { transactionId -> viewModel.acceptOffer(transactionId) },
                        onRejectOffer = { transactionId -> viewModel.rejectOffer(transactionId) }
                    )
                }
            }
        }
    }
}

@Composable
fun OfferRequestsList(
    offerRequests: List<OfferRequestWithItem>,
    onAcceptOffer: (String) -> Unit,
    onRejectOffer: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(offerRequests) { offerRequest ->
            OfferRequestItem(
                offerRequest = offerRequest,
                onAcceptOffer = onAcceptOffer,
                onRejectOffer = onRejectOffer
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OfferRequestItem(
    offerRequest: OfferRequestWithItem,
    onAcceptOffer: (String) -> Unit,
    onRejectOffer: (String) -> Unit
) {
    val transaction = offerRequest.transaction
    val item = offerRequest.item
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Transaction ID (smaller and less prominent)
            Text(
                text = "ID: ${transaction.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Item title
            item?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Renter name
            val renterName = transaction.metadata["renterName"] as? String ?: "Unknown User"
            Text(
                text = "From: $renterName",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rental dates
            Text(
                text = "Rental period: ${formatDate(transaction.startDate)} - ${formatDate(transaction.endDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Price
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total price: $${transaction.totalPrice}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Show if it's an offer with custom price
            if (transaction.metadata.containsKey("isOffer")) {
                val offerPrice = transaction.metadata["offerPricePerDay"] as? Double
                val standardPrice = transaction.metadata["standardPricePerDay"] as? Double
                
                if (offerPrice != null && standardPrice != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Offer price: $${offerPrice}/day (Standard: $${standardPrice}/day)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Show delivery option if present
            val deliveryOption = transaction.metadata["deliveryOption"] as? String
            if (deliveryOption != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Collection method: $deliveryOption",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Show message if present
            val message = transaction.metadata["message"] as? String
            if (message != null && message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Message: \"$message\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            // Status
            Spacer(modifier = Modifier.height(8.dp))
            StatusBadge(status = transaction.status)
            
            // Created date
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Requested on: ${formatDate(transaction.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            // Action buttons if pending
            if (transaction.status == RentalStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onRejectOffer(transaction.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Reject",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedButton(
                        onClick = { onAcceptOffer(transaction.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = "Accept",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RentalStatus) {
    val statusColor = when (status) {
        RentalStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        RentalStatus.APPROVED -> MaterialTheme.colorScheme.primary
        RentalStatus.REJECTED -> MaterialTheme.colorScheme.error
        RentalStatus.COMPLETED -> Color(0xFF4CAF50) // Green
        RentalStatus.CANCELLED -> Color(0xFFFF9800) // Orange
        RentalStatus.IN_PROGRESS -> Color(0xFF2196F3) // Blue
    }
    
    val statusText = when (status) {
        RentalStatus.PENDING -> "Pending"
        RentalStatus.APPROVED -> "Approved"
        RentalStatus.REJECTED -> "Rejected"
        RentalStatus.COMPLETED -> "Completed"
        RentalStatus.CANCELLED -> "Cancelled"
        RentalStatus.IN_PROGRESS -> "In Progress"
    }
    
    Surface(
        color = statusColor.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            color = statusColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(date)
} 