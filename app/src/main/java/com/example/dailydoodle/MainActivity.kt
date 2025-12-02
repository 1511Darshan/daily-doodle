package com.example.dailydoodle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dailydoodle.navigation.NavGraph
import com.example.dailydoodle.navigation.Screen
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.components.navigation.BottomAppBarProvider
import com.example.dailydoodle.ui.components.navigation.NavigationSection
import com.example.dailydoodle.ui.screen.settings.ColorPalette
import com.example.dailydoodle.ui.theme.DailyDoodleTheme
import com.example.dailydoodle.ui.theme.ThemeMode
import com.example.dailydoodle.ui.theme.ThemePreferences
import com.example.dailydoodle.ui.viewmodel.AuthViewModel
import com.example.dailydoodle.ui.viewmodel.AuthUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob
        AdMobManager.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.System)
            val colorPalette by themePreferences.colorPalette.collectAsState(initial = ColorPalette.Default)
            
            val isDarkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            
            CompositionLocalProvider(LocalThemePreferences provides themePreferences) {
                DailyDoodleTheme(
                    darkTheme = isDarkTheme,
                    colorPalette = colorPalette
                ) {
                    DailyDoodleApp()
                }
            }
        }
    }
}

val LocalThemePreferences = compositionLocalOf<ThemePreferences> { 
    error("No ThemePreferences provided") 
}

@Composable
fun DailyDoodleApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel = androidx.lifecycle.viewmodel.compose.viewModel<AuthViewModel>()
    val authState by authViewModel.uiState.collectAsState()
    
    // Track current route for bottom navigation visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Current navigation section based on route
    val currentSection = when (currentRoute) {
        Screen.Feed.route -> NavigationSection.MAIN
        Screen.Favorites.route -> NavigationSection.ARCHIVE
        Screen.Trash.route -> NavigationSection.TRASH
        else -> NavigationSection.MAIN
    }
    
    // Determine if bottom bar should be visible (only on main screens)
    val showBottomBar = currentRoute in listOf(
        Screen.Feed.route,
        Screen.Favorites.route,
        Screen.Trash.route,
    )
    
    // Google Sign-In configuration
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                authViewModel.signInWithGoogle(idToken)
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google sign-in failed: ${e.statusCode}", e)
        }
    }
    
    // Callback for initiating Google Sign-In
    val onGoogleSignIn: () -> Unit = {
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }
    
    // Callback for skipping auth (anonymous/guest mode)
    val onSkipAuth: () -> Unit = {
        // Navigate directly to feed without signing in
        navController.navigate(Screen.Feed.route) {
            popUpTo(0) { inclusive = true }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            onGoogleSignIn = onGoogleSignIn,
            onSkipAuth = onSkipAuth
        )
        
        // Bottom navigation bar - only visible on main screens
        if (showBottomBar) {
            BottomAppBarProvider(
                currentSection = currentSection,
                onSectionSelected = { section ->
                    when (section) {
                        NavigationSection.MAIN -> {
                            if (currentRoute != Screen.Feed.route) {
                                navController.navigate(Screen.Feed.route) {
                                    popUpTo(Screen.Feed.route) { inclusive = true }
                                }
                            }
                        }
                        NavigationSection.ARCHIVE -> {
                            navController.navigate(Screen.Favorites.route)
                        }
                        NavigationSection.TRASH -> {
                            navController.navigate(Screen.Trash.route)
                        }
                        NavigationSection.SETTINGS -> {
                            navController.navigate(Screen.Settings.route)
                        }
                    }
                },
                onCreateClick = {
                    navController.navigate(Screen.CreateChain.route)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}