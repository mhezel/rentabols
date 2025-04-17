package com.mhez_dev.rentabols_v1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.GetUserLendingTransactionsUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.UpdateTransactionStatusUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetUserByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

data class TransactionItem(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val renterName: String = "Unknown Renter"
)

data class RentTransactionsState(
    val transactions: List<RentalTransaction> = emptyList(),
    val itemsMap: Map<String, TransactionItem> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false
)

class RentTransactionsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserLendingTransactionsUseCase: GetUserLendingTransactionsUseCase,
    private val rentalRepository: RentalRepository,
    private val updateTransactionStatusUseCase: UpdateTransactionStatusUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RentTransactionsState(isLoading = true))
    val state: StateFlow<RentTransactionsState> = _state

    init {
        loadUserData()
    }

    private fun loadUserData() {
        getCurrentUserUseCase()
            .onEach { user ->
                user?.let { 
                    loadUserTransactions(it.id) 
                } ?: run {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUserTransactions(userId: String) {
        getUserLendingTransactionsUseCase(userId)
            .onEach { transactions ->
                _state.value = _state.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
                
                // Load item details for each transaction
                loadItemDetails(transactions)
            }
            .launchIn(viewModelScope)
    }
    
    private fun loadItemDetails(transactions: List<RentalTransaction>) {
        viewModelScope.launch {
            val itemsMap = mutableMapOf<String, TransactionItem>()
            
            for (transaction in transactions) {
                try {
                    // Load item details
                    val item = rentalRepository.getItemById(transaction.itemId)
                    if (item == null) continue
                    
                    var renterName = "Unknown"
                    
                    try {
                        // Try to get user info without nested suspending functions
                        val user = getUserByIdUseCase(transaction.renterId).first()
                        if (user != null) {
                            renterName = when {
                                user.name.isNotBlank() -> user.name
                                user.email.isNotBlank() -> user.email
                                else -> "User ${transaction.renterId.takeLast(5)}"
                            }
                        }
                    } catch (e: Exception) {
                        // If user lookup fails, use unknown renter
                        renterName = "Unknown User"
                    }
                    
                    // Add the item with the renter name
                    itemsMap[item.id] = TransactionItem(
                        id = item.id,
                        title = item.title,
                        imageUrl = item.imageUrls.firstOrNull(),
                        renterName = renterName
                    )
                } catch (e: Exception) {
                    // Handle item loading error silently
                }
            }
            
            // Update state once with all items
            _state.value = _state.value.copy(
                itemsMap = itemsMap
            )
        }
    }
    
    fun updateTransactionStatus(transactionId: String, newStatus: RentalStatus) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, error = null)
            
            try {
                val transaction = _state.value.transactions.find { it.id == transactionId }
                transaction?.let {
                    val updatedTransaction = it.copy(status = newStatus)
                    
                    val result = updateTransactionStatusUseCase(updatedTransaction)
                    if (result.isSuccess) {
                        // Update the transaction in the local state
                        val updatedTransactions = _state.value.transactions.map { t ->
                            if (t.id == transactionId) updatedTransaction else t
                        }
                        
                        _state.value = _state.value.copy(
                            transactions = updatedTransactions,
                            isUpdating = false,
                            updateSuccess = true
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isUpdating = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to update transaction status"
                        )
                    }
                } ?: run {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        error = "Transaction not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUpdating = false,
                    error = e.message ?: "An unknown error occurred"
                )
            }
        }
    }
} 