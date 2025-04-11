package com.mhez_dev.rentabols_v1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetUserByIdUseCase
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.Date

@Composable
fun RentalItemCard(
    item: RentalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    getUserByIdUseCase: GetUserByIdUseCase = koinInject()
) {
    val ownerFlow = getUserByIdUseCase(item.ownerId)
    val owner by ownerFlow.collectAsState(initial = null)
    
    // Check if item is available
    val isAvailable = remember(item) {
        val availableFrom = item.metadata["availableFrom"] as? Long
        val availableTo = item.metadata["availableTo"] as? Long
        
        when {
            availableFrom == null && availableTo == null -> false // No dates set, item is on hold
            availableFrom != null && System.currentTimeMillis() < availableFrom -> false // Not yet available
            availableTo != null && System.currentTimeMillis() > availableTo -> false // No longer available
            else -> true // Available
        }
    }
    
    // Define colors for status indicators
    val availableColor = Color(0xFF4CAF50) // Bright green
    val onHoldColor = MaterialTheme.colorScheme.error
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(if (isAvailable) 1f else 0.6f) // Reduced opacity for unavailable items
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box {
                AsyncImage(
                    model = item.imageUrls.firstOrNull(),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                
                // Add status badge in the corner of the image
                Surface(
                    color = if (isAvailable) availableColor else onHoldColor,
                    shape = RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Error,
                            contentDescription = if (isAvailable) "Available" else "On Hold",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAvailable) "Available" else "On Hold",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Item title - always display
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "₱${item.pricePerDay}/day",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Owner",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${owner?.name ?: "User"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (item.rating > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", item.rating),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = " (${item.reviewCount})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
