package com.example.dailydoodle.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailydoodle.data.model.User
import com.example.dailydoodle.data.repository.AuthRepository
import com.example.dailydoodle.di.AppModule
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AppModule.authRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Extended state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()
    
    private val _resendCooldown = MutableStateFlow(0)
    val resendCooldown: StateFlow<Int> = _resendCooldown.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepository.currentUser
        if (user != null) {
            _uiState.value = AuthUiState.Authenticated
            _isEmailVerified.value = user.isEmailVerified
            loadUserData()
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signInWithEmail(email, password)
            _isLoading.value = false
            
            _uiState.value = when {
                result.isSuccess -> {
                    com.example.dailydoodle.util.Analytics.logSignIn("email")
                    loadUserData()
                    _isEmailVerified.value = authRepository.currentUser?.isEmailVerified == true
                    AuthUiState.Authenticated
                }
                else -> {
                    val error = mapFirebaseError(result.exceptionOrNull())
                    _errorMessage.value = error
                    AuthUiState.Error(error)
                }
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signUpWithEmail(email, password, displayName)
            _isLoading.value = false
            
            _uiState.value = when {
                result.isSuccess -> {
                    com.example.dailydoodle.util.Analytics.logSignUp("email")
                    // Send verification email
                    sendVerificationEmail()
                    loadUserData()
                    _isEmailVerified.value = false
                    AuthUiState.NeedsEmailVerification
                }
                else -> {
                    val error = mapFirebaseError(result.exceptionOrNull())
                    _errorMessage.value = error
                    AuthUiState.Error(error)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signInWithGoogle(idToken)
            _isLoading.value = false
            
            _uiState.value = when {
                result.isSuccess -> {
                    com.example.dailydoodle.util.Analytics.logSignIn("google")
                    loadUserData()
                    _isEmailVerified.value = true // Google accounts are always verified
                    AuthUiState.Authenticated
                }
                else -> {
                    val error = mapFirebaseError(result.exceptionOrNull())
                    _errorMessage.value = error
                    AuthUiState.Error(error)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState.Unauthenticated
        _currentUser.value = null
        _isEmailVerified.value = false
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            try {
                authRepository.sendPasswordResetEmail(email)
                _successMessage.value = "Password reset email sent! Check your inbox."
                com.example.dailydoodle.util.Analytics.logEvent("password_reset_sent")
            } catch (e: Exception) {
                Log.e(TAG, "Password reset failed", e)
                _errorMessage.value = mapFirebaseError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send verification email to current user
     */
    private fun sendVerificationEmail() {
        viewModelScope.launch {
            try {
                authRepository.sendEmailVerification()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send verification email", e)
            }
        }
    }
    
    /**
     * Resend verification email with cooldown
     */
    fun resendVerificationEmail() {
        viewModelScope.launch {
            if (_resendCooldown.value > 0) return@launch
            
            _isLoading.value = true
            try {
                authRepository.sendEmailVerification()
                com.example.dailydoodle.util.Analytics.logEvent("verification_email_resent")
                
                // Start 60-second cooldown
                _resendCooldown.value = 60
                for (i in 59 downTo 0) {
                    delay(1000)
                    _resendCooldown.value = i
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resend verification email", e)
                _errorMessage.value = mapFirebaseError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Check if email is verified (refresh user data)
     */
    fun checkEmailVerification(): Boolean {
        val user = authRepository.currentUser
        user?.reload()
        val isVerified = user?.isEmailVerified == true
        _isEmailVerified.value = isVerified
        
        if (isVerified) {
            _uiState.value = AuthUiState.Authenticated
            com.example.dailydoodle.util.Analytics.logEvent("email_verified")
        }
        
        return isVerified
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * Get current user email (for verification screen)
     */
    fun getCurrentUserEmail(): String {
        return authRepository.currentUser?.email ?: ""
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUserData()
        }
    }
    
    /**
     * Map Firebase exceptions to user-friendly messages
     */
    private fun mapFirebaseError(e: Throwable?): String {
        if (e == null) return "An unexpected error occurred."
        
        return when {
            e.message?.contains("email address is already in use") == true ->
                "This email is already registered. Try logging in instead."
            e.message?.contains("password is invalid") == true ->
                "Incorrect password. Please try again."
            e.message?.contains("no user record") == true ->
                "No account found with this email. Sign up first!"
            e.message?.contains("email address is badly formatted") == true ->
                "Please enter a valid email address."
            e.message?.contains("password should be at least") == true ->
                "Password must be at least 6 characters."
            e.message?.contains("network error") == true ->
                "Network error. Please check your connection."
            e.message?.contains("too many requests") == true ->
                "Too many attempts. Please try again later."
            e.message?.contains("account has been disabled") == true ->
                "This account has been disabled. Contact support."
            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                "Invalid email or password. Please try again."
            else ->
                e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    object Authenticated : AuthUiState()
    object Unauthenticated : AuthUiState()
    object NeedsEmailVerification : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
