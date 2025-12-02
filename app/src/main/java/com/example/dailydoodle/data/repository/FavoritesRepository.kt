package com.example.dailydoodle.data.repository

import android.util.Log
import com.example.dailydoodle.data.model.Chain
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FavoritesRepository"
    }

    /**
     * Get all favorite chains for a user
     */
    fun getFavoriteChains(userId: String): Flow<List<Chain>> = flow {
        try {
            // 1. Get all favorite chain IDs
            val favoritesSnapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chainIds = favoritesSnapshot.documents.mapNotNull { doc ->
                doc.getString("chainId") ?: doc.id
            }
            
            Log.d(TAG, "Found ${chainIds.size} favorite chain IDs")
            
            if (chainIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            
            // 2. Fetch each chain (Firestore doesn't support IN queries > 10 items)
            val chains = mutableListOf<Chain>()
            for (chainId in chainIds) {
                try {
                    val chainDoc = firestore.collection("chains")
                        .document(chainId)
                        .get()
                        .await()
                    
                    if (chainDoc.exists()) {
                        Chain.fromDocument(chainDoc)?.let { chain ->
                            chains.add(chain.copy(isFavorite = true))
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch chain $chainId: ${e.message}")
                }
            }
            
            Log.d(TAG, "Fetched ${chains.size} favorite chains")
            emit(chains)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching favorite chains: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Remove a chain from favorites
     */
    suspend fun removeFromFavorites(chainId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(chainId)
                .delete()
                .await()
            
            Log.d(TAG, "Removed chain $chainId from favorites")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove from favorites: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a chain is favorited
     */
    suspend fun isFavorite(chainId: String, userId: String): Boolean {
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

    /**
     * Get the count of favorites
     */
    suspend fun getFavoritesCount(userId: String): Int {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }
}
