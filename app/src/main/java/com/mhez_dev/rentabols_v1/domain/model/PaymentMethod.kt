package com.mhez_dev.rentabols_v1.domain.model

enum class PaymentMethod {
    CASH_ON_PICKUP,
    E_WALLET,
    CREDIT_CARD,
    DEBIT_CARD;
    
    fun getDisplayName(): String {
        return when (this) {
            CASH_ON_PICKUP -> "Cash on Pickup"
            E_WALLET -> "E-Wallet"
            CREDIT_CARD -> "Credit Card"
            DEBIT_CARD -> "Debit Card"
        }
    }
    
    companion object {
        fun fromString(value: String): PaymentMethod {
            return when (value.uppercase()) {
                "CASH_ON_PICKUP" -> CASH_ON_PICKUP
                "E_WALLET" -> E_WALLET
                "CREDIT_CARD" -> CREDIT_CARD
                "DEBIT_CARD" -> DEBIT_CARD
                else -> CASH_ON_PICKUP // Default
            }
        }
    }
} 