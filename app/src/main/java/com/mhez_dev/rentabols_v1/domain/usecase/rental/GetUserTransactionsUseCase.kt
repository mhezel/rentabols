package com.mhez_dev.rentabols_v1.domain.usecase.rental

import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetUserTransactionsUseCase(private val repository: RentalRepository) {
    operator fun invoke(userId: String): Flow<List<RentalTransaction>> =
        combine(
            repository.getLenderTransactions(userId),
            repository.getRenterTransactions(userId)
        ) { lenderTransactions, renterTransactions ->
            (lenderTransactions + renterTransactions).sortedByDescending { it.createdAt }
        }
}
