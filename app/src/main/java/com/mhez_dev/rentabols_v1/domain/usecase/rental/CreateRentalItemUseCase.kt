package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository

class CreateRentalItemUseCase(private val repository: RentalRepository) {
    suspend operator fun invoke(item: RentalItem): Result<String> =
        repository.createItem(item)
}
