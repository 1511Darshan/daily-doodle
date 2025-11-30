package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.ui.screen.onboarding.OnboardingColors
import com.example.dailydoodle.util.Analytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onSendResetEmail: (String) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    successMessage: String? = null,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    fun validateAndSubmit() {
        if (email.isBlank()) {
            emailError = "Email is required"
        } else if (!ValidationUtils.isValidEmail(email)) {
            emailError = "Please enter a valid email address"
        } else {
            emailError = null
            Analytics.logEvent("password_reset_requested")
            onSendResetEmail(email)
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
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Lock icon
            Text(
                text = "🔑",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Forgot Password?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardingColors.DarkText
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No worries! Enter your email and we'll send you a link to reset your password.",
                fontSize = 14.sp,
                color = OnboardingColors.GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Success message
            if (successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✉️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = successMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Error message
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
                singleLine = true,
                enabled = !isLoading && successMessage == null
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Send Button
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
                enabled = !isLoading && successMessage == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnboardingColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Send Reset Link",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingColors.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Back to login
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Remember your password? ",
                    fontSize = 14.sp,
                    color = OnboardingColors.GrayText
                )
                Text(
                    text = "Log in",
                    fontSize = 14.sp,
                    color = OnboardingColors.Blue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onBackClick() }
                )
            }
        }
    }
}
