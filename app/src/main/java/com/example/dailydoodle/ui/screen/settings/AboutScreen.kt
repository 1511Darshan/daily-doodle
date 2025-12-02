package com.example.dailydoodle.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Header with app info
            AboutHeader(versionName = versionName)
            
            // Media content sections
            AboutMediaContent(context = context)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AboutHeader(versionName: String) {
    // App name color animation
    val appNameColors = listOf(
        MaterialTheme.colorScheme.primary,
        Color(0xFF9C27B0), // Purple
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
    )
    var currentColorIndex by remember { mutableIntStateOf(0) }
    val animatedColor by animateColorAsState(
        targetValue = appNameColors[currentColorIndex],
        animationSpec = tween(1000),
        label = "appNameColor"
    )
    
    // Animation timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentColorIndex = (currentColorIndex + 1) % appNameColors.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 30.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // App icon
        Image(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "App Icon"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // App name with animated color
        Text(
            text = "DailyDoodle",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = animatedColor,
            textAlign = TextAlign.Center
        )

        // Author
        Text(
            text = "Made by Darshan K 💜",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )

        // Version
        Text(
            text = versionName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AboutMediaContent(context: Context) {
    // Auto-highlight animation state
    val totalCards = 4
    var highlightedIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            highlightedIndex = (highlightedIndex + 1) % totalCards
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Development Section
        AboutMediaGroup(
            title = "Development",
            isHighlighted = highlightedIndex in 0..1
        ) {
            AboutMediaCard(
                icon = R.drawable.ic_code,
                title = "Source Code",
                description = "View the project on GitHub, contribute, and star!",
                isHighlighted = highlightedIndex == 0,
                onClick = { openUrl(context, "https://github.com/1511Darshan/daily-doodle") }
            )
            AboutMediaCard(
                icon = R.drawable.ic_bug_report,
                title = "Report Issues",
                description = "Found a bug? Report it on GitHub Issues",
                isHighlighted = highlightedIndex == 1,
                onClick = { openUrl(context, "https://github.com/1511Darshan/daily-doodle/issues") }
            )
        }

        // Contact Section
        AboutMediaGroup(
            title = "Contact",
            isHighlighted = highlightedIndex == 2
        ) {
            AboutMediaCard(
                icon = R.drawable.ic_email,
                title = "Darshan K",
                description = "Questions, suggestions, or just want to say hi? Email me!",
                isHighlighted = highlightedIndex == 2,
                onClick = { sendEmail(context, "rushdarshan@gmail.com") }
            )
        }

        // Legal Section
        AboutMediaGroup(
            title = "Legal",
            isHighlighted = highlightedIndex == 3
        ) {
            AboutMediaCard(
                icon = R.drawable.ic_policy,
                title = "Privacy Policy",
                description = "Read about how we handle your data",
                isHighlighted = highlightedIndex == 3,
                onClick = { /* TODO: Add privacy policy URL */ }
            )
        }
    }
}

@Composable
private fun AboutMediaGroup(
    title: String,
    isHighlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val titleColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "groupTitleColor"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun AboutMediaCard(
    @DrawableRes icon: Int,
    title: String,
    description: String,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    // Animate colors based on highlight state
    val containerColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "cardContainerColor"
    )
    
    val iconContainerColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(300),
        label = "iconContainerColor"
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.onPrimary 
        else 
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "iconTint"
    )
    
    val titleColor by animateColorAsState(
        targetValue = if (isHighlighted) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "titleColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun sendEmail(context: Context, email: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "DailyDoodle Feedback")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
