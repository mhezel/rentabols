package com.mhez_dev.rentabols_v1.domain.usecase.auth

import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetUserByIdUseCase(private val repository: AuthRepository) {
    operator fun invoke(userId: String): Flow<User?> = repository.getUserById(userId)
} 