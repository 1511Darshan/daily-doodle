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
fun TermsOfServiceScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SectionTitle("1. Acceptance of Terms")
            SectionContent("""
                By downloading, installing, or using Daily Doodle Chain ("the App"), you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use the App.
            """.trimIndent())
            
            SectionTitle("2. Description of Service")
            SectionContent("""
                Daily Doodle Chain is a social creativity app that allows users to:
                • Create and share digital artwork
                • Participate in collaborative drawing chains
                • React to and comment on other users' creations
                • Purchase premium features through in-app purchases
            """.trimIndent())
            
            SectionTitle("3. User Accounts")
            SectionContent("""
                To use certain features, you must create an account. You agree to:
                • Provide accurate and complete information
                • Maintain the security of your account credentials
                • Notify us immediately of any unauthorized access
                • Be responsible for all activities under your account
                
                You must be at least 13 years old to create an account.
            """.trimIndent())
            
            SectionTitle("4. User Content")
            SectionContent("""
                You retain ownership of content you create. By posting content, you grant us a non-exclusive, worldwide license to:
                • Display your content within the App
                • Use content for promotional purposes
                • Store and backup your content
                
                You agree not to post content that:
                • Is illegal, harmful, or offensive
                • Infringes on intellectual property rights
                • Contains malware or harmful code
                • Violates others' privacy
            """.trimIndent())
            
            SectionTitle("5. Content Moderation")
            SectionContent("""
                We reserve the right to:
                • Remove content that violates these terms
                • Suspend or terminate accounts for violations
                • Use automated and manual moderation systems
                
                Users can report inappropriate content through the App's reporting feature.
            """.trimIndent())
            
            SectionTitle("6. In-App Purchases")
            SectionContent("""
                The App offers optional in-app purchases. All purchases are:
                • Processed through Google Play
                • Subject to Google Play's terms and refund policies
                • Non-transferable between accounts
                
                Premium features include brush packs, ad removal, and high-resolution exports.
            """.trimIndent())
            
            SectionTitle("7. Advertising")
            SectionContent("""
                The free version of the App displays advertisements. We use:
                • Google AdMob for ad delivery
                • Personalized and non-personalized ads based on your consent
                
                You can remove ads through our premium subscription.
            """.trimIndent())
            
            SectionTitle("8. Intellectual Property")
            SectionContent("""
                The App, including its design, features, and code, is protected by copyright and other intellectual property laws. You may not:
                • Copy or modify the App
                • Reverse engineer the App
                • Use our trademarks without permission
            """.trimIndent())
            
            SectionTitle("9. Limitation of Liability")
            SectionContent("""
                The App is provided "as is" without warranties. We are not liable for:
                • Loss of data or content
                • Service interruptions
                • Actions of other users
                • Any indirect or consequential damages
            """.trimIndent())
            
            SectionTitle("10. Changes to Terms")
            SectionContent("""
                We may update these terms from time to time. We will notify you of significant changes through the App. Continued use after changes constitutes acceptance.
            """.trimIndent())
            
            SectionTitle("11. Contact Us")
            SectionContent("""
                For questions about these Terms, contact us at:
                support@dailydoodlechain.com
            """.trimIndent())
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
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
private fun SectionContent(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = OnboardingColors.GrayText,
        lineHeight = 22.sp
    )
}
