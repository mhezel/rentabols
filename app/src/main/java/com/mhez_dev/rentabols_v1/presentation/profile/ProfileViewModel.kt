package com.mhez_dev.rentabols_v1.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.model.User
import com.mhez_dev.rentabols_v1.domain.usecase.auth.GetCurrentUserUseCase
import com.mhez_dev.rentabols_v1.domain.usecase.rental.GetUserTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class ProfileState(
    val user: User? = null,
    val transactions: List<RentalTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserTransactionsUseCase: GetUserTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState(isLoading = true))
    val state: StateFlow<ProfileState> = _state

    init {
        loadUserData()
    }

    private fun loadUserData() {
        getCurrentUserUseCase()
            .onEach { user ->
                _state.value = _state.value.copy(
                    user = user,
                    isLoading = false
                )
                
                user?.let { loadUserTransactions(it.id) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUserTransactions(userId: String) {
        getUserTransactionsUseCase(userId)
            .onEach { transactions ->
                _state.value = _state.value.copy(
                    transactions = transactions,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}
