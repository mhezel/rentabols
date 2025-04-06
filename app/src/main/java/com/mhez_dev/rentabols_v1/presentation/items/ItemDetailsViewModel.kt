package com.mhez_dev.rentabols_v1.presentation.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.usecase.rental.CreateRentalRequestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RentalRequestState {
    object Initial : RentalRequestState()
    object Loading : RentalRequestState()
    object Success : RentalRequestState()
    data class Error(val message: String) : RentalRequestState()
}

class ItemDetailsViewModel(
    private val createRentalRequestUseCase: CreateRentalRequestUseCase,
    private val rentalRepository: com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
) : ViewModel() {

    private val _item = MutableStateFlow<RentalItem?>(null)
    val item: StateFlow<RentalItem?> = _item

    private val _requestState = MutableStateFlow<RentalRequestState>(RentalRequestState.Initial)
    val requestState: StateFlow<RentalRequestState> = _requestState

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            rentalRepository.getItem(itemId)
                .collect { item ->
                    _item.value = item
                }
        }
    }

    fun createRentalRequest(
        renterId: String,
        startDate: Long,
        endDate: Long
    ) {
        val currentItem = _item.value ?: return

        viewModelScope.launch {
            _requestState.value = RentalRequestState.Loading
            
            val transaction = RentalTransaction(
                id = "",  // Will be set by Firebase
                itemId = currentItem.id,
                lenderId = currentItem.ownerId,
                renterId = renterId,
                startDate = startDate,
                endDate = endDate,
                totalPrice = calculateTotalPrice(currentItem.pricePerDay, startDate, endDate),
                status = RentalStatus.PENDING
            )

            createRentalRequestUseCase(transaction)
                .onSuccess {
                    _requestState.value = RentalRequestState.Success
                }
                .onFailure { exception ->
                    _requestState.value = RentalRequestState.Error(
                        exception.message ?: "Failed to create rental request"
                    )
                }
        }
    }

    private fun calculateTotalPrice(pricePerDay: Double, startDate: Long, endDate: Long): Double {
        val days = ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()
        return pricePerDay * days
    }
}
