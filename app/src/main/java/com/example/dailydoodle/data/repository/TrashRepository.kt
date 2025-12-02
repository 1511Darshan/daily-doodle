package com.example.dailydoodle.data.repository

import android.util.Log
import com.example.dailydoodle.data.model.DeletedChain
import com.example.dailydoodle.data.model.DeletedPanel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "TrashRepository"
    }

    /**
     * Move a chain to trash instead of permanently deleting it
     */
    suspend fun moveToTrash(chainId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Moving chain $chainId to trash")
            
            // 1. Get the chain data
            val chainDoc = firestore.collection("chains").document(chainId).get().await()
            if (!chainDoc.exists()) {
                return Result.failure(Exception("Chain not found"))
            }
            
            val seedPrompt = chainDoc.getString("seedPrompt") ?: ""
            val creatorId = chainDoc.getString("creatorId") ?: ""
            val creatorName = chainDoc.getString("creatorName") ?: ""
            val panelCount = chainDoc.getLong("panelCount")?.toInt() ?: 0
            
            // 2. Get all panels
            val panelsSnapshot = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .orderBy("order")
                .get()
                .await()
            
            val panels = panelsSnapshot.documents.map { doc ->
                DeletedPanel(
                    id = doc.id,
                    imageUrl = doc.getString("imageUrl") ?: "",
                    authorName = doc.getString("authorName") ?: "",
                    order = doc.getLong("order")?.toInt() ?: 0
                )
            }
            
            // 3. Create deleted chain in user's trash
            val deletedChain = DeletedChain(
                originalChainId = chainId,
                seedPrompt = seedPrompt,
                creatorId = creatorId,
                creatorName = creatorName,
                panelCount = panelCount,
                deletedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + DeletedChain.TRASH_RETENTION_DAYS * 24 * 60 * 60 * 1000L,
                panels = panels
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("trash")
                .add(deletedChain)
                .await()
            
            // 4. Delete original chain panels
            for (doc in panelsSnapshot.documents) {
                doc.reference.delete().await()
            }
            
            // 5. Delete the chain document
            firestore.collection("chains").document(chainId).delete().await()
            
            Log.d(TAG, "Chain moved to trash successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move chain to trash: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all chains in user's trash
     */
    fun getTrashChains(userId: String): Flow<List<DeletedChain>> = flow {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("trash")
                .orderBy("deletedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chains = snapshot.documents.mapNotNull { doc ->
                DeletedChain.fromDocument(doc)
            }
            Log.d(TAG, "Fetched ${chains.size} trash chains")
            emit(chains)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trash chains: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Restore a chain from trash
     */
    suspend fun restoreFromTrash(deletedChainId: String, userId: String): Result<String> {
        return try {
            Log.d(TAG, "Restoring chain $deletedChainId from trash")
            
            // 1. Get the deleted chain
            val trashDoc = firestore.collection("users")
                .document(userId)
                .collection("trash")
                .document(deletedChainId)
                .get()
                .await()
            
            if (!trashDoc.exists()) {
                return Result.failure(Exception("Deleted chain not found"))
            }
            
            val deletedChain = DeletedChain.fromDocument(trashDoc)
                ?: return Result.failure(Exception("Failed to parse deleted chain"))
            
            // 2. Create new chain
            val chainData = mapOf(
                "seedPrompt" to deletedChain.seedPrompt,
                "creatorId" to deletedChain.creatorId,
                "creatorName" to deletedChain.creatorName,
                "createdAt" to System.currentTimeMillis(),
                "status" to "OPEN",
                "panelCount" to deletedChain.panelCount,
                "lastPanelAt" to System.currentTimeMillis()
            )
            
            val newChainRef = firestore.collection("chains").add(chainData).await()
            val newChainId = newChainRef.id
            
            // 3. Restore panels
            for (panel in deletedChain.panels) {
                val panelData = mapOf(
                    "chainId" to newChainId,
                    "imageUrl" to panel.imageUrl,
                    "authorName" to panel.authorName,
                    "order" to panel.order,
                    "createdAt" to System.currentTimeMillis()
                )
                newChainRef.collection("panels").add(panelData).await()
            }
            
            // 4. Delete from trash
            trashDoc.reference.delete().await()
            
            Log.d(TAG, "Chain restored successfully with new ID: $newChainId")
            Result.success(newChainId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore chain: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Permanently delete a chain from trash
     */
    suspend fun permanentlyDelete(deletedChainId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Permanently deleting chain $deletedChainId")
            
            // 1. Get the deleted chain to get image URLs
            val trashDoc = firestore.collection("users")
                .document(userId)
                .collection("trash")
                .document(deletedChainId)
                .get()
                .await()
            
            if (trashDoc.exists()) {
                val deletedChain = DeletedChain.fromDocument(trashDoc)
                
                // 2. Delete images from storage
                deletedChain?.panels?.forEach { panel ->
                    if (panel.imageUrl.isNotEmpty() && panel.imageUrl.startsWith("https://")) {
                        try {
                            val imageRef = storage.getReferenceFromUrl(panel.imageUrl)
                            imageRef.delete().await()
                            Log.d(TAG, "Deleted image: ${panel.imageUrl}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to delete image: ${e.message}")
                        }
                    }
                }
                
                // 3. Delete from trash collection
                trashDoc.reference.delete().await()
            }
            
            Log.d(TAG, "Chain permanently deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to permanently delete chain: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Empty all items from trash
     */
    suspend fun emptyTrash(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Emptying trash for user $userId")
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("trash")
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                permanentlyDelete(doc.id, userId)
            }
            
            Log.d(TAG, "Trash emptied successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to empty trash: ${e.message}", e)
            Result.failure(e)
        }
    }
}
