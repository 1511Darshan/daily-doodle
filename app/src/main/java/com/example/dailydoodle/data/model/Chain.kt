package com.example.dailydoodle.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Chain(
    val id: String = "",
    val seedPrompt: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: ChainStatus = ChainStatus.OPEN,
    val featuredScore: Double = 0.0,
    val panelCount: Int = 0,
    val lastPanelAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Chain? {
            return document.toObject(Chain::class.java)?.copy(id = document.id)
        }
    }
}

enum class ChainStatus {
    OPEN,
    CLOSED
}
