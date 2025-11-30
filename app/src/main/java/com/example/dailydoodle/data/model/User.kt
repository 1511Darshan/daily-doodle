package com.example.dailydoodle.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val streak: Int = 0,
    val lastActive: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): User? {
            return document.toObject(User::class.java)?.copy(id = document.id)
        }
    }
}
