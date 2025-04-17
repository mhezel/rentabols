package com.mhez_dev.rentabols_v1.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class User(
    @get:Exclude
    var id: String = "",
    
    @PropertyName("email")
    val email: String = "",
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("fullName")
    val fullName: String? = null,
    
    @PropertyName("phoneNumber")
    val phoneNumber: String? = null,
    
    @PropertyName("profileImageUrl")
    val profileImageUrl: String? = null,
    
    @PropertyName("rating")
    val rating: Double = 0.0,
    
    @PropertyName("reviewCount")
    val reviewCount: Int = 0,
    
    @PropertyName("birthdate")
    val birthdate: Long? = null,
    
    @PropertyName("gender")
    val gender: String? = null,
    
    @PropertyName("location")
    val location: String? = null
)
