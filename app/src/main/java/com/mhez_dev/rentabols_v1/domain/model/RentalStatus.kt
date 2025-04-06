package com.mhez_dev.rentabols_v1.domain.model

enum class RentalStatus {
    PENDING,        // Initial request state
    APPROVED,       // Request approved by lender
    REJECTED,       // Request rejected by lender
    IN_PROGRESS,    // Item is currently being rented
    COMPLETED,      // Rental period completed
    CANCELLED       // Request cancelled by renter
}
