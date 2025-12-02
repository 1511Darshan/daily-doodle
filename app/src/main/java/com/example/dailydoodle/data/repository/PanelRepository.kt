package com.example.dailydoodle.data.repository

import com.example.dailydoodle.data.model.Panel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PanelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun addPanel(
        chainId: String,
        authorId: String,
        authorName: String,
        imageUrl: String,
        caption: String = ""
    ): Result<String> {
        return try {
            val chainRef = firestore.collection("chains").document(chainId)
            
            // Check if chain exists first
            val chainDoc = chainRef.get().await()
            if (!chainDoc.exists()) {
                return Result.failure(Exception("Chain not found. Please create the chain first."))
            }

            // Get current panel count for order
            val currentOrder = chainDoc.getLong("panelCount")?.toInt() ?: 0

            val panel = Panel(
                chainId = chainId,
                authorId = authorId,
                authorName = authorName,
                imageUrl = imageUrl,
                caption = caption,
                createdAt = System.currentTimeMillis(),
                order = currentOrder
            )

            // Add panel to subcollection
            val docRef = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .add(panel)
                .await()

            // Update chain panel count using transaction
            firestore.runTransaction { transaction ->
                val chainDocSnapshot = transaction.get(chainRef)
                if (!chainDocSnapshot.exists()) {
                    throw Exception("Chain no longer exists")
                }
                val count = chainDocSnapshot.getLong("panelCount")?.toInt() ?: 0
                transaction.update(chainRef, "panelCount", count + 1)
                transaction.update(chainRef, "lastPanelAt", System.currentTimeMillis())
            }.await()

            // Increment user's doodle count
            val userRef = firestore.collection("users").document(authorId)
            firestore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                if (userDoc.exists()) {
                    val currentDoodleCount = userDoc.getLong("panelCount")?.toInt() ?: 0
                    transaction.update(userRef, "panelCount", currentDoodleCount + 1)
                    transaction.update(userRef, "lastActive", System.currentTimeMillis())
                } else {
                    // Create user document if it doesn't exist
                    val userData = hashMapOf(
                        "panelCount" to 1,
                        "streak" to 0,
                        "lastActive" to System.currentTimeMillis(),
                        "createdAt" to System.currentTimeMillis()
                    )
                    transaction.set(userRef, userData)
                }
            }.await()
            
            android.util.Log.d("PanelRepository", "Panel added and user doodle count incremented for $authorId")

            Result.success(docRef.id)
        } catch (e: Exception) {
            // Provide user-friendly error messages
            val errorMessage = when {
                e.message?.contains("not found") == true || 
                e.message?.contains("does not exist") == true -> 
                    "Chain not found. The chain may have been deleted."
                e.message?.contains("permission") == true -> 
                    "Permission denied. Please check your Firestore security rules."
                e.message?.contains("network") == true -> 
                    "Network error. Please check your connection."
                else -> e.message ?: "Failed to add panel. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    fun getPanelsForChain(chainId: String): Flow<List<Panel>> = flow {
        try {
            android.util.Log.d("PanelRepository", "Fetching panels for chain: $chainId")
            val snapshot = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            android.util.Log.d("PanelRepository", "Found ${snapshot.documents.size} panel documents")
            val panels = snapshot.documents.mapNotNull { doc ->
                val panel = Panel.fromDocument(doc)
                android.util.Log.d("PanelRepository", "Panel: ${panel?.id}, imageUrl: ${panel?.imageUrl}")
                panel
            }
            android.util.Log.d("PanelRepository", "Emitting ${panels.size} panels")
            emit(panels)
        } catch (e: Exception) {
            android.util.Log.e("PanelRepository", "Error fetching panels: ${e.message}", e)
            emit(emptyList())
        }
    }

    suspend fun getPanel(chainId: String, panelId: String): Panel? {
        return try {
            val doc = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .document(panelId)
                .get()
                .await()
            Panel.fromDocument(doc)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete a panel from Firestore.
     * Returns the imageUrl so it can be deleted from Storage.
     */
    suspend fun deletePanel(chainId: String, panelId: String): Result<String> {
        return try {
            android.util.Log.d("PanelRepository", "Deleting panel: $panelId from chain: $chainId")
            
            // Get the panel first to retrieve the imageUrl
            val panel = getPanel(chainId, panelId)
            val imageUrl = panel?.imageUrl ?: ""
            
            // Delete the panel document
            firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .document(panelId)
                .delete()
                .await()
            
            // Decrement panel count on the chain
            val chainRef = firestore.collection("chains").document(chainId)
            firestore.runTransaction { transaction ->
                val chainDoc = transaction.get(chainRef)
                if (chainDoc.exists()) {
                    val count = chainDoc.getLong("panelCount")?.toInt() ?: 1
                    transaction.update(chainRef, "panelCount", maxOf(0, count - 1))
                }
            }.await()
            
            android.util.Log.d("PanelRepository", "Panel deleted successfully")
            Result.success(imageUrl)
        } catch (e: Exception) {
            android.util.Log.e("PanelRepository", "Failed to delete panel: ${e.message}", e)
            Result.failure(Exception("Failed to delete panel: ${e.message}"))
        }
    }

    /**
     * Delete all panels for a chain.
     * Returns list of imageUrls so they can be deleted from Storage.
     */
    suspend fun deleteAllPanelsForChain(chainId: String): Result<List<String>> {
        return try {
            android.util.Log.d("PanelRepository", "Deleting all panels for chain: $chainId")
            
            val snapshot = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .get()
                .await()
            
            val imageUrls = mutableListOf<String>()
            
            for (doc in snapshot.documents) {
                val imageUrl = doc.getString("imageUrl") ?: ""
                if (imageUrl.isNotEmpty()) {
                    imageUrls.add(imageUrl)
                }
                doc.reference.delete().await()
            }
            
            android.util.Log.d("PanelRepository", "Deleted ${snapshot.documents.size} panels")
            Result.success(imageUrls)
        } catch (e: Exception) {
            android.util.Log.e("PanelRepository", "Failed to delete panels: ${e.message}", e)
            Result.failure(Exception("Failed to delete panels: ${e.message}"))
        }
    }
}
