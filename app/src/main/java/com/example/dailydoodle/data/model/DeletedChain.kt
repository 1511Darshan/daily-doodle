package com.example.dailydoodle.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Represents a chain that has been moved to trash.
 * Chains in trash are automatically deleted after 30 days.
 */
data class DeletedChain(
    val id: String = "",
    val originalChainId: String = "",
    val seedPrompt: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val panelCount: Int = 0,
    val deletedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + TRASH_RETENTION_DAYS * 24 * 60 * 60 * 1000L,
    val panels: List<DeletedPanel> = emptyList()
) {
    companion object {
        const val TRASH_RETENTION_DAYS = 30L
        
        fun fromDocument(document: DocumentSnapshot): DeletedChain? {
            return document.toObject(DeletedChain::class.java)?.copy(id = document.id)
        }
    }
    
    /**
     * Returns the number of days remaining before permanent deletion
     */
    fun daysUntilExpiration(): Int {
        val remainingMs = expiresAt - System.currentTimeMillis()
        return (remainingMs / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
    }
}

/**
 * Represents a panel within a deleted chain
 */
data class DeletedPanel(
    val id: String = "",
    val imageUrl: String = "",
    val authorName: String = "",
    val order: Int = 0
)
