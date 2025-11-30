package com.example.dailydoodle.data.repository

import com.example.dailydoodle.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            // Firebase is now configured with real google-services.json
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            // Provide more user-friendly error messages
            val errorMessage = when {
                e.message?.contains("API key") == true -> "Firebase API key is invalid. Please configure google-services.json"
                e.message?.contains("network") == true -> "Network error. Please check your connection."
                e.message?.contains("invalid") == true -> "Invalid email or password."
                else -> e.message ?: "An error occurred. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            // Firebase is now configured with real google-services.json
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            )?.await()

            // Create user document in Firestore
            val user = User(
                id = result.user!!.uid,
                displayName = displayName,
                email = email,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(user.id).set(user).await()

            Result.success(result.user!!)
        } catch (e: Exception) {
            // Provide more user-friendly error messages
            val errorMessage = when {
                e.message?.contains("API key") == true -> "Firebase API key is invalid. Please configure google-services.json"
                e.message?.contains("network") == true -> "Network error. Please check your connection."
                e.message?.contains("already") == true -> "An account with this email already exists."
                e.message?.contains("weak") == true -> "Password is too weak. Please use a stronger password."
                else -> e.message ?: "An error occurred. Please try again."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            
            // Create or update user document
            val user = result.user!!
            val userDoc = firestore.collection("users").document(user.uid)
            val existingUser = userDoc.get().await()
            
            if (!existingUser.exists()) {
                val newUser = User(
                    id = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    createdAt = System.currentTimeMillis()
                )
                userDoc.set(newUser).await()
            } else {
                // Update last active
                userDoc.update("lastActive", System.currentTimeMillis()).await()
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
    
    /**
     * Send email verification to current user
     */
    suspend fun sendEmailVerification() {
        currentUser?.sendEmailVerification()?.await()
    }
    
    /**
     * Reload current user to get updated email verification status
     */
    suspend fun reloadCurrentUser() {
        currentUser?.reload()?.await()
    }

    suspend fun getCurrentUserData(): User? {
        val userId = currentUserId ?: return null
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            User.fromDocument(doc)
        } catch (e: Exception) {
            null
        }
    }
}
