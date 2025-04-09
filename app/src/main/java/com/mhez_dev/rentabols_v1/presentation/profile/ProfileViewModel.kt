package com.mhez_dev.rentabols_v1.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.UpdateProfileUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.GetUserTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileState(
    val user: User? = null,
    val transactions: List<RentalTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false
)

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserTransactionsUseCase: GetUserTransactionsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state

    init {
        loadUserData()
    }

    private fun loadUserData() {
        getCurrentUserUseCase()
            .onEach { user ->
                _state.value = _state.value.copy(
                    user = user,
                    isLoading = false,
                    isUpdating = false,
                    updateSuccess = false
                )
                
                user?.let { loadUserTransactions(it.id) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUserTransactions(userId: String) {
        getUserTransactionsUseCase(userId)
            .onEach { transactions ->
                _state.value = _state.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
    
    fun updateProfile(
        name: String,
        phoneNumber: String,
        location: String,
        gender: String,
        birthdate: Long?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, error = null, updateSuccess = false)
            
            val currentUser = _state.value.user ?: return@launch
            
            val updatedUser = currentUser.copy(
                name = name,
                phoneNumber = phoneNumber,
                location = location,
                gender = gender,
                birthdate = birthdate
            )
            
            try {
                val result = updateProfileUseCase(updatedUser)
                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update profile"
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
    
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, error = null)
            
            val currentUser = _state.value.user ?: return@launch
            val userId = currentUser.id
            
            try {
                val firebaseStorage = FirebaseStorage.getInstance()
                val imageRef = firebaseStorage.reference
                    .child("users")
                    .child(userId)
                    .child("profile_${System.currentTimeMillis()}.jpg")
                
                // Upload the image
                val uploadTask = imageRef.putFile(imageUri)
                uploadTask.await()
                
                // Get the download URL
                val downloadUrl = imageRef.downloadUrl.await().toString()
                
                // Update user with new profile picture URL
                val updatedUser = currentUser.copy(profileImageUrl = downloadUrl)
                val result = updateProfileUseCase(updatedUser)
                
                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update profile picture"
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
    
    fun resetUpdateStatus() {
        _state.value = _state.value.copy(
            updateSuccess = false,
            error = null
        )
    }
}
