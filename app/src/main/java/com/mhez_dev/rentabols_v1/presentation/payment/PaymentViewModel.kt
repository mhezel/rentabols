package com.mhez_dev.rentabols_v1.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PaymentState(
    val transaction: RentalTransaction? = null,
    val itemName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PaymentViewModel(
    private val rentalRepository: RentalRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PaymentState(isLoading = true))
    val state: StateFlow<PaymentState> = _state
    
    fun getTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Fetch the transaction details
                val transaction = rentalRepository.getTransaction(transactionId)
                
                if (transaction != null) {
                    // Fetch item details to get the name
                    val item = rentalRepository.getItemById(transaction.itemId)
                    val itemName = item?.title ?: transaction.metadata["itemName"] as? String ?: "Unknown Item"
                    
                    _state.value = _state.value.copy(
                        transaction = transaction,
                        itemName = itemName,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Transaction not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to load transaction details",
                    isLoading = false
                )
            }
        }
    }
    
    fun updatePaymentMethod(
        transactionId: String, 
        paymentMethod: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val transaction = _state.value.transaction
                if (transaction != null) {
                    // Create an updated transaction with the payment method
                    val updatedTransaction = transaction.copy(
                        paymentMethod = paymentMethod
                    )
                    
                    // Update the transaction in the repository
                    rentalRepository.updateTransaction(updatedTransaction)
                    
                    // Reset the loading state
                    _state.value = _state.value.copy(
                        transaction = updatedTransaction,
                        isLoading = false
                    )
                    
                    // Notify completion
                    onComplete()
                } else {
                    _state.value = _state.value.copy(
                        error = "Cannot update: Transaction details not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to update payment method",
                    isLoading = false
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 