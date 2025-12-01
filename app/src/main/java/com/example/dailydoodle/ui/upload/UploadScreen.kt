package com.example.dailydoodle.ui.upload

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Upload screen composable that displays the current upload state
 * and allows users to submit their panel artwork.
 * 
 * @param viewModel The UploadViewModel managing upload state
 * @param bitmapProvider Function that provides the bitmap to upload (from canvas)
 * @param chainId The chain ID for this panel
 * @param authorId The current user's ID
 * @param onUploadComplete Callback when upload succeeds (optional)
 * @param modifier Modifier for the composable
 */
@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    bitmapProvider: () -> Bitmap,
    chainId: String,
    authorId: String,
    onUploadComplete: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uploadState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val currentState = state) {
            is UploadState.Idle -> {
                IdleContent(
                    onSubmit = {
                        val bitmap = bitmapProvider()
                        viewModel.upload(bitmap, chainId, authorId)
                    }
                )
            }
            
            is UploadState.Uploading -> {
                UploadingContent()
            }
            
            is UploadState.Success -> {
                SuccessContent(
                    imageUrl = currentState.imageUrl,
                    thumbUrl = currentState.thumbUrl,
                    onDone = {
                        onUploadComplete?.invoke(currentState.imageUrl, currentState.thumbUrl)
                        viewModel.resetState()
                    }
                )
            }
            
            is UploadState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(onSubmit: () -> Unit) {
    Text(
        text = "Ready to share your doodle!",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(0.6f)
    ) {
        Text("Submit Panel")
    }
}

@Composable
private fun UploadingContent() {
    CircularProgressIndicator(
        modifier = Modifier.size(64.dp),
        color = MaterialTheme.colorScheme.primary
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Uploading your masterpiece...",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SuccessContent(
    imageUrl: String,
    thumbUrl: String,
    onDone: () -> Unit
) {
    Text(
        text = "🎉 Uploaded Successfully!",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Card(
        modifier = Modifier.size(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = thumbUrl,
            contentDescription = "Your uploaded panel",
            modifier = Modifier.fillMaxSize()
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Share this link:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(4.dp))
    
    Text(
        text = imageUrl,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onDone,
        modifier = Modifier.fillMaxWidth(0.6f)
    ) {
        Text("Done")
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Text(
        text = "😔 Upload Failed",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.error
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    OutlinedButton(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth(0.6f)
    ) {
        Text("Try Again")
    }
}
