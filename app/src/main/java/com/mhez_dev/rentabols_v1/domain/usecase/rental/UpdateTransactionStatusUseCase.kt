package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateTransactionStatusUseCase(
    private val rentalRepository: RentalRepository
) {
    suspend operator fun invoke(transaction: RentalTransaction): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            rentalRepository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 