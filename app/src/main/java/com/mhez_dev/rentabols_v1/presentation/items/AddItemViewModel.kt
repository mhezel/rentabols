package com.mhez_dev.rentabols_v1.presentation.items

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class AddItemState {
    object Initial : AddItemState()
    object Loading : AddItemState()
    data class Uploading(val progress: Int) : AddItemState()
    object Success : AddItemState()
    data class Error(val message: String) : AddItemState()
}

class AddItemViewModel(
    private val rentalRepository: RentalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val firebaseStorage = FirebaseStorage.getInstance()
    private val _state = MutableStateFlow<AddItemState>(AddItemState.Initial)
    val state: StateFlow<AddItemState> = _state

    fun createItem(
        title: String,
        description: String,
        category: String,
        pricePerDay: Double,
        images: List<Uri>,
        location: GeoPoint,
        metadata: Map<String, Any>
    ) {
        if (title.isBlank() || description.isBlank() || category.isBlank() || pricePerDay <= 0) {
            _state.value = AddItemState.Error("Please fill in all required fields")
            return
        }

        _state.value = AddItemState.Loading

        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser().first()
                if (currentUser == null) {
                    _state.value = AddItemState.Error("User not authenticated")
                    return@launch
                }

                // First, create the rental item without images
                val item = RentalItem(
                    id = "",  // Will be set by Firebase
                    ownerId = currentUser.id,
                    title = title,
                    description = description,
                    category = category,
                    pricePerDay = pricePerDay,
                    location = location,
                    address = "", // Optional address
                    imageUrls = emptyList(), // Will be updated after upload
                    availability = true,
                    rating = 0.0,
                    reviewCount = 0,
                    createdAt = System.currentTimeMillis(),
                    searchKeywords = generateSearchKeywords(title, description, category),
                    metadata = metadata
                )

                // Step 1: Create the item in Firestore (without images)
                val itemResult = rentalRepository.createItem(item)
                if (itemResult.isFailure) {
                    _state.value = AddItemState.Error(itemResult.exceptionOrNull()?.message ?: "Failed to create item")
                    return@launch
                }
                
                val itemId = itemResult.getOrNull() ?: ""
                
                // If no images to upload, we're done
                if (images.isEmpty()) {
                    _state.value = AddItemState.Success
                    return@launch
                }
                
                // Step 2: Now attempt to upload images to Firebase Storage
                _state.value = AddItemState.Uploading(0)
                try {
                    val uploadedImageUrls = mutableListOf<String>()
                    
                    // Upload just the first image for simplicity
                    val uri = images.first()
                    val imageRef = firebaseStorage.reference
                        .child("users")
                        .child(currentUser.id)
                        .child("items")
                        .child(itemId)
                        .child("image_0.jpg")
                    
                    // Upload the file
                    val uploadTask = imageRef.putFile(uri)
                    uploadTask.await()
                    
                    // Get download URL
                    val downloadUrl = imageRef.downloadUrl.await().toString()
                    uploadedImageUrls.add(downloadUrl)
                    
                    // If we got here, update the item with the image URL
                    if (uploadedImageUrls.isNotEmpty()) {
                        val updatedItem = item.copy(id = itemId, imageUrls = uploadedImageUrls)
                        rentalRepository.updateItem(updatedItem)
                    }
                    
                    _state.value = AddItemState.Success
                } catch (e: Exception) {
                    // Item created but image upload failed
                    _state.value = AddItemState.Error("Item created but failed to upload images: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = AddItemState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun generateSearchKeywords(title: String, description: String, category: String): List<String> {
        val words = mutableSetOf<String>()
        
        // Add title words
        words.addAll(title.lowercase().split(" ").filter { it.length > 2 })
        
        // Add description words
        words.addAll(description.lowercase().split(" ").filter { it.length > 2 })
        
        // Add category
        words.add(category.lowercase())
        
        return words.toList()
    }
} 