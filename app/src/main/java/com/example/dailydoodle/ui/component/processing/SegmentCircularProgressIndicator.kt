package com.example.dailydoodle.ui.component.processing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A segmented circular progress indicator that shows progress through multiple segments.
 * Each segment represents a step in the process.
 *
 * @param segments Number of segments in the circle
 * @param progress Current progress (0f to 1f)
 * @param gapPx Gap between segments in pixels
 * @param size Size of the indicator
 * @param trackColor Background color of the segments
 * @param progressColor Color of the filled progress
 * @param strokeWidth Width of the progress stroke
 * @param strokeCap Cap style for the stroke ends
 */
@Composable
fun SegmentCircularProgressIndicator(
    segments: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    gapPx: Float = 6f,
    size: Dp = 152.dp,
    trackColor: Color = Color(0xFF3D3D3D), // Dark gray track
    progressColor: Color = Color.White,
    strokeWidth: Dp = 8.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    val perSegmentProgress = remember(segments) { if (segments > 0) 1f / segments else 1f }
    var targetProgress by remember { mutableFloatStateOf(if (progress.isNaN()) 0f else progress) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800, delayMillis = 100),
        label = "SegmentProgress"
    )
    
    LaunchedEffect(progress) {
        targetProgress = if (progress.isNaN()) 0f else progress
    }

    Canvas(modifier = modifier.size(size)) {
        val strokeWidthPx = strokeWidth.toPx()
        val sizePx = size.toPx() - strokeWidthPx
        
        if (segments <= 0) return@Canvas
        
        repeat(segments) { index ->
            val current = perSegmentProgress * (index + 1)
            val diff = current - animatedProgress
            val segmentProgress = when {
                diff < perSegmentProgress -> {
                    if (diff >= 0) {
                        1 - diff / perSegmentProgress
                    } else {
                        1f
                    }
                }
                else -> 0f
            }

            // Draw background arc (track)
            drawArc(
                color = trackColor,
                startAngle = 270f + gapPx + index * 360f / segments,
                sweepAngle = 360f / segments - gapPx * 2,
                useCenter = false,
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(sizePx, sizePx),
                style = Stroke(width = strokeWidthPx, cap = strokeCap)
            )

            // Draw progress arc
            if (segmentProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = 270f + gapPx + index * 360f / segments,
                    sweepAngle = (360f / segments - gapPx * 2) * segmentProgress,
                    useCenter = false,
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(sizePx, sizePx),
                    style = Stroke(width = strokeWidthPx, cap = strokeCap)
                )
            }
        }
    }
}
