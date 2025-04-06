package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository

class CreateRentalRequestUseCase(private val repository: RentalRepository) {
    suspend operator fun invoke(transaction: RentalTransaction): Result<String> =
        repository.createRentalRequest(transaction)
}
