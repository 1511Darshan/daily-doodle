package com.example.dailydoodle.data.repository

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.example.dailydoodle.network.NetworkModule
import com.example.dailydoodle.network.PanelDto
import com.example.dailydoodle.network.UploadResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

/**
 * Repository for handling panel upload and retrieval operations with the backend server.
 * Converts bitmaps to WebP format and uploads via Retrofit.
 */
class UploadRepository {
    
    companion object {
        private const val TAG = "UploadRepository"
    }
    
    /**
     * Convert a Bitmap to WebP byte array for efficient upload.
     * Uses WEBP_LOSSY on API 30+ for better compression.
     * 
     * @param bitmap The bitmap to convert
     * @param quality Compression quality (0-100), default 75
     * @return ByteArray of the compressed image
     */
    private fun bitmapToWebpBytes(bitmap: Bitmap, quality: Int = 75): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        @Suppress("DEPRECATION")
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
        
        bitmap.compress(format, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Upload a panel bitmap to the backend server.
     * 
     * @param bitmap The panel image to upload
     * @param chainId The chain this panel belongs to
     * @param authorId The user ID of the panel author
     * @return Result containing UploadResponse on success, or exception on failure
     */
    suspend fun uploadPanel(
        bitmap: Bitmap,
        chainId: String,
        authorId: String
    ): Result<UploadResponse> {
        return try {
            Log.d(TAG, "Starting upload for chain: $chainId, author: $authorId")
            
            // Convert bitmap to WebP bytes
            val bytes = bitmapToWebpBytes(bitmap, quality = 75)
            Log.d(TAG, "Compressed image size: ${bytes.size} bytes")
            
            // Create multipart request body
            val requestFile = bytes.toRequestBody("image/webp".toMediaType())
            val imagePart = MultipartBody.Part.createFormData("panel", "panel.webp", requestFile)
            
            // Create text request bodies for metadata
            val chainIdBody = chainId.toRequestBody("text/plain".toMediaType())
            val authorIdBody = authorId.toRequestBody("text/plain".toMediaType())

            // Make the upload request
            val response = NetworkModule.api.uploadPanel(imagePart, chainIdBody, authorIdBody)
            Log.d(TAG, "Upload successful: ${response.imageUrl}")
            
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get all panels from the server.
     * 
     * @return Result containing list of panels on success, or exception on failure
     */
    suspend fun getPanels(): Result<List<PanelDto>> {
        return try {
            val panels = NetworkModule.api.getPanels()
            Result.success(panels)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get panels", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get panels for a specific chain.
     * 
     * @param chainId The chain ID to filter by
     * @return Result containing list of panels on success, or exception on failure
     */
    suspend fun getPanelsByChain(chainId: String): Result<List<PanelDto>> {
        return try {
            val panels = NetworkModule.api.getPanelsByChain(chainId)
            Result.success(panels)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get panels for chain: $chainId", e)
            Result.failure(e)
        }
    }
}
