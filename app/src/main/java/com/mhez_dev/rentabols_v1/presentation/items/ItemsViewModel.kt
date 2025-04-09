package com.mhez_dev.rentabols_v1.presentation.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class UserItemsState {
    object Loading : UserItemsState()
    data class Success(val items: List<RentalItem>) : UserItemsState()
    data class Error(val message: String) : UserItemsState()
}

class ItemsViewModel(
    private val rentalRepository: RentalRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<UserItemsState>(UserItemsState.Loading)
    val state: StateFlow<UserItemsState> = _state
    
    private val _userItems = MutableStateFlow<List<RentalItem>>(emptyList())
    val userItems: StateFlow<List<RentalItem>> = _userItems
    
    init {
        loadUserItems()
    }
    
    private fun loadUserItems() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            _state.value = UserItemsState.Error("User not authenticated")
            return
        }
        
        _state.value = UserItemsState.Loading
        
        viewModelScope.launch {
            rentalRepository.getItems(null, null)
                .map { items -> 
                    items.filter { it.ownerId == currentUserId }
                }
                .catch { e ->
                    _state.value = UserItemsState.Error(e.message ?: "Failed to load your items")
                }
                .collect { filteredItems ->
                    _userItems.value = filteredItems
                    _state.value = UserItemsState.Success(filteredItems)
                }
        }
    }
    
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                rentalRepository.deleteItem(itemId)
                // Refresh the list after deletion
                loadUserItems()
            } catch (e: Exception) {
                _state.value = UserItemsState.Error("Failed to delete item: ${e.message}")
            }
        }
    }
} 