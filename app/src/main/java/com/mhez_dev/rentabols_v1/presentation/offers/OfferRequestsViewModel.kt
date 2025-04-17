package com.mhez_dev.rentabols_v1.presentation.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OfferRequestWithItem(
    val transaction: RentalTransaction,
    val item: RentalItem? = null
)

data class OfferRequestsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val offerRequests: List<OfferRequestWithItem> = emptyList()
)

class OfferRequestsViewModel(
    private val rentalRepository: RentalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _state = MutableStateFlow(OfferRequestsState(isLoading = true))
    val state: StateFlow<OfferRequestsState> = _state.asStateFlow()
    
    init {
        loadOfferRequests()
    }
    
    private fun loadOfferRequests() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                rentalRepository.getLenderTransactions(currentUserId)
                    .catch { e -> 
                        _state.update { it.copy(
                            isLoading = false, 
                            error = e.message ?: "Error loading offer requests"
                        )}
                    }
                    .collectLatest { transactions ->
                        val offerRequestsWithItems = ArrayList<OfferRequestWithItem>()
                        
                        for (transaction in transactions) {
                            try {
                                // Get the item for this transaction
                                val item = rentalRepository.getItem(transaction.itemId)
                                    .catch { /* Ignore errors and proceed with null item */ }
                                    .first()
                                
                                offerRequestsWithItems.add(
                                    OfferRequestWithItem(
                                        transaction = transaction,
                                        item = item
                                    )
                                )
                            } catch (e: Exception) {
                                // If there's an error getting the item, add the transaction with null item
                                offerRequestsWithItems.add(
                                    OfferRequestWithItem(
                                        transaction = transaction,
                                        item = null
                                    )
                                )
                            }
                        }
                        
                        _state.update { it.copy(
                            isLoading = false,
                            error = null,
                            offerRequests = offerRequestsWithItems
                        )}
                    }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading offer requests"
                )}
            }
        }
    }
    
    fun acceptOffer(transactionId: String) {
        viewModelScope.launch {
            rentalRepository.updateTransactionStatus(transactionId, RentalStatus.APPROVED)
                .onSuccess { loadOfferRequests() }
                .onFailure { e -> 
                    _state.update { it.copy(error = e.message ?: "Error accepting offer") }
                }
        }
    }
    
    fun rejectOffer(transactionId: String) {
        viewModelScope.launch {
            rentalRepository.updateTransactionStatus(transactionId, RentalStatus.REJECTED)
                .onSuccess { loadOfferRequests() }
                .onFailure { e -> 
                    _state.update { it.copy(error = e.message ?: "Error rejecting offer") }
                }
        }
    }
} 