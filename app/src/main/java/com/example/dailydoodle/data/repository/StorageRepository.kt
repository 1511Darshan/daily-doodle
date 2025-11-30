package com.example.dailydoodle.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val context: Context
) {
    suspend fun uploadPanelImage(
        bitmap: Bitmap,
        userId: String,
        chainId: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Compress bitmap to WebP
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.WEBP, 85, outputStream)
                val imageBytes = outputStream.toByteArray()

                // Validate image size (max 2MB)
                if (imageBytes.size > 2 * 1024 * 1024) {
                    return@withContext Result.failure(Exception("Image is too large. Maximum size is 2MB."))
                }

                // Create panels directory in app's internal storage
                val panelsDir = File(context.filesDir, "panels/$chainId")
                if (!panelsDir.exists()) {
                    panelsDir.mkdirs()
                }

                // Generate unique filename
                val filename = "${UUID.randomUUID()}.webp"
                val imageFile = File(panelsDir, filename)

                // Save bitmap to file
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                }

                // Return file path that can be used to load the image
                // Using FileProvider for secure file access
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )

                Result.success(uri.toString())
            } catch (e: Exception) {
                Result.failure(Exception("Failed to save image: ${e.message}"))
            }
        }
    }

    suspend fun uploadImageFromUri(
        uri: Uri,
        userId: String,
        chainId: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // For now, just return the URI as-is
                // In the future, you might want to copy it to internal storage
                Result.success(uri.toString())
            } catch (e: Exception) {
                Result.failure(Exception("Failed to process image: ${e.message}"))
            }
        }
    }

    fun getImageUri(imagePath: String): Uri? {
        return try {
            if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
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
}
