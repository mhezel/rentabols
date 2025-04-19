package com.mhez_dev.rentabols_v1.domain.repository

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import kotlinx.coroutines.flow.Flow

interface RentalRepository {
    // Item Management
    suspend fun createItem(item: RentalItem): Result<String>
    suspend fun updateItem(item: RentalItem): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    fun getItem(itemId: String): Flow<RentalItem?>
    suspend fun getItemById(itemId: String): RentalItem?
    fun searchItems(query: String?, category: String?, location: GeoPoint?, radius: Double?): Flow<List<RentalItem>>
    fun getItems(searchQuery: String?, category: String?): Flow<List<RentalItem>>
    fun getItemsByOwnerId(ownerId: String): Flow<List<RentalItem>>
    
    // Transaction Management
    suspend fun createRentalRequest(transaction: RentalTransaction): Result<String>
    suspend fun updateTransactionStatus(transactionId: String, status: RentalStatus): Result<Unit>
    suspend fun updateTransaction(transaction: RentalTransaction): Result<Unit>
    suspend fun getTransaction(transactionId: String): RentalTransaction?
    fun getLenderTransactions(userId: String): Flow<List<RentalTransaction>>
    fun getRenterTransactions(userId: String): Flow<List<RentalTransaction>>
    fun getLendingTransactionsForUser(userId: String): Flow<List<RentalTransaction>>
    
    // Reviews and Ratings
    suspend fun addReview(userId: String, rating: Double, review: String): Result<Unit>
}
