package com.example.dailydoodle.ui.screen.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.data.repository.AuthRepository
import com.example.dailydoodle.data.repository.ChainRepository
import com.example.dailydoodle.di.AppModule
import com.example.dailydoodle.util.Analytics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChainScreen(
    onChainCreated: (String) -> Unit,
    onCancel: () -> Unit
) {
    var seedPrompt by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val authRepository = AppModule.authRepository
    val chainRepository = AppModule.chainRepository
    val userId = authRepository.currentUserId ?: return
    val userName = authRepository.currentUser?.displayName ?: "User"
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Chain") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Start a new chain",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = "Enter a prompt to inspire others to add panels to your chain",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = seedPrompt,
                onValueChange = { 
                    seedPrompt = it
                    errorMessage = null
                },
                label = { Text("Seed Prompt") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., A day in the life of a robot") },
                minLines = 3,
                maxLines = 5
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (seedPrompt.isBlank()) {
                        errorMessage = "Please enter a prompt"
                        return@Button
                    }
                    
                    // Check for profanity
                    val moderationRepo = AppModule.moderationRepository
                    if (moderationRepo.containsProfanity(seedPrompt)) {
                        errorMessage = "Please use appropriate language"
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        val result = chainRepository.createChain(seedPrompt, userId, userName)
                        isLoading = false
                        
                        if (result.isSuccess) {
                            val chainId = result.getOrNull()!!
                            Analytics.logEvent("chain_created", mapOf("chain_id" to chainId))
                            onChainCreated(chainId)
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to create chain"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && seedPrompt.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Chain")
                }
            }
        }
    }
}
