package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos

/**
 * Animated wavy progress bar inspired by Lotus music player.
 * Shows a sine wave animation for the filled portion and a static line for the rest.
 */
@Composable
fun WavingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    enableWaving: Boolean = true,
    waveHeight: Dp = 8.dp,
    waveWidth: Dp = 8.dp,
    strokeWidth: Dp = 4.dp,
    handleSize: Dp = 24.dp
) {
    val density = LocalDensity.current
    var barWidth by remember { mutableFloatStateOf(0f) }
    
    // Animate progress smoothly
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "progress-animation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(waveHeight * 3) // Give enough space for wave + handle
            .onGloballyPositioned { barWidth = it.size.width.toFloat() }
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filled wavy segment
            FilledWaveSegment(
                color = waveColor,
                enableWaving = enableWaving,
                waveWidth = with(density) { waveWidth.toPx() },
                strokeWidth = with(density) { strokeWidth.toPx() },
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(waveHeight)
            )

            // Rest segment (static line)
            RestSegment(
                color = trackColor,
                strokeWidth = with(density) { strokeWidth.toPx() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(waveHeight)
            )
        }

        // Handle (rounded square that moves with progress)
        val handleOffset = with(density) {
            (barWidth * animatedProgress).toDp() - handleSize / 2
        }
        
        Box(
            modifier = Modifier
                .size(handleSize)
                .offset(x = handleOffset.coerceAtLeast(0.dp))
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(handleSize / 3))
                .background(color = waveColor)
        )
    }
}

/**
 * Filled segment with animated wave
 */
@Composable
private fun FilledWaveSegment(
    color: Color,
    enableWaving: Boolean = true,
    waveWidth: Float,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave-transition")

    var canvasHeight by remember { mutableFloatStateOf(0f) }
    val effectiveWaveWidth by remember(waveWidth, canvasHeight) { 
        derivedStateOf { if (waveWidth > 0) waveWidth else (canvasHeight / 1.5f) } 
    }
    val waveHeight by animateFloatAsState(
        targetValue = if (enableWaving) canvasHeight else 0f,
        animationSpec = tween(300),
        label = "wave-height"
    )

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -effectiveWaveWidth * 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "wave-offset"
    )

    Canvas(
        modifier = modifier.onGloballyPositioned { 
            canvasHeight = it.size.height.toFloat() 
        }
    ) {
        if (size.width <= 0) return@Canvas
        
        val path = Path().apply {
            val startY = canvasHeight / 2 + waveHeight * cos(-offset / effectiveWaveWidth) / 2
            moveTo(x = 0f, y = startY)
            
            for (i in 0..size.width.toInt()) {
                val y = canvasHeight / 2 + waveHeight * cos((i.toFloat() - offset) / effectiveWaveWidth) / 2
                lineTo(x = i.toFloat(), y = y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Rest segment (static line for unfilled portion)
 */
@Composable
private fun RestSegment(
    color: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (size.width <= 0) return@Canvas
        
        val path = Path().apply {
            moveTo(x = 0f, y = size.height / 2)
            lineTo(x = size.width, y = size.height / 2)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
