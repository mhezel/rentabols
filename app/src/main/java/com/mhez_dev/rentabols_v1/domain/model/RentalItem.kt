package com.mhez_dev.rentabols_v1.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class RentalItem(
    @DocumentId
    var id: String = "",
    
    @PropertyName("ownerId")
    val ownerId: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("category")
    val category: String = "",
    
    @PropertyName("pricePerDay")
    val pricePerDay: Double = 0.0,
    
    @PropertyName("location")
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    
    @PropertyName("address")
    val address: String = "",
    
    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),
    
    @PropertyName("availability")
    val availability: Boolean = true,
    
    @PropertyName("rating")
    val rating: Double = 0.0,
    
    @PropertyName("reviewCount")
    val reviewCount: Int = 0,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @PropertyName("searchKeywords")
    val searchKeywords: List<String> = emptyList(),
    
    @PropertyName("metadata")
    val metadata: Map<String, Any> = mapOf()
)
