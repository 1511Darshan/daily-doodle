package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun EmailVerificationScreen(
    email: String,
    onResendEmail: () -> Unit,
    onContinue: () -> Unit,
    onChangeEmail: () -> Unit,
    isResending: Boolean = false,
    resendCooldown: Int = 0
) {
    LaunchedEffect(Unit) {
        Analytics.logEvent("email_verification_shown")
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
                .padding(top = 80.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email icon animation
            Text(
                text = "📧",
                fontSize = 80.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Verify Your Email",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardingColors.DarkText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "We've sent a verification link to:",
                fontSize = 14.sp,
                color = OnboardingColors.GrayText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnboardingColors.DarkText
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = OnboardingColors.LightBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📌 Next Steps:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnboardingColors.DarkText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Check your inbox (and spam folder)\n2. Click the verification link\n3. Come back here and tap Continue",
                        fontSize = 13.sp,
                        color = OnboardingColors.GrayText,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnboardingColors.Black
                )
            ) {
                Text(
                    text = "I've Verified My Email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnboardingColors.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend Button
            OutlinedButton(
                onClick = {
                    Analytics.logEvent("verification_email_resent")
                    onResendEmail()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isResending && resendCooldown == 0
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (resendCooldown > 0) {
                    Text(
                        text = "Resend in ${resendCooldown}s",
                        fontSize = 14.sp,
                        color = OnboardingColors.GrayText
                    )
                } else {
                    Text(
                        text = "Resend Verification Email",
                        fontSize = 14.sp,
                        color = OnboardingColors.DarkText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Change email
            TextButton(onClick = onChangeEmail) {
                Text(
                    text = "Wrong email? Change it",
                    fontSize = 14.sp,
                    color = OnboardingColors.Blue
                )
            }
        }
    }
}
