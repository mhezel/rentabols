package com.mhez_dev.rentabols_v1.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class PaymentOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconId: String = "credit_card"
)

sealed class PaymentMethodEvent {
    data class SelectPaymentMethod(val paymentMethodId: String) : PaymentMethodEvent()
    object ProcessPayment : PaymentMethodEvent()
}

sealed class PaymentMethodEffect {
    object NavigateBack : PaymentMethodEffect()
    data class ShowError(val message: String) : PaymentMethodEffect()
    data class PaymentSuccess(val transactionId: String) : PaymentMethodEffect()
}

class PaymentMethodViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: String = savedStateHandle.get<String>("transactionId") ?: ""

    // UI state
    var selectedPaymentMethodId by mutableStateOf<String?>(null)
        private set

    // Available payment methods
    val paymentOptions = listOf(
        PaymentOption(
            id = "credit_card",
            title = "Credit Card",
            subtitle = "Visa ending in 4242"
        ),
        PaymentOption(
            id = "bank_transfer",
            title = "Bank Transfer", 
            subtitle = "Direct bank transfer"
        ),
        PaymentOption(
            id = "cash",
            title = "Cash on Delivery",
            subtitle = "Pay when you receive"
        )
    )

    // UI effects
    private val _effect = MutableSharedFlow<PaymentMethodEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: PaymentMethodEvent) {
        when (event) {
            is PaymentMethodEvent.SelectPaymentMethod -> {
                selectedPaymentMethodId = event.paymentMethodId
            }
            is PaymentMethodEvent.ProcessPayment -> {
                processPayment()
            }
        }
    }

    private fun processPayment() {
        viewModelScope.launch {
            if (selectedPaymentMethodId == null) {
                _effect.emit(PaymentMethodEffect.ShowError("Please select a payment method"))
                return@launch
            }

            // In a real app, this would make an API call to process the payment
            // For this example, we'll just simulate a successful payment
            try {
                // Simulate API call
                // apiService.processPayment(transactionId, selectedPaymentMethodId)
                
                _effect.emit(PaymentMethodEffect.PaymentSuccess(transactionId))
                _effect.emit(PaymentMethodEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(PaymentMethodEffect.ShowError("Payment failed: ${e.message}"))
            }
        }
    }
} 