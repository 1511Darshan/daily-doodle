package com.example.dailydoodle.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.ui.screen.onboarding.OnboardingColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OnboardingColors.Background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OnboardingColors.Background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Last Updated: December 2024",
                fontSize = 12.sp,
                color = OnboardingColors.GrayText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your privacy is important to us. This Privacy Policy explains how Daily Doodle Chain collects, uses, and protects your information.",
                fontSize = 14.sp,
                color = OnboardingColors.DarkText,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrivacySectionTitle("1. Information We Collect")
            PrivacySectionContent("""
                Account Information:
                • Email address
                • Display name
                • Profile picture (optional)
                
                Usage Data:
                • Drawings and content you create
                • Interactions (likes, comments, reactions)
                • App usage patterns and preferences
                
                Device Information:
                • Device type and model
                • Operating system version
                • Unique device identifiers
                • Crash logs and diagnostics
            """.trimIndent())
            
            PrivacySectionTitle("2. How We Use Your Information")
            PrivacySectionContent("""
                We use your information to:
                • Provide and improve our services
                • Display your content to other users
                • Send notifications and updates
                • Personalize your experience
                • Analyze usage and performance
                • Prevent abuse and enforce policies
                • Comply with legal obligations
            """.trimIndent())
            
            PrivacySectionTitle("3. Information Sharing")
            PrivacySectionContent("""
                We share your information with:
                
                Other Users:
                • Your public profile and content
                • Your reactions and comments
                
                Service Providers:
                • Firebase (authentication, database, analytics)
                • Google AdMob (advertising)
                • Cloud storage providers
                
                We do NOT sell your personal information.
            """.trimIndent())
            
            PrivacySectionTitle("4. Advertising")
            PrivacySectionContent("""
                We display ads using Google AdMob. These ads may be:
                
                • Personalized: Based on your interests and activity
                • Non-personalized: Generic ads based on context
                
                You can choose ad preferences in your device settings. Premium users can remove all ads.
                
                Ad partners may collect:
                • Device advertising ID
                • IP address
                • App usage data
            """.trimIndent())
            
            PrivacySectionTitle("5. Data Storage & Security")
            PrivacySectionContent("""
                Your data is stored securely using:
                • Firebase Cloud Firestore
                • Firebase Cloud Storage
                • Industry-standard encryption
                
                We retain your data while your account is active. You can request deletion at any time.
            """.trimIndent())
            
            PrivacySectionTitle("6. Your Rights")
            PrivacySectionContent("""
                You have the right to:
                • Access your personal data
                • Correct inaccurate data
                • Delete your account and data
                • Export your data
                • Opt-out of personalized ads
                • Withdraw consent
                
                To exercise these rights, go to Settings > Privacy or contact us.
            """.trimIndent())
            
            PrivacySectionTitle("7. Children's Privacy")
            PrivacySectionContent("""
                Daily Doodle Chain is not intended for children under 13. We do not knowingly collect information from children under 13. If you believe we have collected such information, please contact us.
            """.trimIndent())
            
            PrivacySectionTitle("8. International Users")
            PrivacySectionContent("""
                Your data may be transferred to and processed in countries other than your own. By using the App, you consent to this transfer.
                
                For EU/EEA users: We comply with GDPR requirements.
            """.trimIndent())
            
            PrivacySectionTitle("9. Analytics")
            PrivacySectionContent("""
                We use Firebase Analytics to understand how users interact with our App. This includes:
                • Screen views and navigation
                • Feature usage
                • Error and crash reports
                
                You can opt-out of analytics in your device settings.
            """.trimIndent())
            
            PrivacySectionTitle("10. Changes to This Policy")
            PrivacySectionContent("""
                We may update this Privacy Policy from time to time. We will notify you of significant changes through the App or via email.
            """.trimIndent())
            
            PrivacySectionTitle("11. Contact Us")
            PrivacySectionContent("""
                For questions about this Privacy Policy or your data:
                
                Email: privacy@dailydoodlechain.com
                
                Data Protection Officer:
                dpo@dailydoodlechain.com
            """.trimIndent())
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySectionTitle(text: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = OnboardingColors.DarkText
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun PrivacySectionContent(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = OnboardingColors.GrayText,
        lineHeight = 22.sp
    )
}
