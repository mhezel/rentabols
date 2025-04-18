package com.mhez_dev.rentabols_v1.presentation.items

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetUserByIdUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.CreateRentalRequestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class RentalRequestState {
    object Initial : RentalRequestState()
    object Loading : RentalRequestState()
    object Success : RentalRequestState()
    data class Error(val message: String) : RentalRequestState()
}

sealed class ItemDetailsState {
    object Loading : ItemDetailsState()
    data class Success(val item: RentalItem) : ItemDetailsState()
    data class Error(val message: String) : ItemDetailsState()
}

class ItemDetailsViewModel(
    private val createRentalRequestUseCase: CreateRentalRequestUseCase,
    private val rentalRepository: RentalRepository,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<ItemDetailsState>(ItemDetailsState.Loading)
    val state: StateFlow<ItemDetailsState> = _state

    private val _item = MutableStateFlow<RentalItem?>(null)
    val item: StateFlow<RentalItem?> = _item

    private val _requestState = MutableStateFlow<RentalRequestState>(RentalRequestState.Initial)
    val requestState: StateFlow<RentalRequestState> = _requestState

    private val _ownerName = MutableStateFlow<String?>(null)
    val ownerName: StateFlow<String?> = _ownerName
    
    private val _ownerProfilePic = MutableStateFlow<String?>(null)
    val ownerProfilePic: StateFlow<String?> = _ownerProfilePic

    fun getItemDetails(itemId: String) {
        viewModelScope.launch {
            try {
                rentalRepository.getItem(itemId).collect { item ->
                    if (item != null) {
                        _item.value = item
                        _state.value = ItemDetailsState.Success(item)
                        fetchOwnerInfo(item.ownerId)
                    } else {
                        _state.value = ItemDetailsState.Error("Item not found")
                    }
                }
            } catch (e: Exception) {
                _state.value = ItemDetailsState.Error(e.message ?: "Failed to fetch item details")
            }
        }
    }

    private fun fetchOwnerInfo(ownerId: String) {
        viewModelScope.launch {
            getUserByIdUseCase(ownerId).collect { user ->
                _ownerName.value = user?.name
                _ownerProfilePic.value = user?.profileImageUrl
            }
        }
    }

    fun createRentalRequest(
        startDate: Long, 
        endDate: Long,
        offerPrice: Double? = null,
        deliveryOption: String? = null,
        paymentMethod: String? = null,
        message: String? = null
    ) {
        if (startDate >= endDate) {
            _requestState.value = RentalRequestState.Error("End date must be after start date")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _requestState.value = RentalRequestState.Error("You must be logged in to make a request")
            return
        }

        _requestState.value = RentalRequestState.Loading
        val item = _item.value ?: return
        
        // Prevent users from renting their own items
        if (currentUser.uid == item.ownerId) {
            _requestState.value = RentalRequestState.Error("You cannot rent your own item")
            return
        }

        viewModelScope.launch {
            // Get full user profile to ensure we have the name
            val currentUserProfile = try {
                getCurrentUserUseCase().first()
            } catch (e: Exception) {
                null
            }
            
            // Calculate the price based on offer price or item's standard price
            val pricePerDay = offerPrice ?: item.pricePerDay
            val totalPrice = calculateTotalPrice(pricePerDay, startDate, endDate)
            
            // Create metadata map for additional info
            val metadata = mutableMapOf<String, Any>()
            if (message != null && message.isNotBlank()) {
                metadata["message"] = message
            }
            if (offerPrice != null) {
                metadata["isOffer"] = true
                metadata["offerPricePerDay"] = offerPrice
                metadata["standardPricePerDay"] = item.pricePerDay
            }
            
            // Include renter name in metadata
            val renterName = currentUserProfile?.name ?: currentUser.displayName ?: "Unknown User"
            metadata["renterName"] = renterName
            
            // Include item details
            metadata["itemName"] = item.title
            if (item.imageUrls.isNotEmpty()) {
                metadata["itemImageUrl"] = item.imageUrls.first()
            }
            
            val transaction = RentalTransaction(
                itemId = item.id,
                lenderId = item.ownerId,
                renterId = currentUser.uid,
                startDate = startDate,
                endDate = endDate,
                totalPrice = totalPrice,
                status = RentalStatus.PENDING,
                paymentMethod = paymentMethod,
                deliveryOption = deliveryOption,
                metadata = metadata
            )

            val result = createRentalRequestUseCase(transaction)
            
            _requestState.value = if (result.isSuccess) {
                RentalRequestState.Success
            } else {
                RentalRequestState.Error(result.exceptionOrNull()?.message ?: "Failed to create request")
            }
        }
    }

    private fun calculateTotalPrice(pricePerDay: Double, startDate: Long, endDate: Long): Double {
        val days = ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt() + 1
        return pricePerDay * days
    }

    fun updateItem(item: RentalItem, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = rentalRepository.updateItem(item)
                onResult(result)
                if (result.isSuccess) {
                    _item.value = item
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }
    
    fun uploadItemImages(
        itemId: String, 
        images: List<Uri>,
        onProgress: (Int) -> Unit,
        onComplete: (Result<List<String>>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val firebaseStorage = FirebaseStorage.getInstance()
                val uploadedImageUrls = mutableListOf<String>()
                val totalImages = images.size
                var completedUploads = 0
                
                for ((index, uri) in images.withIndex()) {
                    val imageRef = firebaseStorage.reference
                        .child("items")
                        .child(itemId)
                        .child("image_${System.currentTimeMillis()}_$index.jpg")
                    
                    // Upload file
                    val uploadTask = imageRef.putFile(uri)
                    uploadTask.await()
                    
                    // Get download URL
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    uploadedImageUrls.add(downloadUrl)
                    
                    // Update progress
                    completedUploads++
                    val progress = (completedUploads * 100) / totalImages
                    onProgress(progress)
                }
                
                // If we successfully uploaded images, update the item with new URLs
                if (uploadedImageUrls.isNotEmpty() && _item.value != null) {
                    val currentItem = _item.value!!
                    val updatedImageUrls = currentItem.imageUrls + uploadedImageUrls
                    val updatedItem = currentItem.copy(imageUrls = updatedImageUrls)
                    
                    val updateResult = rentalRepository.updateItem(updatedItem)
                    if (updateResult.isSuccess) {
                        _item.value = updatedItem
                        onComplete(Result.success(uploadedImageUrls))
                    } else {
                        onComplete(Result.failure(updateResult.exceptionOrNull() 
                            ?: Exception("Failed to update item with new images")))
                    }
                } else {
                    onComplete(Result.success(uploadedImageUrls))
                }
            } catch (e: Exception) {
                onComplete(Result.failure(e))
            }
        }
    }
}
