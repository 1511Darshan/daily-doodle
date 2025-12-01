package com.example.dailydoodle.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Response from the /upload endpoint
 */
data class UploadResponse(
    val id: String,
    val imageUrl: String,
    val thumbUrl: String
)

/**
 * Panel data from the /panels endpoint
 */
data class PanelDto(
    val id: String,
    val chainId: String?,
    val authorId: String?,
    val imagePath: String?,
    val thumbPath: String?,
    val createdAt: Long?
)

/**
 * Retrofit API service for communicating with the DailyDoodle backend server
 */
interface ApiService {
    
    /**
     * Upload a panel image to the server
     * @param panel The image file as multipart form data
     * @param chainId The chain ID this panel belongs to
     * @param authorId The author's user ID
     * @return UploadResponse with URLs to the uploaded image
     */
    @Multipart
    @POST("/upload")
    suspend fun uploadPanel(
        @Part panel: MultipartBody.Part,
        @Part("chainId") chainId: RequestBody,
        @Part("authorId") authorId: RequestBody
    ): UploadResponse

    /**
     * Get all panels from the server
     * @return List of panel data
     */
    @GET("/panels")
    suspend fun getPanels(): List<PanelDto>
    
    /**
     * Get panels for a specific chain
     * @param chainId The chain ID to filter by
     * @return List of panels in the chain
     */
    @GET("/panels")
    suspend fun getPanelsByChain(@Query("chainId") chainId: String): List<PanelDto>
}
