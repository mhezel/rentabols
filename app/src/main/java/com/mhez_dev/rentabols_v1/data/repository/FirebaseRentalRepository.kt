package com.mhez_dev.rentabols_v1.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRentalRepository(
    private val firestore: FirebaseFirestore
) : RentalRepository {

    // Collection constants
    private val ITEMS_COLLECTION = "rental_items"
    private val TRANSACTIONS_COLLECTION = "rental_transactions"

    override suspend fun createItem(item: RentalItem): Result<String> = try {
        val docRef = firestore.collection(ITEMS_COLLECTION).document()
        val itemWithId = item.copy(id = docRef.id)
        docRef.set(itemWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateItem(item: RentalItem): Result<Unit> = try {
        firestore.collection(ITEMS_COLLECTION).document(item.id).set(item).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = try {
        firestore.collection(ITEMS_COLLECTION).document(itemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getItem(itemId: String): Flow<RentalItem?> = callbackFlow {
        val subscription = firestore.collection(ITEMS_COLLECTION).document(itemId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val item = snapshot?.toObject(RentalItem::class.java)
                trySend(item)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getItemById(itemId: String): RentalItem? = try {
        val snapshot = firestore.collection(ITEMS_COLLECTION)
            .document(itemId)
            .get()
            .await()
        snapshot.toObject(RentalItem::class.java)
    } catch (e: Exception) {
        null
    }

    override fun getItems(searchQuery: String?, category: String?): Flow<List<RentalItem>> = callbackFlow {
        var queryRef = firestore.collection(ITEMS_COLLECTION)
            .whereEqualTo("availability", true)

        if (!category.isNullOrBlank()) {
            queryRef = queryRef.whereEqualTo("category", category)
        }

        if (!searchQuery.isNullOrBlank()) {
            queryRef = queryRef.whereArrayContains("searchKeywords", searchQuery.lowercase())
        }

        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalItem::class.java)
            } ?: emptyList()
            
            trySend(items)
        }
        awaitClose { subscription.remove() }
    }

    override fun searchItems(
        query: String?,
        category: String?,
        location: GeoPoint?,
        radius: Double?
    ): Flow<List<RentalItem>> = callbackFlow {
        var queryRef = firestore.collection(ITEMS_COLLECTION)
            .whereEqualTo("availability", true)

        if (!category.isNullOrBlank()) {
            queryRef = queryRef.whereEqualTo("category", category)
        }

        if (!query.isNullOrBlank()) {
            queryRef = queryRef.whereArrayContains("searchKeywords", query.lowercase())
        }

        // Note: For proper geolocation queries, you'd typically use a solution like GeoFirestore
        // This is a simplified version
        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalItem::class.java)
            }?.filter { item ->
                if (location != null && radius != null) {
                    // Simple distance calculation (not accurate for large distances)
                    val distance = calculateDistance(
                        location.latitude, location.longitude,
                        item.location.latitude, item.location.longitude
                    )
                    distance <= radius
                } else true
            } ?: emptyList()
            
            trySend(items)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun createRentalRequest(transaction: RentalTransaction): Result<String> = try {
        val docRef = firestore.collection(TRANSACTIONS_COLLECTION).document()
        val transactionWithId = transaction.copy(id = docRef.id)
        docRef.set(transactionWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTransactionStatus(
        transactionId: String,
        status: RentalStatus
    ): Result<Unit> = try {
        firestore.collection(TRANSACTIONS_COLLECTION).document(transactionId)
            .update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLenderTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        val queryRef = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("lenderId", userId)
        
        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalTransaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }
        awaitClose { subscription.remove() }
    }

    override fun getRenterTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        val queryRef = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("renterId", userId)
        
        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalTransaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addReview(userId: String, rating: Double, review: String): Result<Unit> {
        // Implement review functionality
        return Result.success(Unit)
    }

    override fun getItemsByOwnerId(ownerId: String): Flow<List<RentalItem>> = callbackFlow {
        val queryRef = firestore.collection(ITEMS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
        
        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val items = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalItem::class.java)
            } ?: emptyList()
            
            trySend(items)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateTransaction(transaction: RentalTransaction): Result<Unit> = try {
        firestore.collection(TRANSACTIONS_COLLECTION)
            .document(transaction.id)
            .set(transaction)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLendingTransactionsForUser(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        val queryRef = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("lenderId", userId)
            .whereIn("status", listOf(
                RentalStatus.APPROVED.name,
                RentalStatus.IN_PROGRESS.name,
                RentalStatus.COMPLETED.name
            ))
        
        val subscription = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val transactions = snapshot?.documents?.mapNotNull { 
                it.toObject(RentalTransaction::class.java)
            } ?: emptyList()
            
            trySend(transactions)
        }
        awaitClose { subscription.remove() }
    }

    // Helper function for distance calculation
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
