package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailydoodle.ui.component.processing.SegmentCircularProgressIndicator

/**
 * Upload overlay with DataBackup-style animations:
 * - Segmented circular progress during upload
 * - Animated seal badge on success
 */
@Composable
fun UploadOverlay(
    isVisible: Boolean,
    progress: Float,
    stage: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Show animated seal badge on success
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSuccess,
                        enter = fadeIn() + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut() + scaleOut()
                    ) {
                        AnimatedSealBadge(size = 148.dp)
                    }
                    
                    // Show segmented progress during upload
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !isSuccess,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                        val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                        
                        Box(
                            modifier = Modifier.size(152.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Background circle with hourglass - uses primary color
                            Box(
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.example.dailydoodle.R.drawable.ic_hourglass_empty),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = onPrimaryColor
                                )
                            }
                            
                            // Segmented progress indicator - uses primary color
                            SegmentCircularProgressIndicator(
                                segments = 4, // 4 upload stages
                                progress = progress,
                                modifier = Modifier.size(152.dp),
                                progressColor = primaryColor,
                                trackColor = primaryContainerColor
                            )
                        }
                    }
                }
                
                // Stage text
                Text(
                    text = if (isSuccess) "Upload Complete!" else stage.ifEmpty { "Uploading..." },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                // Progress percentage
                if (!isSuccess) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 16.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }
        }
    }
}

/**
 * Animated seal badge for completion state - matches DataBackup style
 */
@Composable
private fun AnimatedSealBadge(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    
    var isAnimating by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isAnimating = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "seal_scale"
    )
    
    val tickProgress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "tick_progress"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Seal badge background with primary color
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            // Checkmark
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Completed",
                modifier = Modifier.size(64.dp),
                tint = onPrimaryColor.copy(alpha = tickProgress)
            )
        }
    }
}
