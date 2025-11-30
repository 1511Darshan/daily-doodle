package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.ui.screen.onboarding.OnboardingColors
import com.example.dailydoodle.util.Analytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    
    fun validateAndSubmit() {
        var isValid = true
        
        if (email.isBlank()) {
            emailError = "Email is required"
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }
        
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else {
            passwordError = null
        }
        
        if (isValid) {
            Analytics.logEvent("login_started", mapOf("method" to "email"))
            onLoginClick(email, password)
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
                .padding(top = 80.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Welcome Back!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardingColors.DarkText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Log in to continue your creative journey",
                fontSize = 14.sp,
                color = OnboardingColors.GrayText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
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
                HorizontalDivider(modifier = Modifier.weight(1f), color = OnboardingColors.GrayText.copy(alpha = 0.3f))
                Text(
                    text = "  or  ",
                    color = OnboardingColors.GrayText,
                    fontSize = 14.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = OnboardingColors.GrayText.copy(alpha = 0.3f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        validateAndSubmit()
                    }
                ),
                singleLine = true,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    color = OnboardingColors.Blue,
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
            
            // Login Button
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
                        text = "Log In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingColors.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Don't have account
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = OnboardingColors.GrayText
                )
                Text(
                    text = "Sign up",
                    fontSize = 14.sp,
                    color = OnboardingColors.Blue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}
