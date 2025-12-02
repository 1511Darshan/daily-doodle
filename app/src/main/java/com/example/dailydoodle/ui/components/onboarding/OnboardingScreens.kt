package com.example.dailydoodle.ui.components.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.R
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Welcome screen with confetti animation.
 * First screen in the onboarding flow.
 */
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier
) {
    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x6dd5ed),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Confetti - full screen
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(party),
        )
        
        // Content centered
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(end = 16.dp), // Account for indicator space
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // App logo - circular version with dark background
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "DailyDoodle Logo",
                modifier = Modifier.size(180.dp)
            )
            
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Welcome text at bottom
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for button
        }
    }
}

/**
 * Features showcase screen with rotating feature cards.
 * Second screen in the onboarding flow.
 */
@Composable
fun FeaturesScreen(
    modifier: Modifier = Modifier
) {
    val features = remember {
        listOf(
            Feature(
                title = "Daily Doodle Chains",
                description = "Keep your creative streak alive by drawing every day",
                icon = Icons.Filled.Star,
                containerColor = Color(0xFF6366F1)
            ),
            Feature(
                title = "Creative Brushes",
                description = "Express yourself with unique brush styles and effects",
                icon = Icons.Filled.Create,
                containerColor = Color(0xFFEC4899)
            ),
            Feature(
                title = "Beautiful Colors",
                description = "Beautiful pre-made palettes or create your own",
                icon = Icons.Filled.Favorite,
                containerColor = Color(0xFF8B5CF6)
            ),
            Feature(
                title = "Daily Reminders",
                description = "Never miss a day with gentle notifications",
                icon = Icons.Filled.Notifications,
                containerColor = Color(0xFF14B8A6)
            ),
            Feature(
                title = "Share Your Art",
                description = "Show off your doodles with friends and family",
                icon = Icons.Filled.Share,
                containerColor = Color(0xFFF59E0B)
            ),
            Feature(
                title = "Community Gallery",
                description = "Get inspired by doodles from around the world",
                icon = Icons.Filled.Info,
                containerColor = Color(0xFF3B82F6)
            )
        )
    }
    
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % features.size
        }
    }
    
    val (first, second, third) = features.splitIntoTriple(currentIndex)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(end = 16.dp), // Account for indicator space
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title - left aligned like Shkiper
        Text(
            text = stringResource(R.string.onboarding_features_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        
        Spacer(modifier = Modifier.weight(0.15f))
        
        // Feature cards
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = first,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith
                    fadeOut(animationSpec = tween(500))
                },
                label = "feature1"
            ) { features ->
                features.firstOrNull()?.let { feature ->
                    FeatureCard(feature = feature)
                }
            }
            
            AnimatedContent(
                targetState = second,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500, delayMillis = 100)) togetherWith
                    fadeOut(animationSpec = tween(500))
                },
                label = "feature2"
            ) { features ->
                features.firstOrNull()?.let { feature ->
                    FeatureCard(feature = feature)
                }
            }
            
            AnimatedContent(
                targetState = third,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500, delayMillis = 200)) togetherWith
                    fadeOut(animationSpec = tween(500))
                },
                label = "feature3"
            ) { features ->
                features.firstOrNull()?.let { feature ->
                    FeatureCard(feature = feature)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp)) // Space for button
    }
}

/**
 * Links and resources screen.
 * Third screen in the onboarding flow.
 */
@Composable
fun LinksScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val mediaItems = remember {
        listOf(
            MediaItem(
                title = "Rate the App",
                description = "Love DailyDoodle? Leave us a review!",
                icon = Icons.Filled.Star,
                containerColor = Color(0xFFF59E0B),
                onClick = {
                    // Open Play Store
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Play Store not installed
                    }
                }
            ),
            MediaItem(
                title = "Source Code",
                description = "Explore the code on GitHub",
                icon = Icons.Filled.Info,
                containerColor = Color(0xFF1F2937),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/example/dailydoodle")
                    }
                    context.startActivity(intent)
                }
            ),
            MediaItem(
                title = "Share with Friends",
                description = "Spread the creativity!",
                icon = Icons.AutoMirrored.Filled.Send,
                containerColor = Color(0xFF6366F1),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out DailyDoodle - Keep your creative streak alive! 🎨")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share DailyDoodle"))
                }
            ),
            MediaItem(
                title = "Made with Love",
                description = "Thank you for using DailyDoodle",
                icon = Icons.Filled.Favorite,
                iconTint = Color.White,
                containerColor = Color(0xFFEC4899),
                onClick = {}
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(end = 16.dp), // Account for indicator space
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title - left aligned like Shkiper
        Text(
            text = stringResource(R.string.onboarding_links_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Media cards
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            mediaItems.forEachIndexed { index, item ->
                MediaCard(
                    item = item,
                    highlight = index == 0
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(100.dp)) // Space for button
    }
}
