package com.mhez_dev.rentabols_v1.domain.repository

import com.mhez_dev.rentabols_v1.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, name: String): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): Flow<User?>
    suspend fun updateProfile(user: User): Result<Unit>
    fun getUserById(userId: String): Flow<User?>
}
