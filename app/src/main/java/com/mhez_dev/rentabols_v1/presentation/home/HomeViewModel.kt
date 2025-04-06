package com.mhez_dev.rentabols_v1.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
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
    private val searchRentalItemsUseCase: SearchRentalItemsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state

    private var currentQuery: String? = null
    private var currentCategory: String? = null
    private var currentLocation: GeoPoint? = null
    private var currentRadius: Double? = null

    init {
        // Start with an empty list instead of loading state
        _state.value = HomeState.Success(emptyList())
    }

    fun searchItems(
        query: String? = currentQuery,
        category: String? = currentCategory,
        location: GeoPoint? = currentLocation,
        radius: Double? = currentRadius
    ) {
        currentQuery = query
        currentCategory = category
        currentLocation = location
        currentRadius = radius

        searchRentalItemsUseCase(
            query = query,
            category = category,
            location = location,
            radius = radius
        )
            .onEach { items ->
                _state.value = HomeState.Success(items)
            }
            .catch { e ->
                _state.value = HomeState.Error(e.message ?: "Failed to load items")
            }
            .launchIn(viewModelScope)
    }

    fun updateCategory(category: String?) {
        searchItems(category = category)
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onSignOutComplete()
            } catch (e: Exception) {
                _state.value = HomeState.Error("Failed to sign out: ${e.message}")
            }
        }
    }
}
