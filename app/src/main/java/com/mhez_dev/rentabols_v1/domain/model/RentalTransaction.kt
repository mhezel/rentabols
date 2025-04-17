package com.mhez_dev.rentabols_v1.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class RentalTransaction(
    @DocumentId
    val id: String = "",
    
    @PropertyName("itemId")
    val itemId: String = "",
    
    @PropertyName("lenderId")
    val lenderId: String = "",
    
    @PropertyName("renterId")
    val renterId: String = "",
    
    @PropertyName("startDate")
    val startDate: Long = 0L,
    
    @PropertyName("endDate")
    val endDate: Long = 0L,
    
    @PropertyName("totalPrice")
    val totalPrice: Double = 0.0,
    
    @PropertyName("status")
    val status: RentalStatus = RentalStatus.PENDING,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("metadata")
    val metadata: Map<String, Any> = emptyMap()
)
