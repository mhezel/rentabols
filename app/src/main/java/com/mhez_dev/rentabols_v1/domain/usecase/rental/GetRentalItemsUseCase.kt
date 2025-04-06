package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow

class GetRentalItemsUseCase(private val repository: RentalRepository) {
    operator fun invoke(
        searchQuery: String? = null,
        category: String? = null
    ): Flow<List<RentalItem>> =
        repository.getItems(
            searchQuery = searchQuery,
            category = category
        )
}
