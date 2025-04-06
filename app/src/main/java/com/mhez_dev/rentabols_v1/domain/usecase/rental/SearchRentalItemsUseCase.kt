package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow

class SearchRentalItemsUseCase(private val repository: RentalRepository) {
    operator fun invoke(
        query: String? = null,
        category: String? = null,
        location: GeoPoint? = null,
        radius: Double? = null
    ): Flow<List<RentalItem>> =
        repository.searchItems(
            query = query,
            category = category,
            location = location,
            radius = radius
        )
}
