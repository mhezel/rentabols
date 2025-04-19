package com.mhez_dev.rentabols_v1.presentation.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mhez_dev.rentabols_v1.domain.model.PaymentMethod
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    transactionId: String,
    onPaymentComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_PICKUP) }
    
    LaunchedEffect(transactionId) {
        viewModel.getTransactionDetails(transactionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Payment Method") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Transaction info card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Transaction Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Item:")
                                Text(
                                    text = state.itemName ?: "Unknown Item", 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Amount:")
                                Text(
                                    text = "₱${String.format("%.2f", state.transaction?.totalPrice ?: 0.0)}", 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Delivery Option:")
                                Text(
                                    text = state.transaction?.deliveryOption ?: "Pickup", 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Select Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Payment options
                    PaymentOption(
                        title = "Cash on Pickup",
                        description = "Pay with cash when you pickup the item",
                        icon = Icons.Default.Payments,
                        selected = selectedPaymentMethod == PaymentMethod.CASH_ON_PICKUP,
                        onClick = { selectedPaymentMethod = PaymentMethod.CASH_ON_PICKUP }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    PaymentOption(
                        title = "E-Wallet",
                        description = "Pay using GCash, PayMaya, or other e-wallets",
                        icon = Icons.Default.AccountBalance,
                        selected = selectedPaymentMethod == PaymentMethod.E_WALLET,
                        onClick = { selectedPaymentMethod = PaymentMethod.E_WALLET }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    PaymentOption(
                        title = "Credit Card",
                        description = "Pay using your credit card",
                        icon = Icons.Default.CreditCard,
                        selected = selectedPaymentMethod == PaymentMethod.CREDIT_CARD,
                        onClick = { selectedPaymentMethod = PaymentMethod.CREDIT_CARD }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    PaymentOption(
                        title = "Debit Card",
                        description = "Pay using your debit card",
                        icon = Icons.Default.CreditCard,
                        selected = selectedPaymentMethod == PaymentMethod.DEBIT_CARD,
                        onClick = { selectedPaymentMethod = PaymentMethod.DEBIT_CARD }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { 
                            viewModel.updatePaymentMethod(
                                transactionId,
                                selectedPaymentMethod.toString(),
                                onComplete = onPaymentComplete
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Continue with Payment")
                    }
                }
            }
            
            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(state.error ?: "An error occurred") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentOption(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 