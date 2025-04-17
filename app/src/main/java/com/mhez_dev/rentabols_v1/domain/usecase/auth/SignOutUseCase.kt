package com.mhez_dev.rentabols_v1.domain.usecase.auth

import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() {
        try {
            withContext(Dispatchers.IO) {
                repository.signOut()
            }
        } catch (e: Exception) {
            // Just log the error and return - don't throw
            e.printStackTrace()
        }
    }
} 