package com.example.dailydoodle.data.repository

import android.util.Log
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.model.ChainStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChainRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ChainRepository"
    }

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
                    .limit(limit.toLong())
                
                ChainFilter.FEATURED -> firestore.collection("chains")
                    .whereEqualTo("status", ChainStatus.OPEN.name)
                    .orderBy("featuredScore", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
            }
            
            val snapshot = query.get().await()
            val chains = snapshot.documents.mapNotNull { doc ->
                Chain.fromDocument(doc)
            }
            android.util.Log.d("ChainRepository", "Fetched ${chains.size} chains for filter $filter")
            chains.forEach { chain ->
                android.util.Log.d("ChainRepository", "Chain: ${chain.id}, creator: ${chain.creatorId}, name: ${chain.creatorName}")
            }
            emit(chains)
        } catch (e: Exception) {
            android.util.Log.e("ChainRepository", "Error fetching chains: ${e.message}", e)
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
            Log.d(TAG, "Deleting chain: $chainId and all its panels/images")
            
            // 1. Get all panels to collect image URLs
            val panelsSnapshot = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .get()
                .await()
            
            val imageUrls = panelsSnapshot.documents.mapNotNull { doc ->
                doc.getString("imageUrl")
            }
            Log.d(TAG, "Found ${imageUrls.size} images to delete")
            
            // 2. Delete all images from Firebase Storage
            for (imageUrl in imageUrls) {
                if (imageUrl.isNotEmpty() && imageUrl.startsWith("https://")) {
                    try {
                        val imageRef = storage.getReferenceFromUrl(imageUrl)
                        imageRef.delete().await()
                        Log.d(TAG, "Deleted image: $imageUrl")
                    } catch (e: Exception) {
                        // Log but continue - don't fail entire operation for one image
                        Log.w(TAG, "Failed to delete image $imageUrl: ${e.message}")
                    }
                }
            }
            
            // 3. Delete all panel documents
            for (doc in panelsSnapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "Deleted ${panelsSnapshot.documents.size} panel documents")
            
            // 4. Delete the chain document
            firestore.collection("chains").document(chainId).delete().await()
            Log.d(TAG, "Chain deleted successfully")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete chain: ${e.message}", e)
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
