package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow

class GetUserLendingTransactionsUseCase(
    private val rentalRepository: RentalRepository
) {
    operator fun invoke(userId: String): Flow<List<RentalTransaction>> {
        return rentalRepository.getLendingTransactionsForUser(userId)
    }
} 