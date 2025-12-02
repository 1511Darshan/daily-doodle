package com.example.dailydoodle.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A segmented circular progress indicator that animates each segment individually.
 * Similar to the one used in Android-DataBackup app.
 *
 * @param segments Number of segments to divide the circle into
 * @param progress Overall progress from 0f to 1f
 * @param gapPx Gap between segments in pixels
 * @param size Size of the indicator
 * @param trackColor Color of the completed/active portion
 * @param backgroundColor Color of the incomplete/background portion
 * @param strokeWidth Width of the progress stroke
 * @param strokeCap Cap style for the stroke ends
 */
@Composable
fun SegmentedCircularProgressIndicator(
    segments: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    gapPx: Float = 6f,
    size: Dp = 128.dp,
    trackColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    strokeWidth: Dp = 8.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    val perSegmentProgress = remember(segments) { 1f / segments }
    var targetProgress by remember { mutableFloatStateOf(if (progress.isNaN()) 0f else progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, delayMillis = 100),
        label = "AnimatedProgress"
    )

    LaunchedEffect(progress) {
        targetProgress = if (progress.isNaN()) 0f else progress
    }

    Canvas(modifier = modifier.size(size)) {
        val strokeWidthPx = strokeWidth.toPx()
        val sizePx = size.toPx() - strokeWidthPx
        
        repeat(segments) { index ->
            val current = perSegmentProgress * (index + 1)
            val diff = current - animatedProgress
            val segmentProgress = when {
                diff < perSegmentProgress -> if (diff >= 0) 1 - diff / perSegmentProgress else 1f
                else -> 0f
            }

            val startAngle = 270f + gapPx + index * 360f / segments
            val sweepAngle = 360f / segments - gapPx * 2

            // Background arc (track)
            drawArc(
                color = backgroundColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(sizePx, sizePx),
                style = Stroke(width = strokeWidthPx, cap = strokeCap)
            )

            // Foreground arc (progress)
            if (segmentProgress > 0f) {
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * segmentProgress,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(sizePx, sizePx),
                    style = Stroke(width = strokeWidthPx, cap = strokeCap)
                )
            }
        }
    }
}

/**
 * A simple circular progress indicator without segments.
 */
@Composable
fun CircularProgressIndicatorWithTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 128.dp,
    trackColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    strokeWidth: Dp = 8.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "CircularProgress"
    )

    Canvas(modifier = modifier.size(size)) {
        val strokeWidthPx = strokeWidth.toPx()
        val sizePx = size.toPx() - strokeWidthPx

        // Background track
        drawArc(
            color = backgroundColor,
            startAngle = 270f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            size = Size(sizePx, sizePx),
            style = Stroke(width = strokeWidthPx, cap = strokeCap)
        )

        // Progress arc
        drawArc(
            color = trackColor,
            startAngle = 270f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            size = Size(sizePx, sizePx),
            style = Stroke(width = strokeWidthPx, cap = strokeCap)
        )
    }
}
