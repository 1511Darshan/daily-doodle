package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.*

/**
 * Custom color picker dialog with:
 * - Color wheel for hue/saturation selection
 * - Brightness slider
 * - Saturation slider  
 * - Alpha slider
 * - Hex color display
 * - Preview of selected color
 */
@Composable
fun CustomColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // Convert initial color to HSV
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hsv
    }
    
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember { mutableFloatStateOf(initialColor.alpha) }
    
    val currentColor by remember(hue, saturation, brightness, alpha) {
        derivedStateOf {
            Color.hsv(hue, saturation, brightness, alpha)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Color preview circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                
                // Hex color display
                Text(
                    text = "#${currentColor.toArgb().toUInt().toString(16).uppercase().padStart(8, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Color wheel
                ColorWheel(
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    onColorChanged = { h, s ->
                        hue = h
                        saturation = s
                    },
                    modifier = Modifier.size(240.dp)
                )
                
                // Brightness slider
                SliderWithLabel(
                    label = "Brightness",
                    value = brightness,
                    onValueChange = { brightness = it },
                    gradientColors = listOf(
                        Color.Black,
                        Color.hsv(hue, saturation, 1f)
                    )
                )
                
                // Saturation slider
                SliderWithLabel(
                    label = "Saturation",
                    value = saturation,
                    onValueChange = { saturation = it },
                    gradientColors = listOf(
                        Color.hsv(hue, 0f, brightness),
                        Color.hsv(hue, 1f, brightness)
                    )
                )
                
                // Alpha slider
                SliderWithLabel(
                    label = "Alpha",
                    value = alpha,
                    onValueChange = { alpha = it },
                    showCheckerboard = true,
                    gradientColors = listOf(
                        Color.hsv(hue, saturation, brightness, 0f),
                        Color.hsv(hue, saturation, brightness, 1f)
                    )
                )
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            onColorSelected(currentColor)
                            onDismiss()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Color wheel for selecting hue and saturation
 * Uses efficient shader-based rendering instead of pixel-by-pixel
 */
@Composable
private fun ColorWheel(
    hue: Float,
    saturation: Float,
    brightness: Float,
    onColorChanged: (hue: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pre-compute the hue colors for the sweep gradient
    val hueColors = remember(brightness) {
        (0..360 step 30).map { angle ->
            Color.hsv(angle.toFloat(), 1f, brightness)
        } + Color.hsv(0f, 1f, brightness) // Close the loop
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = minOf(centerX, centerY)
                        
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)
                        
                        if (distance <= radius) {
                            val angle = atan2(dy, dx)
                            val h = ((Math.toDegrees(angle.toDouble()) + 360) % 360).toFloat()
                            val s = (distance / radius).coerceIn(0f, 1f)
                            onColorChanged(h, s)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val radius = minOf(centerX, centerY)
                        
                        val dx = change.position.x - centerX
                        val dy = change.position.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)
                        
                        val angle = atan2(dy, dx)
                        val h = ((Math.toDegrees(angle.toDouble()) + 360) % 360).toFloat()
                        val s = (distance / radius).coerceIn(0f, 1f)
                        onColorChanged(h, s)
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = minOf(centerX, centerY)
            
            // Draw color wheel using sweep gradient (efficient GPU-based rendering)
            val sweepBrush = Brush.sweepGradient(
                colors = hueColors,
                center = Offset(centerX, centerY)
            )
            
            // Draw the hue wheel
            drawCircle(
                brush = sweepBrush,
                radius = radius,
                center = Offset(centerX, centerY)
            )
            
            // Overlay radial gradient for saturation (white center fading to transparent)
            val saturationBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color.White.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = radius
            )
            
            drawCircle(
                brush = saturationBrush,
                radius = radius,
                center = Offset(centerX, centerY)
            )
            
            // Draw selector circle
            val selectorAngle = Math.toRadians(hue.toDouble())
            val selectorDistance = saturation * radius
            val selectorX = centerX + (cos(selectorAngle) * selectorDistance).toFloat()
            val selectorY = centerY + (sin(selectorAngle) * selectorDistance).toFloat()
            
            // Outer white circle
            drawCircle(
                color = Color.White,
                radius = 14f,
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 4f)
            )
            // Inner black circle
            drawCircle(
                color = Color.Black,
                radius = 10f,
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 2f)
            )
        }
    }
}

/**
 * Slider with gradient background and label
 */
@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    gradientColors: List<Color>,
    showCheckerboard: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val trackWidth = maxWidth - 24.dp // Account for thumb size
            val thumbOffset = trackWidth * value
            
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (showCheckerboard) {
                            Modifier.background(checkerboardBrush())
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newValue = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val newValue = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    }
            )
            
            // Slider thumb
            Box(
                modifier = Modifier
                    .padding(start = thumbOffset)
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

/**
 * Creates a checkerboard pattern brush for alpha preview
 */
@Composable
private fun checkerboardBrush(): Brush {
    return Brush.linearGradient(
        colors = listOf(Color.LightGray, Color.White),
        start = Offset.Zero,
        end = Offset(8f, 8f),
        tileMode = TileMode.Repeated
    )
}
