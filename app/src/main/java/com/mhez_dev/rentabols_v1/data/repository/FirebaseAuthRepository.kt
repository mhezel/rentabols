package com.mhez_dev.rentabols_v1.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.repository.AuthRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeoutException

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        withTimeout(30000) { // 30 seconds timeout
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java) ?: User(
                    email = email,
                    name = firebaseUser.displayName ?: ""
                )
                user.id = firebaseUser.uid
                Result.success(user)
            } ?: Result.failure(Exception("Authentication failed"))
        }
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is FirebaseAuthInvalidUserException -> "Account not found. Please check your email or sign up."
            is FirebaseAuthInvalidCredentialsException -> "Invalid password. Please try again."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is TimeoutException -> "Connection timeout. Please try again."
            else -> "Authentication failed: ${e.message}"
        }
        Result.failure(Exception(errorMessage))
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> = try {
        withTimeout(30000) { // 30 seconds timeout
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val user = User(
                    email = email,
                    name = name
                )
                user.id = firebaseUser.uid
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                Result.success(user)
            } ?: Result.failure(Exception("User creation failed"))
        }
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email format. Please check your email."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is TimeoutException -> "Connection timeout. Please try again."
            else -> "Sign up failed: ${e.message}"
        }
        Result.failure(Exception(errorMessage))
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { firebaseUser ->
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val user = snapshot?.toObject(User::class.java)
                        user?.id = firebaseUser.uid
                        trySend(user)
                    }
            } ?: trySend(null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun updateProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
