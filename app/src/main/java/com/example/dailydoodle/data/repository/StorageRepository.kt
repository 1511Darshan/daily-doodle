package com.example.dailydoodle.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val context: Context
) {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    companion object {
        private const val TAG = "StorageRepository"
    }

    /**
     * Upload panel image to Firebase Storage and return the download URL.
     * This URL is publicly accessible by all users.
     */
    suspend fun uploadPanelImage(
        bitmap: Bitmap,
        userId: String,
        chainId: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Resize bitmap if too large (max 1080px on longest side)
                val maxDimension = 1080
                val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                    val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                    val newWidth = (bitmap.width * scale).toInt()
                    val newHeight = (bitmap.height * scale).toInt()
                    Log.d(TAG, "Resizing bitmap from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")
                    Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                } else {
                    bitmap
                }

                // Compress bitmap to JPEG (faster than WebP, good compression)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val imageBytes = outputStream.toByteArray()
                
                Log.d(TAG, "Compressed image size: ${imageBytes.size / 1024} KB")

                // Validate image size (max 2MB)
                if (imageBytes.size > 2 * 1024 * 1024) {
                    return@withContext Result.failure(Exception("Image is too large. Maximum size is 2MB."))
                }

                // Generate unique filename
                val filename = "${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child("panels/$chainId/$filename")

                Log.d(TAG, "Uploading to Firebase Storage: panels/$chainId/$filename")

                // Upload to Firebase Storage
                val uploadTask = imageRef.putBytes(imageBytes).await()
                Log.d(TAG, "Upload task completed. Getting download URL...")
                
                // Get the download URL - this returns an https:// URL
                val downloadUrl = imageRef.downloadUrl.await()
                val urlString = downloadUrl.toString()
                
                Log.d(TAG, "Upload successful!")
                Log.d(TAG, "Download URL: $urlString")
                
                // Verify we got an https URL, not a gs:// URL
                if (!urlString.startsWith("https://")) {
                    Log.e(TAG, "ERROR: Got invalid URL format: $urlString")
                    return@withContext Result.failure(Exception("Invalid URL format returned from Firebase"))
                }

                Result.success(urlString)
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed: ${e.message}", e)
                Result.failure(Exception("Failed to upload image: ${e.message}"))
            }
        }
    }

    /**
     * Upload image from URI to Firebase Storage.
     */
    suspend fun uploadImageFromUri(
        uri: Uri,
        userId: String,
        chainId: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val filename = "${UUID.randomUUID()}.webp"
                val imageRef = storageRef.child("panels/$chainId/$filename")

                Log.d(TAG, "Uploading from URI to Firebase Storage: panels/$chainId/$filename")

                // Upload to Firebase Storage
                imageRef.putFile(uri).await()
                
                // Get the download URL
                val downloadUrl = imageRef.downloadUrl.await().toString()
                
                Log.d(TAG, "Upload successful! URL: $downloadUrl")

                Result.success(downloadUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Upload from URI failed: ${e.message}", e)
                Result.failure(Exception("Failed to upload image: ${e.message}"))
            }
        }
    }

    fun getImageUri(imagePath: String): Uri? {
        return try {
            if (imagePath.startsWith("content://") || imagePath.startsWith("file://") || imagePath.startsWith("https://")) {
                Uri.parse(imagePath)
            } else {
                // Assume it's a file path
                val file = File(imagePath)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete an image from Firebase Storage using its download URL.
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUrl.isEmpty()) {
                    return@withContext Result.success(Unit)
                }
                
                Log.d(TAG, "Deleting image from Storage: $imageUrl")
                
                // Get reference from URL
                val imageRef = storage.getReferenceFromUrl(imageUrl)
                imageRef.delete().await()
                
                Log.d(TAG, "Image deleted successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete image: ${e.message}", e)
                // Don't fail if image doesn't exist - it may have been deleted already
                if (e.message?.contains("does not exist") == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete image: ${e.message}"))
                }
            }
        }
    }

    /**
     * Delete multiple images from Firebase Storage.
     */
    suspend fun deleteImages(imageUrls: List<String>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Deleting ${imageUrls.size} images from Storage")
                
                for (url in imageUrls) {
                    if (url.isNotEmpty()) {
                        try {
                            val imageRef = storage.getReferenceFromUrl(url)
                            imageRef.delete().await()
                        } catch (e: Exception) {
                            // Log but continue - don't fail entire operation for one image
                            Log.w(TAG, "Failed to delete image $url: ${e.message}")
                        }
                    }
                }
                
                Log.d(TAG, "Images deleted successfully")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete images: ${e.message}", e)
                Result.failure(Exception("Failed to delete images: ${e.message}"))
            }
        }
    }
}
