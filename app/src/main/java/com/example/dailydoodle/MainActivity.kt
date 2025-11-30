package com.example.dailydoodle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.dailydoodle.navigation.NavGraph
import com.example.dailydoodle.navigation.Screen
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.theme.DailyDoodleTheme
import com.example.dailydoodle.ui.viewmodel.AuthViewModel
import com.example.dailydoodle.ui.viewmodel.AuthUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob
        AdMobManager.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            DailyDoodleTheme {
                DailyDoodleApp()
            }
        }
    }
}

@Composable
fun DailyDoodleApp() {
    val navController = rememberNavController()
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    val authState by authViewModel.uiState.collectAsState()

    // Show loading while determining auth state
    if (authState is AuthUiState.Initial || authState is AuthUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthUiState.Authenticated -> Screen.Feed.route
        is AuthUiState.NeedsEmailVerification -> Screen.EmailVerification.route
        else -> Screen.Onboarding.route  // Show onboarding for new/logged-out users
    }

    NavGraph(
        navController = navController,
        startDestination = startDestination
    )
}