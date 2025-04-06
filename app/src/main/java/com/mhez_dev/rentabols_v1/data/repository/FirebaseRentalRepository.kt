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

    override suspend fun createItem(item: RentalItem): Result<String> = try {
        val docRef = firestore.collection("items").document()
        val itemWithId = item.copy(id = docRef.id)
        docRef.set(itemWithId).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateItem(item: RentalItem): Result<Unit> = try {
        firestore.collection("items").document(item.id).set(item).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = try {
        firestore.collection("items").document(itemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getItem(itemId: String): Flow<RentalItem?> = callbackFlow {
        val subscription = firestore.collection("items").document(itemId)
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

    override fun searchItems(
        query: String?,
        category: String?,
        location: GeoPoint?,
        radius: Double?
    ): Flow<List<RentalItem>> = callbackFlow {
        var queryRef = firestore.collection("items")
            .whereEqualTo("availability", true)

        if (!category.isNullOrBlank()) {
            queryRef = queryRef.whereEqualTo("category", category)
        }

        if (!query.isNullOrBlank()) {
            queryRef = queryRef.orderBy("title")
                .startAt(query)
                .endAt(query + '\uf8ff')
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
        val docRef = firestore.collection("transactions").document()
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
        firestore.collection("transactions").document(transactionId)
            .update("status", status).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLenderTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        val subscription = firestore.collection("transactions")
            .whereEqualTo("lenderId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(RentalTransaction::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { subscription.remove() }
    }

    override fun getRenterTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        val subscription = firestore.collection("transactions")
            .whereEqualTo("renterId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(RentalTransaction::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addReview(userId: String, rating: Double, review: String): Result<Unit> = try {
        // Update user's rating and review count
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val currentRating = userDoc.getDouble("rating") ?: 0.0
            val currentCount = userDoc.getLong("reviewCount")?.toInt() ?: 0
            
            val newCount = currentCount + 1
            val newRating = ((currentRating * currentCount) + rating) / newCount
            
            transaction.update(userRef, mapOf(
                "rating" to newRating,
                "reviewCount" to newCount
            ))
        }.await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getItems(searchQuery: String?, category: String?): Flow<List<RentalItem>> = callbackFlow {
        val collectionRef = firestore.collection("items")
        var query: Query = collectionRef

        // Apply category filter
        if (!category.isNullOrBlank()) {
            query = query.whereEqualTo("category", category)
        }

        // Apply search query - simple prefix search
        if (!searchQuery.isNullOrBlank()) {
            query = query.orderBy("title")
                .startAt(searchQuery)
                .endAt(searchQuery + '\uf8ff')
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                val item = doc.toObject(RentalItem::class.java)
                if (item != null) {
                    item.id = doc.id
                    item
                } else null
            } ?: emptyList()
            trySend(items)
        }

        awaitClose { subscription.remove() }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
