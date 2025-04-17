package com.mhez_dev.rentabols_v1.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // Keep track of all active Firestore listeners
    private val listeners = mutableListOf<ListenerRegistration>()

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
        try {
            // Ensure we're on a background thread
            withContext(Dispatchers.IO) {
                // First, detach all Firestore listeners to prevent permission errors
                synchronized(listeners) {
                    listeners.forEach { listener ->
                        try {
                            listener.remove()
                        } catch (e: Exception) {
                            // Ignore removal errors
                        }
                    }
                    listeners.clear()
                }
                
                // Then sign out from Firebase Auth
                auth.signOut()
            }
        } catch (e: Exception) {
            // Log the error but don't throw to prevent app crashes
            e.printStackTrace()
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.let { firebaseUser ->
                val listenerRegistration = firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val user = snapshot?.toObject(User::class.java)
                        user?.id = firebaseUser.uid
                        trySend(user)
                    }
                
                // Track this listener
                synchronized(listeners) {
                    listeners.add(listenerRegistration)
                }
            } ?: trySend(null)
        }
        
        auth.addAuthStateListener(listener)
        
        awaitClose { 
            auth.removeAuthStateListener(listener)
            
            // Also clean up any lingering Firestore listeners
            synchronized(listeners) {
                listeners.forEach { it.remove() }
                listeners.clear()
            }
        }
    }

    override suspend fun updateProfile(user: User): Result<Unit> = try {
        firestore.collection("users").document(user.id).set(user).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                
                val user = snapshot?.toObject(User::class.java)
                if (user != null) {
                    user.id = userId
                }
                trySend(user)
            }
        
        // Track this listener
        synchronized(listeners) {
            listeners.add(listenerRegistration)
        }
        
        awaitClose { 
            listenerRegistration.remove()
            
            // Remove from tracked listeners
            synchronized(listeners) {
                listeners.remove(listenerRegistration)
            }
        }
    }
}
