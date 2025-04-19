package com.mhez_dev.rentabols_v1.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.mhez_dev.rentabols_v1.domain.model.RentalItem
import com.mhez_dev.rentabols_v1.domain.model.RentalStatus
import com.mhez_dev.rentabols_v1.domain.model.RentalTransaction
import com.mhez_dev.rentabols_v1.domain.repository.RentalRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreRentalRepository(
    private val firestore: FirebaseFirestore
) : RentalRepository {

    override fun getItems(
        searchQuery: String?,
        category: String?
    ): Flow<List<RentalItem>> = callbackFlow {
        val collectionRef = firestore.collection("rental_items")
        var query: Query = collectionRef

        // Apply category filter
        if (!category.isNullOrBlank()) {
            query = query.whereEqualTo("category", category)
        }

        // Apply search query
        if (!searchQuery.isNullOrBlank()) {
            query = query.whereArrayContains("searchKeywords", searchQuery.lowercase())
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RentalItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(items)
        }

        awaitClose { subscription.remove() }
    }
    
    override suspend fun createItem(item: RentalItem): Result<String> = try {
        val docRef = firestore.collection("rental_items").document()
        val searchKeywords = generateSearchKeywords(item.title)
        item.copy(
            id = docRef.id,
            searchKeywords = searchKeywords
        ).let { itemWithId ->
            docRef.set(itemWithId).await()
            Result.success(docRef.id)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateItem(item: RentalItem): Result<Unit> = try {
        val searchKeywords = generateSearchKeywords(item.title)
        firestore.collection("rental_items")
            .document(item.id)
            .set(item.copy(searchKeywords = searchKeywords))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun generateSearchKeywords(text: String): List<String> {
        val keywords = mutableSetOf<String>()
        val words = text.split(" ").filter { it.isNotBlank() }

        // Add full text
        keywords.add(text.lowercase())

        // Add individual words
        keywords.addAll(words.map { it.lowercase() })

        // Add partial matches (for autocomplete)
        words.forEach { word ->
            for (i in 1..word.length) {
                keywords.add(word.substring(0, i).lowercase())
            }
        }

        return keywords.toList()
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = try {
        firestore.collection("rental_items")
            .document(itemId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getItem(itemId: String): Flow<RentalItem?> = flow {
        try {
            val snapshot = firestore.collection("rental_items")
                .document(itemId)
                .get()
                .await()
            emit(snapshot.toObject(RentalItem::class.java))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getItemById(itemId: String): RentalItem? = try {
        val snapshot = firestore.collection("rental_items")
            .document(itemId)
            .get()
            .await()
        snapshot.toObject(RentalItem::class.java)
    } catch (e: Exception) {
        null
    }

    override fun searchItems(
        query: String?,
        category: String?,
        location: GeoPoint?,
        radius: Double?
    ): Flow<List<RentalItem>> = flow {
        try {
            // For now, just emit an empty list since we haven't added any items yet
            emit(emptyList())
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun createRentalRequest(transaction: RentalTransaction): Result<String> = try {
        val docRef = firestore.collection("rental_transactions").document()
        transaction.copy(id = docRef.id).let { transactionWithId ->
            docRef.set(transactionWithId).await()
            Result.success(docRef.id)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateTransactionStatus(
        transactionId: String,
        status: RentalStatus
    ): Result<Unit> = try {
        firestore.collection("rental_transactions")
            .document(transactionId)
            .update("status", status)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLenderTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        try {
            val queryRef = firestore.collection("rental_transactions")
                .whereEqualTo("lenderId", userId)
            
            val subscription = queryRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val transaction = doc.toObject(RentalTransaction::class.java)
                        transaction?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(transactions)
            }
            
            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }

    override fun getRenterTransactions(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        try {
            val queryRef = firestore.collection("rental_transactions")
                .whereEqualTo("renterId", userId)
            
            val subscription = queryRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val transaction = doc.toObject(RentalTransaction::class.java)
                        transaction?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(transactions)
            }
            
            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun addReview(
        userId: String,
        rating: Double,
        review: String
    ): Result<Unit> = try {
        firestore.collection("user_reviews")
            .add(mapOf(
                "userId" to userId,
                "rating" to rating,
                "review" to review,
                "timestamp" to Timestamp.now()
            ))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getItemsByOwnerId(ownerId: String): Flow<List<RentalItem>> = callbackFlow {
        val queryRef = firestore.collection("rental_items")
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
        firestore.collection("rental_transactions")
            .document(transaction.id)
            .set(transaction)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getLendingTransactionsForUser(userId: String): Flow<List<RentalTransaction>> = callbackFlow {
        try {
            // Get transactions where user is the lender
            val query = firestore.collection("rental_transactions")
                .whereEqualTo("lenderId", userId)
                .whereIn("status", listOf(
                    RentalStatus.APPROVED.name,
                    RentalStatus.IN_PROGRESS.name,
                    RentalStatus.COMPLETED.name
                ))
            
            val subscription = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val transaction = doc.toObject(RentalTransaction::class.java)
                        transaction?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(transactions)
            }
            
            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun getTransaction(transactionId: String): RentalTransaction? = try {
        val snapshot = firestore.collection("rental_transactions")
            .document(transactionId)
            .get()
            .await()
        snapshot.toObject(RentalTransaction::class.java)?.copy(id = snapshot.id)
    } catch (e: Exception) {
        null
    }
}
