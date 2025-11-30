package com.example.dailydoodle.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class Panel(
    val id: String = "",
    val chainId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val order: Int = 0,
    val moderationState: ModerationState = ModerationState.APPROVED
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Panel? {
            return document.toObject(Panel::class.java)?.copy(id = document.id)
        }
    }
}

enum class ModerationState {
    PENDING,
    APPROVED,
    FLAGGED,
    REMOVED
}
