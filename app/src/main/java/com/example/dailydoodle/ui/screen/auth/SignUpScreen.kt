package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.dailydoodle.util.Analytics

/**
 * Kenko-style design constants
 */
private val KenkoBorderWidth = 1.4.dp

private object KenkoColors {
    val Background = Color(0xFFF5F5F0)
    val CardBackground = Color(0xFFFFFFFF)
    val BorderColor = Color(0xFFE0E0E0)
    val PrimaryText = Color(0xFF1A1A1A)
    val SecondaryText = Color(0xFF757575)
    val GoogleBlue = Color(0xFF4285F4)
    val ErrorRed = Color(0xFFE53935)
    val SuccessGreen = Color(0xFF43A047)
}

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
    
    fun validateAndSubmit() {
        var isValid = true
        
        if (displayName.isBlank()) {
            displayNameError = "Display name is required"
            isValid = false
        } else if (displayName.length < 2) {
            displayNameError = "Display name must be at least 2 characters"
            isValid = false
        } else {
            displayNameError = null
        }
        
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
        } else if (!ValidationUtils.isValidPassword(password)) {
            passwordError = "Password must be at least 8 characters"
            isValid = false
        } else {
            passwordError = null
        }
        
        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }
        
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
            .background(KenkoColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header - Large Kenko-style title
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = KenkoColors.PrimaryText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Join Daily Doodle Chain and start creating!",
                style = MaterialTheme.typography.bodyLarge,
                color = KenkoColors.SecondaryText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Google Sign In - Kenko style card
            KenkoAuthButton(
                onClick = onGoogleSignIn,
                icon = {
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = KenkoColors.GoogleBlue
                    )
                },
                text = "Continue with Google",
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider with "or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = KenkoBorderWidth,
                    color = KenkoColors.BorderColor
                )
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = KenkoColors.SecondaryText
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = KenkoBorderWidth,
                    color = KenkoColors.BorderColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Fields - Kenko style
            KenkoTextField(
                value = displayName,
                onValueChange = { displayName = it; displayNameError = null },
                placeholder = "Display Name",
                icon = Icons.Default.Person,
                error = displayNameError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            KenkoTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                placeholder = "Email",
                icon = Icons.Default.Email,
                error = emailError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            KenkoPasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                placeholder = "Password",
                passwordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                error = passwordError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Password Strength Indicator
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(strength = passwordStrength)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            KenkoPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = null },
                placeholder = "Confirm Password",
                passwordVisible = confirmPasswordVisible,
                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                error = confirmPasswordError,
                enabled = !isLoading,
                showMatchIcon = confirmPassword.isNotEmpty() && confirmPassword == password,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Terms Checkbox - Kenko style
            KenkoCheckboxRow(
                checked = acceptedTerms,
                onCheckedChange = { acceptedTerms = it; termsError = null },
                enabled = !isLoading,
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick
            )
            
            if (termsError != null) {
                Text(
                    text = termsError!!,
                    color = KenkoColors.ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = KenkoColors.ErrorRed.copy(alpha = 0.1f),
                    border = BorderStroke(KenkoBorderWidth, KenkoColors.ErrorRed.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = errorMessage,
                        color = KenkoColors.ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Create Account Button - Kenko style (dark filled)
            KenkoPrimaryButton(
                onClick = { validateAndSubmit() },
                text = "Create Account",
                isLoading = isLoading,
                enabled = !isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Already have account
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KenkoColors.SecondaryText
                )
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = KenkoColors.PrimaryText,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bottom tagline
            Text(
                text = "draw together",
                style = MaterialTheme.typography.labelMedium,
                color = KenkoColors.SecondaryText.copy(alpha = 0.6f)
            )
        }
    }
}

// ===== Kenko-style UI Components =====

@Composable
private fun KenkoAuthButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = KenkoColors.CardBackground,
        border = BorderStroke(KenkoBorderWidth, KenkoColors.BorderColor),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = KenkoColors.PrimaryText
            )
        }
    }
}

@Composable
private fun KenkoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    error: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = KenkoColors.CardBackground,
            border = BorderStroke(
                KenkoBorderWidth,
                if (error != null) KenkoColors.ErrorRed else KenkoColors.BorderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KenkoColors.SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = KenkoColors.SecondaryText
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = KenkoColors.PrimaryText
                        ),
                        singleLine = true,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = KenkoColors.ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun KenkoPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    error: String? = null,
    enabled: Boolean = true,
    showMatchIcon: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = KenkoColors.CardBackground,
            border = BorderStroke(
                KenkoBorderWidth,
                if (error != null) KenkoColors.ErrorRed else KenkoColors.BorderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = KenkoColors.SecondaryText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = KenkoColors.SecondaryText
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = KenkoColors.PrimaryText
                        ),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (showMatchIcon) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Passwords match",
                        tint = KenkoColors.SuccessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    TextButton(
                        onClick = onToggleVisibility,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KenkoColors.SecondaryText
                        )
                    }
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = KenkoColors.ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isActive = when (strength) {
                PasswordStrength.WEAK -> index == 0
                PasswordStrength.MEDIUM -> index <= 1
                PasswordStrength.STRONG -> true
                PasswordStrength.NONE -> false
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) strength.color else KenkoColors.BorderColor
                    )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = strength.label,
            color = strength.color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun KenkoCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Kenko-style checkbox
        Surface(
            modifier = Modifier
                .size(24.dp)
                .clickable(enabled = enabled) { onCheckedChange(!checked) },
            shape = RoundedCornerShape(6.dp),
            color = if (checked) KenkoColors.PrimaryText else KenkoColors.CardBackground,
            border = BorderStroke(
                KenkoBorderWidth,
                if (checked) KenkoColors.PrimaryText else KenkoColors.BorderColor
            )
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KenkoColors.CardBackground,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Row {
                Text(
                    text = "I agree to the ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KenkoColors.SecondaryText
                )
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KenkoColors.PrimaryText,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onTermsClick() }
                )
                Text(
                    text = " and",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KenkoColors.SecondaryText
                )
            }
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.bodyMedium,
                color = KenkoColors.PrimaryText,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onPrivacyClick() }
            )
        }
    }
}

@Composable
private fun KenkoPrimaryButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = KenkoColors.PrimaryText,
        onClick = onClick,
        enabled = enabled && !isLoading
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = KenkoColors.CardBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = KenkoColors.CardBackground
                )
            }
        }
    }
}
