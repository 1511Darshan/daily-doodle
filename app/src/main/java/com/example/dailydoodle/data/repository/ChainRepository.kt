package com.example.dailydoodle.data.repository

import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.model.ChainStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createChain(
        seedPrompt: String,
        creatorId: String,
        creatorName: String
    ): Result<String> {
        return try {
            val chain = Chain(
                seedPrompt = seedPrompt,
                creatorId = creatorId,
                creatorName = creatorName,
                createdAt = System.currentTimeMillis(),
                status = ChainStatus.OPEN
            )
            val docRef = firestore.collection("chains").add(chain).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChains(
        filter: ChainFilter = ChainFilter.RECENT,
        limit: Int = 20
    ): Flow<List<Chain>> = flow {
        try {
            val query = when (filter) {
                ChainFilter.RECENT -> firestore.collection("chains")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                
                ChainFilter.POPULAR -> firestore.collection("chains")
                    .orderBy("panelCount", Query.Direction.DESCENDING)
                    .orderBy("lastPanelAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                
                ChainFilter.FEATURED -> firestore.collection("chains")
                    .whereEqualTo("status", ChainStatus.OPEN)
                    .orderBy("featuredScore", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            }
            
            val snapshot = query.get().await()
            val chains = snapshot.documents.mapNotNull { Chain.fromDocument(it) }
            emit(chains)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun getChain(chainId: String): Chain? {
        return try {
            val doc = firestore.collection("chains").document(chainId).get().await()
            Chain.fromDocument(doc)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateChainPanelCount(chainId: String, increment: Int = 1) {
        try {
            val chainRef = firestore.collection("chains").document(chainId)
            firestore.runTransaction { transaction ->
                val chain = transaction.get(chainRef)
                val currentCount = chain.getLong("panelCount")?.toInt() ?: 0
                transaction.update(chainRef, "panelCount", currentCount + increment)
                transaction.update(chainRef, "lastPanelAt", System.currentTimeMillis())
            }.await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun closeChain(chainId: String) {
        try {
            firestore.collection("chains").document(chainId)
                .update("status", ChainStatus.CLOSED)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun toggleFavorite(chainId: String, userId: String): Result<Boolean> {
        return try {
            val favoriteRef = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(chainId)
            
            val doc = favoriteRef.get().await()
            val isFavorite = if (doc.exists()) {
                favoriteRef.delete().await()
                false
            } else {
                favoriteRef.set(mapOf(
                    "chainId" to chainId,
                    "addedAt" to System.currentTimeMillis()
                )).await()
                true
            }
            Result.success(isFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChain(chainId: String, userId: String): Result<Unit> {
        return try {
            // Delete the chain directly
            // TODO: Add proper authorization check once creatorId is consistently set
            firestore.collection("chains").document(chainId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isChainFavorited(chainId: String, userId: String): Boolean {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(chainId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}

enum class ChainFilter {
    RECENT,
    POPULAR,
    FEATURED
}
