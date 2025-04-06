package com.mhez_dev.rentabols_v1.domain.usecase.auth

import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<User> =
        repository.signUp(email, password, name)
}
