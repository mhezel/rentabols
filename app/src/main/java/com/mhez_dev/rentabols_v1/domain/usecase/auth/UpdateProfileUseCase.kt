package com.mhez_dev.rentabols_v1.domain.usecase.auth

import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository

class UpdateProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User): Result<Unit> =
        repository.updateProfile(user)
} 