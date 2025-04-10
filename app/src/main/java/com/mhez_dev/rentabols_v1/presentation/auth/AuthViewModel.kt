package com.mhez_dev.rentabols_v1.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.SignInUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.auth.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Set initial state
        _authState.value = AuthState.Initial
        
        // Check for existing user only if we're in Initial state
        // This allows sign-out to work correctly
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                if (_authState.value is AuthState.Initial && user != null) {
                    // Only auto-login if we're in the Initial state AND there's a user
                    // This prevents auto-login after sign-out since we'll have manually
                    // set the state to something other than Initial
                    _authState.value = AuthState.Success(user)
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                signInUseCase(email, password)
                    .onSuccess { user ->
                        _authState.value = AuthState.Success(user)
                    }
                    .onFailure { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                signUpUseCase(email, password, name)
                    .onSuccess { user ->
                        _authState.value = AuthState.Success(user)
                    }
                    .onFailure { exception ->
                        _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("An unexpected error occurred")
            }
        }
    }

    // Clear auth state - explicitly set to Initial
    fun clearState() {
        _authState.value = AuthState.Initial
    }

    fun toggleAuthMode() {
        // Implementation of toggleAuthMode method
    }
}
