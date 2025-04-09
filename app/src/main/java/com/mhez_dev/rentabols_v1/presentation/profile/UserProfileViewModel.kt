package com.mhez_dev.rentabols_v1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetUserByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _state.value = UserProfileState(isLoading = true)
            
            try {
                // Load user profile
                getUserByIdUseCase(userId).collect { user ->
                    if (user != null) {
                        _state.value = _state.value.copy(
                            user = user,
                            isLoading = false
                        )
                        
                        // Load user's items
                        loadUserItems(userId)
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "User not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while loading the user profile"
                )
            }
        }
    }
    
    private fun loadUserItems(userId: String) {
        viewModelScope.launch {
            try {
                rentalRepository.getItemsByOwnerId(userId)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            error = e.message ?: "Failed to load user items"
                        )
                    }
                    .collect { items ->
                        _state.value = _state.value.copy(
                            userItems = items
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "An error occurred while loading user items"
                )
            }
        }
    }
} 