package com.example.dailydoodle.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.data.repository.AuthRepository
import com.example.dailydoodle.di.AppModule
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.admob.BannerAdView
import com.example.dailydoodle.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onShopClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val authRepository = AppModule.authRepository

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShopClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Shop")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = currentUser?.displayName ?: "User",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                currentUser?.let { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔥 Streak: ${user.streak} days",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (AdMobManager.isAdFreeUser()) {
                                Text(
                                    text = "✨ Premium Member",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Shop button
                OutlinedButton(
                    onClick = onShopClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Visit Shop")
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        authViewModel.signOut()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }
            
            // Banner ad at bottom (per PRD: banner in profile/settings)
            BannerAdView(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
