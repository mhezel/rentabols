package com.mhez_dev.rentabols_v1.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.usecase.rental.SearchRentalItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class MapState(
    val items: List<RentalItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapViewModel(
    private val searchRentalItemsUseCase: SearchRentalItemsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapState(isLoading = true))
    val state: StateFlow<MapState> = _state

    fun searchNearbyItems(location: GeoPoint, radiusInKm: Double) {
        _state.value = _state.value.copy(isLoading = true)

        searchRentalItemsUseCase(
            location = location,
            radius = radiusInKm
        )
            .onEach { items ->
                _state.value = MapState(
                    items = items,
                    isLoading = false
                )
            }
            .catch { e ->
                _state.value = MapState(
                    error = e.message ?: "Failed to load nearby items",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}
