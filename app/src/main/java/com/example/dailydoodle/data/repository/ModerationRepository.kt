package com.example.dailydoodle.data.repository

import com.example.dailydoodle.data.model.ModerationReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModerationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun submitReport(
        panelId: String,
        chainId: String,
        reporterId: String,
        reason: String
    ): Result<String> {
        return try {
            val report = ModerationReport(
                panelId = panelId,
                chainId = chainId,
                reporterId = reporterId,
                reason = reason,
                createdAt = System.currentTimeMillis()
            )
            val docRef = firestore.collection("moderation_reports").add(report).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun containsProfanity(text: String): Boolean {
        // Simple profanity filter - in production, use a more sophisticated solution
        val profanityWords = listOf(
            "badword1", "badword2" // Add actual profanity list or use MLKit
        )
        val lowerText = text.lowercase()
        return profanityWords.any { lowerText.contains(it) }
    }
}
