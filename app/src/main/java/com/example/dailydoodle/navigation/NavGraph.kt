package com.example.dailydoodle.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dailydoodle.ui.screen.auth.AuthScreen
import com.example.dailydoodle.ui.screen.auth.EmailVerificationScreen
import com.example.dailydoodle.ui.screen.auth.ForgotPasswordScreen
import com.example.dailydoodle.ui.screen.auth.LoginScreen
import com.example.dailydoodle.ui.screen.auth.PrivacyPolicyScreen
import com.example.dailydoodle.ui.screen.auth.SignUpScreen
import com.example.dailydoodle.ui.screen.auth.TermsOfServiceScreen
import com.example.dailydoodle.ui.screen.chain.ChainViewerScreen
import com.example.dailydoodle.ui.screen.create.CreateChainScreen
import com.example.dailydoodle.ui.screen.drawing.DrawingScreen
import com.example.dailydoodle.ui.screen.feed.FeedScreen
import com.example.dailydoodle.ui.screen.onboarding.OnboardingScreen
import com.example.dailydoodle.ui.screen.profile.ProfileScreen
import com.example.dailydoodle.ui.screen.settings.AboutScreen
import com.example.dailydoodle.ui.screen.settings.AccessibilitySettingsScreen
import com.example.dailydoodle.ui.screen.settings.SettingsScreen
import com.example.dailydoodle.ui.screen.settings.ThemeSettingsScreen
import com.example.dailydoodle.ui.screen.shop.ShopScreen
import com.example.dailydoodle.ui.viewmodel.AuthUiState
import com.example.dailydoodle.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object SignUp : Screen("signup")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    object TermsOfService : Screen("terms_of_service")
    object PrivacyPolicy : Screen("privacy_policy")
    object Feed : Screen("feed")
    object ChainViewer : Screen("chain_viewer/{chainId}") {
        fun createRoute(chainId: String) = "chain_viewer/$chainId"
    }
    object Drawing : Screen("drawing/{chainId}") {
        fun createRoute(chainId: String) = "drawing/$chainId"
    }
    object CreateChain : Screen("create_chain")
    object Profile : Screen("profile")
    object Shop : Screen("shop")
    object Settings : Screen("settings")
    object SettingsTheme : Screen("settings/theme")
    object SettingsAccessibility : Screen("settings/accessibility")
    object SettingsAbout : Screen("settings/about")
}

// Spring animation spec for smooth transitions
private val springSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route,
    onGoogleSignIn: () -> Unit = {},
    onSkipAuth: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        // Slide in from right
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = springSpec
            )
        },
        // Slide out to left
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = springSpec
            )
        },
        // When going back: slide in from left
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = springSpec
            )
        },
        // When going back: slide out to right
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = springSpec
            )
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            val authViewModel: AuthViewModel = viewModel()
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val uiState by authViewModel.uiState.collectAsState()
            
            // Handle auth state changes with LaunchedEffect to prevent repeated navigation
            LaunchedEffect(uiState) {
                when (uiState) {
                    is AuthUiState.Authenticated -> {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is AuthUiState.NeedsEmailVerification -> {
                        navController.navigate(Screen.EmailVerification.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
            
            SignUpScreen(
                onSignUpClick = { displayName, email, password ->
                    authViewModel.signUpWithEmail(email, password, displayName)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onGoogleSignIn = onGoogleSignIn,
                onSkipClick = onSkipAuth,
                onTermsClick = {
                    navController.navigate(Screen.TermsOfService.route)
                },
                onPrivacyClick = {
                    navController.navigate(Screen.PrivacyPolicy.route)
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel()
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val uiState by authViewModel.uiState.collectAsState()
            
            // Handle auth state changes with LaunchedEffect to prevent repeated navigation
            LaunchedEffect(uiState) {
                when (uiState) {
                    is AuthUiState.Authenticated -> {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    is AuthUiState.NeedsEmailVerification -> {
                        navController.navigate(Screen.EmailVerification.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    else -> {}
                }
            }
            
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.signInWithEmail(email, password)
                },
                onSignUpClick = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoogleSignIn = onGoogleSignIn,
                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            val authViewModel: AuthViewModel = viewModel()
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val successMessage by authViewModel.successMessage.collectAsState()
            
            ForgotPasswordScreen(
                onSendResetEmail = { email ->
                    authViewModel.sendPasswordResetEmail(email)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = isLoading,
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        
        composable(Screen.EmailVerification.route) {
            val authViewModel: AuthViewModel = viewModel()
            val resendCooldown by authViewModel.resendCooldown.collectAsState()
            val isLoading by authViewModel.isLoading.collectAsState()
            
            EmailVerificationScreen(
                email = authViewModel.getCurrentUserEmail(),
                onResendEmail = {
                    authViewModel.resendVerificationEmail()
                },
                onContinue = {
                    if (authViewModel.checkEmailVerification()) {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.EmailVerification.route) { inclusive = true }
                        }
                    }
                },
                onChangeEmail = {
                    authViewModel.signOut()
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.EmailVerification.route) { inclusive = true }
                    }
                },
                isResending = isLoading,
                resendCooldown = resendCooldown
            )
        }
        
        composable(Screen.TermsOfService.route) {
            TermsOfServiceScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                onChainClick = { chainId ->
                    navController.navigate(Screen.ChainViewer.createRoute(chainId))
                },
                onAddPanelClick = { chainId ->
                    navController.navigate(Screen.Drawing.createRoute(chainId))
                },
                onCreateChainClick = {
                    navController.navigate(Screen.CreateChain.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.CreateChain.route) {
            CreateChainScreen(
                onChainCreated = { chainId ->
                    navController.navigate(Screen.ChainViewer.createRoute(chainId)) {
                        popUpTo(Screen.CreateChain.route) { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChainViewer.route) { backStackEntry ->
            val chainId = backStackEntry.arguments?.getString("chainId") ?: return@composable
            ChainViewerScreen(
                chainId = chainId,
                onAddPanelClick = {
                    navController.navigate(Screen.Drawing.createRoute(chainId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Drawing.route) { backStackEntry ->
            val chainId = backStackEntry.arguments?.getString("chainId") ?: return@composable
            DrawingScreen(
                chainId = chainId,
                onComplete = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onShopClick = {
                    navController.navigate(Screen.Shop.route)
                }
            )
        }
        
        composable(Screen.Shop.route) {
            ShopScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Settings screens
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onThemeClick = {
                    navController.navigate(Screen.SettingsTheme.route)
                },
                onAccessibilityClick = {
                    navController.navigate(Screen.SettingsAccessibility.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.SettingsAbout.route)
                }
            )
        }
        
        composable(Screen.SettingsTheme.route) {
            ThemeSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.SettingsAccessibility.route) {
            AccessibilitySettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.SettingsAbout.route) {
            AboutScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
