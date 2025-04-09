package com.mhez_dev.rentabols_v1.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.rental.SearchRentalItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val items: List<RentalItem>) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<RentalItem>>(emptyList())
    val items: StateFlow<List<RentalItem>> = _items
    
    private val _currentSearchQuery = MutableStateFlow<String?>(null)
    private val _currentCategory = MutableStateFlow<String?>(null)

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            rentalRepository.getItems(null, null)
                .collect { items ->
                    _items.value = items
                }
        }
    }

    fun searchItems(query: String?) {
        _currentSearchQuery.value = query
        performSearch()
    }
    
    fun filterByCategory(category: String?) {
        _currentCategory.value = category
        performSearch()
    }
    
    private fun performSearch() {
        viewModelScope.launch {
            rentalRepository.getItems(
                searchQuery = _currentSearchQuery.value,
                category = _currentCategory.value
            ).collect { items ->
                _items.value = items
            }
        }
    }
    
    fun clearFilters() {
        _currentSearchQuery.value = null
        _currentCategory.value = null
        loadItems()
    }
}
