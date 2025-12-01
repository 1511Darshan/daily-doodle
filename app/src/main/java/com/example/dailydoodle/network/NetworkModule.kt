package com.example.dailydoodle.network

import com.example.dailydoodle.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton network module providing configured Retrofit API instance.
 * 
 * The BASE_URL is set via BuildConfig - update it in app/build.gradle.kts:
 * - For ngrok: "https://abcd-1234.ngrok.io"
 * - For production: "https://api.yourdomain.com/"
 */
object NetworkModule {
    
    // Logging interceptor for debugging network calls
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // OkHttp client with logging and timeout configuration
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        // Timeouts for upload operations (images can take time)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // Uncomment to add API key authentication:
        // .addInterceptor { chain ->
        //     val request = chain.request().newBuilder()
        //         .addHeader("x-api-key", "YOUR_API_KEY_HERE")
        //         .build()
        //     chain.proceed(request)
        // }
        .build()

    // Moshi instance with Kotlin support
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Retrofit API service instance.
     * Use this to make network calls to the backend server.
     */
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
