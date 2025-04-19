package com.mhez_dev.rentabols_v1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetUserByIdUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.GetUserTransactionsUseCase
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class RentItem(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val lenderName: String,
    val rentalAmount: Double,
    val status: RentalStatus,
    val startDate: Long?,
    val endDate: Long?,
    val itemId: String,
    val isDeliveryAvailable: Boolean = false
)

data class MyRentItemsState(
    val rentItems: List<RentItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyRentItemsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserTransactionsUseCase: GetUserTransactionsUseCase,
    private val rentalRepository: RentalRepository,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyRentItemsState(isLoading = true))
    val state: StateFlow<MyRentItemsState> = _state

    init {
        loadUserRentals()
    }

    private fun loadUserRentals() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            getCurrentUserUseCase()
                .onEach { user ->
                    user?.let { currentUser ->
                        // Load transactions where the current user is the renter
                        getUserTransactionsUseCase(currentUser.id)
                            .onEach { transactions ->
                                // Filter for transactions where user is the renter not the lender
                                val userRentals = transactions.filter { it.renterId == currentUser.id }
                                
                                val rentItemsList = mutableListOf<RentItem>()
                                
                                for (transaction in userRentals) {
                                    // Fetch item details if needed
                                    val item = try {
                                        rentalRepository.getItemById(transaction.itemId)
                                    } catch (e: Exception) {
                                        null
                                    }
                                    
                                    // Fetch lender details to get the name
                                    val lenderName = try {
                                        // GetUserByIdUseCase returns a Flow, so we need to collect the first value
                                        val lenderFlow = getUserByIdUseCase(transaction.lenderId)
                                        val lenderUser = lenderFlow.first()
                                        
                                        // Use the name if available, fallback to email or a default
                                        when {
                                            lenderUser != null && lenderUser.name.isNotBlank() -> lenderUser.name
                                            lenderUser != null && lenderUser.email.isNotBlank() -> lenderUser.email
                                            else -> "Unknown Lender"
                                        }
                                    } catch (e: Exception) {
                                        "Unknown Lender"
                                    }
                                    
                                    // Check if delivery is available from item metadata
                                    val isDeliveryAvailable = try {
                                        item?.metadata?.get("isForDelivery") as? Boolean ?: false
                                    } catch (e: Exception) {
                                        false
                                    }
                                    
                                    // Create a RentItem using available data
                                    val rentItem = RentItem(
                                        id = transaction.id,
                                        title = item?.title ?: "Unknown Item",
                                        imageUrl = item?.imageUrls?.firstOrNull(),
                                        lenderName = lenderName,
                                        rentalAmount = transaction.totalPrice,
                                        status = transaction.status,
                                        startDate = transaction.startDate,
                                        endDate = transaction.endDate,
                                        itemId = transaction.itemId,
                                        isDeliveryAvailable = isDeliveryAvailable
                                    )
                                    
                                    rentItemsList.add(rentItem)
                                }
                                
                                _state.value = _state.value.copy(
                                    rentItems = rentItemsList,
                                    isLoading = false
                                )
                            }
                            .launchIn(viewModelScope)
                    } ?: run {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "User not found"
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    // Method to cancel a rental request
    fun cancelRental(transactionId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Update the transaction status to CANCELLED
                rentalRepository.updateTransactionStatus(transactionId, RentalStatus.CANCELLED)
                
                // Reload the user rentals to reflect the change
                loadUserRentals()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to cancel rental: ${e.message}"
                )
            }
        }
    }
} 