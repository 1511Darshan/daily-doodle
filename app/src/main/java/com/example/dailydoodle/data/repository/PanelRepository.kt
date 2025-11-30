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
            val snapshot = firestore.collection("chains")
                .document(chainId)
                .collection("panels")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val panels = snapshot.documents.mapNotNull { Panel.fromDocument(it) }
            emit(panels)
        } catch (e: Exception) {
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
}
