package com.example.app.ui.components

import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

private const val WAVE_AMPLITUDE = 12F
private const val WAVE_FREQUENCY = 0.055F
private const val INITIAL_PHASE = 0F
private val FINAL_PHASE = (2 * PI).toFloat()

/**
 * Static sine wave component.
 * Used as a decorative element next to category headers.
 */
@Composable
fun Wave(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4F,
    amplitude: Float = WAVE_AMPLITUDE,
    frequency: Float = WAVE_FREQUENCY,
    color: Color = MaterialTheme.colorScheme.tertiary,
) {
    Canvas(modifier = modifier.height(24.dp)) {
        drawWave(
            strokeColor = color,
            strokeWidth = strokeWidth,
            amplitude = amplitude,
            frequency = frequency
        )
    }
}

/**
 * Animated sine wave component that continuously animates.
 * Used as a decorative element next to category headers in settings.
 */
@Composable
fun AnimatedWave(
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4F,
    amplitude: Float = WAVE_AMPLITUDE,
    frequency: Float = WAVE_FREQUENCY,
    color: Color = MaterialTheme.colorScheme.tertiary,
    durationMillis: Int = 1_000,
) {
    val transition = rememberInfiniteTransition(label = "Wave")
    val phase by transition.animateFloat(
        initialValue = INITIAL_PHASE,
        targetValue = FINAL_PHASE,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = "Phase"
    )

    Canvas(modifier = modifier.height(24.dp)) {
        drawWave(
            strokeColor = color,
            strokeWidth = strokeWidth,
            amplitude = amplitude,
            frequency = frequency,
            phase = phase
        )
    }
}

/**
 * Draws a sine wave path.
 */
private fun DrawScope.drawWave(
    strokeColor: Color,
    strokeWidth: Float = 4F,
    amplitude: Float = WAVE_AMPLITUDE,
    frequency: Float = WAVE_FREQUENCY,
    @FloatRange(from = INITIAL_PHASE.toDouble(), to = FINAL_PHASE.toDouble()) phase: Float = 0F,
) {
    val path = Path()
    val centerY = center.y
    path.moveTo(0F, amplitude * sin(phase) + centerY)
    for (x in 0..size.width.toInt()) {
        val y = amplitude * sin((frequency * x) + phase)
        path.lineTo(x.toFloat(), y + centerY)
    }
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
    )
}
