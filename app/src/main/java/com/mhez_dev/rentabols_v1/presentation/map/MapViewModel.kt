package com.mhez_dev.rentabols_v1.presentation.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import com.mhez_dev.rentabols_v1.domain.usecase.rental.SearchRentalItemsUseCase
import com.mhez_dev.rentabols_v1.utils.MapUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MapState(
    val items: List<RentalItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SingleItemState(
    val item: RentalItem? = null,
    val nearbyItems: List<RentalItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapViewModel(
    private val searchRentalItemsUseCase: SearchRentalItemsUseCase,
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapState(isLoading = true))
    val state: StateFlow<MapState> = _state

    private val _singleItemState = MutableStateFlow(SingleItemState(isLoading = false))
    val singleItemState: StateFlow<SingleItemState> = _singleItemState
    
    // Track last search location to avoid redundant searches
    private val _lastSearchLocation = MutableStateFlow<GeoPoint?>(null)
    val lastSearchLocation: GeoPoint? get() = _lastSearchLocation.value

    fun searchNearbyItems(location: GeoPoint, radiusInKm: Double) {
        // Update the last search location
        _lastSearchLocation.value = location
        
        _state.value = _state.value.copy(isLoading = true)

        // Add a small delay to prevent rapid consecutive searches
        viewModelScope.launch {
            try {
                // Use a custom implementation that efficiently manages network requests
                searchRentalItemsUseCase(
                    location = location,
                    radius = radiusInKm
                )
                    .onEach { items ->
                        Log.d("MapViewModel", "Found ${items.size} nearby items")
                        _state.value = MapState(
                            items = items,
                            isLoading = false
                        )
                    }
                    .catch { e ->
                        Log.e("MapViewModel", "Error searching nearby items", e)
                        _state.value = MapState(
                            error = e.message ?: "Failed to load nearby items",
                            isLoading = false
                        )
                    }
                    .launchIn(this)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Exception in searchNearbyItems", e)
                _state.value = _state.value.copy(
                    error = "Error: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun getItemById(itemId: String) {
        _singleItemState.value = _singleItemState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                rentalRepository.getItem(itemId).collect { item ->
                    if (item != null) {
                        _singleItemState.value = _singleItemState.value.copy(
                            item = item,
                            isLoading = false
                        )
                        // Fetch nearby items based on this item's location
                        searchNearbyItemsForSingleItem(item.location, MapUtils.DEFAULT_SEARCH_RADIUS) // Use constant instead of hardcoded 50.0
                    } else {
                        _singleItemState.value = _singleItemState.value.copy(
                            error = "Item not found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching item details", e)
                _singleItemState.value = _singleItemState.value.copy(
                    error = e.message ?: "Failed to load item details",
                    isLoading = false
                )
            }
        }
    }

    private fun searchNearbyItemsForSingleItem(location: GeoPoint, radiusInKm: Double) {
        searchRentalItemsUseCase(
            location = location,
            radius = radiusInKm
        )
            .onEach { items ->
                try {
                    // Filter out the main item we're viewing
                    val nearbyItems = items.filter { it.id != _singleItemState.value.item?.id }
                    _singleItemState.value = _singleItemState.value.copy(
                        nearbyItems = nearbyItems
                    )
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error processing nearby items", e)
                    _singleItemState.value = _singleItemState.value.copy(
                        error = "Error loading nearby items: ${e.message}"
                    )
                }
            }
            .catch { e ->
                Log.e("MapViewModel", "Error fetching nearby items", e)
                _singleItemState.value = _singleItemState.value.copy(
                    error = e.message ?: "Failed to load nearby items"
                )
            }
            .launchIn(viewModelScope)
    }
}
