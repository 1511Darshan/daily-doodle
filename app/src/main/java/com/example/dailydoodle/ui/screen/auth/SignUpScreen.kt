package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.ui.screen.onboarding.OnboardingColors
import com.example.dailydoodle.util.Analytics

// Validation helpers
object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }
    
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        if (password.length < 6) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 1 -> PasswordStrength.WEAK
            score == 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
}

enum class PasswordStrength(val label: String, val color: Color) {
    NONE("", Color.Transparent),
    WEAK("Weak", Color(0xFFE53935)),
    MEDIUM("Medium", Color(0xFFFFA726)),
    STRONG("Strong", Color(0xFF43A047))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptedTerms by remember { mutableStateOf(false) }
    
    // Validation states
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val passwordStrength = ValidationUtils.getPasswordStrength(password)
    
    // Check if form is valid
    val isFormValid = displayName.isNotBlank() &&
            ValidationUtils.isValidEmail(email) &&
            ValidationUtils.isValidPassword(password) &&
            password == confirmPassword &&
            acceptedTerms
    
    fun validateAndSubmit() {
        var isValid = true
        
        // Validate display name
        if (displayName.isBlank()) {
            displayNameError = "Display name is required"
            isValid = false
        } else if (displayName.length < 2) {
            displayNameError = "Display name must be at least 2 characters"
            isValid = false
        } else {
            displayNameError = null
        }
        
        // Validate email
        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }
        
        // Validate password
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (!ValidationUtils.isValidPassword(password)) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        } else {
            passwordError = null
        }
        
        // Validate confirm password
        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }
        
        // Validate terms
        if (!acceptedTerms) {
            termsError = "You must accept the Terms and Privacy Policy"
            isValid = false
        } else {
            termsError = null
        }
        
        if (isValid) {
            Analytics.logEvent("sign_up_started", mapOf("method" to "email"))
            onSignUpClick(displayName, email, password)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardingColors.DarkText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Join Daily Doodle Chain and start creating!",
                fontSize = 14.sp,
                color = OnboardingColors.GrayText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Google Sign In Button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = OnboardingColors.White
                )
            ) {
                Text(
                    text = "🔵 Continue with Google",
                    fontSize = 16.sp,
                    color = OnboardingColors.DarkText
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = OnboardingColors.GrayText.copy(alpha = 0.3f))
                Text(
                    text = "  or  ",
                    color = OnboardingColors.GrayText,
                    fontSize = 14.sp
                )
                Divider(modifier = Modifier.weight(1f), color = OnboardingColors.GrayText.copy(alpha = 0.3f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display Name Field
            OutlinedTextField(
                value = displayName,
                onValueChange = { 
                    displayName = it
                    displayNameError = null
                },
                label = { Text("Display Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = displayNameError != null,
                supportingText = displayNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontSize = 12.sp,
                            color = OnboardingColors.Blue
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading
            )
            
            // Password Strength Indicator
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isActive = when (passwordStrength) {
                            PasswordStrength.WEAK -> index == 0
                            PasswordStrength.MEDIUM -> index <= 1
                            PasswordStrength.STRONG -> true
                            PasswordStrength.NONE -> false
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    if (isActive) passwordStrength.color else Color.LightGray,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = passwordStrength.label,
                        color = passwordStrength.color,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                        Icon(Icons.Default.Check, contentDescription = "Passwords match", tint = Color(0xFF43A047))
                    } else {
                        TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Text(
                                text = if (confirmPasswordVisible) "Hide" else "Show",
                                fontSize = 12.sp,
                                color = OnboardingColors.Blue
                            )
                        }
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        if (isFormValid) validateAndSubmit()
                    }
                ),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Terms and Privacy Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { 
                        acceptedTerms = it
                        termsError = null
                    },
                    enabled = !isLoading
                )
                Column {
                    Row {
                        Text(
                            text = "I agree to the ",
                            fontSize = 13.sp,
                            color = OnboardingColors.GrayText
                        )
                        Text(
                            text = "Terms of Service",
                            fontSize = 13.sp,
                            color = OnboardingColors.Blue,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { onTermsClick() }
                        )
                        Text(
                            text = " and ",
                            fontSize = 13.sp,
                            color = OnboardingColors.GrayText
                        )
                    }
                    Text(
                        text = "Privacy Policy",
                        fontSize = 13.sp,
                        color = OnboardingColors.Blue,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onPrivacyClick() }
                    )
                }
            }
            
            if (termsError != null) {
                Text(
                    text = termsError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error message from Firebase
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Sign Up Button
            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnboardingColors.Black,
                    disabledContainerColor = OnboardingColors.Black.copy(alpha = 0.5f)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnboardingColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingColors.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Already have account
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = OnboardingColors.GrayText
                )
                Text(
                    text = "Log in",
                    fontSize = 14.sp,
                    color = OnboardingColors.Blue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skip for now (Guest mode)
            TextButton(onClick = onSkipClick) {
                Text(
                    text = "Skip for now",
                    fontSize = 14.sp,
                    color = OnboardingColors.GrayText
                )
            }
        }
    }
}
