package com.mhez_dev.rentabols_v1.domain.usecase.auth

import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.signIn(email, password)
}
