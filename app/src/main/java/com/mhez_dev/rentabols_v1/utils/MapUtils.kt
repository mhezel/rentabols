package com.mhez_dev.rentabols_v1.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.Locale

/**
 * Common map constants and utility functions to be used across the app
 */
object MapUtils {
    // Default coordinates for Zamboanga City, Philippines
    val ZAMBOANGA_CITY = LatLng(6.9214, 122.0790)
    
    // For compatibility with FullScreenMapScreen
    const val DEFAULT_LATITUDE = 6.9214
    const val DEFAULT_LONGITUDE = 122.0790
    const val DEFAULT_ZOOM_LEVEL = 12f
    
    // Philippines bounds
    val PHILIPPINES_NORTH_BOUND = 21.0 // Northern tip
    val PHILIPPINES_SOUTH_BOUND = 4.0  // Southern tip
    val PHILIPPINES_WEST_BOUND = 116.0 // Western edge
    val PHILIPPINES_EAST_BOUND = 127.0 // Eastern edge
    
    // Philippines boundary for map camera
    val PHILIPPINES_BOUNDS = LatLngBounds(
        LatLng(PHILIPPINES_SOUTH_BOUND, PHILIPPINES_WEST_BOUND), // Southwest corner
        LatLng(PHILIPPINES_NORTH_BOUND, PHILIPPINES_EAST_BOUND)  // Northeast corner
    )
    
    // Default zoom levels
    const val DEFAULT_ZOOM = 12f
    const val DETAIL_ZOOM = 15f
    
    // Default search radius (in km)
    const val DEFAULT_SEARCH_RADIUS = 50.0
    
    // Minimum distance (in km) to trigger a new search
    const val MIN_DISTANCE_FOR_SEARCH = 0.5
    
    /**
     * Get marker color based on item category
     */
    fun getCategoryMarkerColor(category: String?): Float {
        return when(category?.lowercase()) {
            "electronics" -> BitmapDescriptorFactory.HUE_VIOLET
            "furniture" -> BitmapDescriptorFactory.HUE_ORANGE
            "tools" -> BitmapDescriptorFactory.HUE_YELLOW
            "vehicles" -> BitmapDescriptorFactory.HUE_AZURE
            "clothing" -> BitmapDescriptorFactory.HUE_ROSE
            "books" -> BitmapDescriptorFactory.HUE_GREEN
            else -> BitmapDescriptorFactory.HUE_MAGENTA
        }
    }
    
    /**
     * Alias for getCategoryMarkerColor for compatibility with FullScreenMapScreen
     */
    fun getCategoryColor(category: String?): Float {
        return getCategoryMarkerColor(category)
    }
    
    /**
     * Check if coordinates are within the Philippines
     */
    fun isWithinPhilippines(latitude: Double, longitude: Double): Boolean {
        return latitude in PHILIPPINES_SOUTH_BOUND..PHILIPPINES_NORTH_BOUND &&
               longitude in PHILIPPINES_WEST_BOUND..PHILIPPINES_EAST_BOUND
    }
    
    /**
     * Convert address string to coordinates using Geocoder
     */
    fun getCoordinatesFromAddress(addressString: String, context: Context): LatLng? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(addressString, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                LatLng(address.latitude, address.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MapUtils", "Error geocoding address: $addressString", e)
            null
        }
    }
    
    /**
     * Calculate distance between two points in km using Haversine formula
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
    
    /**
     * Format date for display
     */
    fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        return java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
    
    /**
     * Get category color as integer value for UI components
     */
    fun getCategoryColorInt(category: String?): Int {
        return when(category?.lowercase()) {
            "electronics" -> 0xFF9C27B0.toInt() // Purple
            "furniture" -> 0xFFFF9800.toInt()   // Orange
            "tools" -> 0xFFFFEB3B.toInt()       // Yellow
            "vehicles" -> 0xFF00BCD4.toInt()    // Cyan
            "clothing" -> 0xFFE91E63.toInt()    // Pink
            "books" -> 0xFF4CAF50.toInt()       // Green
            "other" -> 0xFF9E9E9E.toInt()       // Gray
            else -> 0xFFFF5722.toInt()          // Deep Orange (default)
        }
    }
} 