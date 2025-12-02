package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State holder for zoom and pan functionality.
 * Manages scale, offset, and provides methods for zoom gestures.
 */
@Stable
class ZoomState(
    private val coroutineScope: CoroutineScope,
    initialScale: Float = 1f,
    val minScale: Float = 1f,
    val maxScale: Float = 5f
) {
    // Current scale factor
    private val _scale = Animatable(initialScale)
    val scale: Float get() = _scale.value
    
    // Current offset for panning
    private val _offsetX = Animatable(0f)
    private val _offsetY = Animatable(0f)
    val offset: Offset get() = Offset(_offsetX.value, _offsetY.value)
    
    // Zoom percentage for display
    val zoomPercentage: Int get() = (scale * 100).toInt()
    
    // Whether currently zoomed in
    val isZoomed: Boolean get() = scale > minScale + 0.01f
    
    /**
     * Handle zoom gesture (pinch to zoom).
     */
    fun onZoom(zoomChange: Float, centroid: Offset) {
        coroutineScope.launch {
            val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
            _scale.snapTo(newScale)
            
            // Adjust offset to zoom towards the centroid
            if (newScale > minScale) {
                val scaleDelta = newScale / scale
                val newOffsetX = (offset.x - centroid.x) * scaleDelta + centroid.x
                val newOffsetY = (offset.y - centroid.y) * scaleDelta + centroid.y
                _offsetX.snapTo(newOffsetX)
                _offsetY.snapTo(newOffsetY)
            }
        }
    }
    
    /**
     * Handle pan gesture (drag while zoomed).
     */
    fun onPan(pan: Offset) {
        if (!isZoomed) return
        
        coroutineScope.launch {
            _offsetX.snapTo(offset.x + pan.x)
            _offsetY.snapTo(offset.y + pan.y)
        }
    }
    
    /**
     * Set specific zoom level.
     */
    fun setZoom(newScale: Float, animate: Boolean = true) {
        coroutineScope.launch {
            val clampedScale = newScale.coerceIn(minScale, maxScale)
            if (animate) {
                _scale.animateTo(
                    clampedScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            } else {
                _scale.snapTo(clampedScale)
            }
            
            // Reset offset if zooming out to 100%
            if (clampedScale <= minScale) {
                _offsetX.animateTo(0f)
                _offsetY.animateTo(0f)
            }
        }
    }
    
    /**
     * Reset zoom to 100%.
     */
    fun reset() {
        coroutineScope.launch {
            launch { 
                _scale.animateTo(
                    minScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch { _offsetX.animateTo(0f) }
            launch { _offsetY.animateTo(0f) }
        }
    }
    
    /**
     * Double-tap to zoom in/out.
     */
    fun onDoubleTap(position: Offset) {
        coroutineScope.launch {
            if (isZoomed) {
                // Zoom out to 100%
                reset()
            } else {
                // Zoom in to 200% centered on tap position
                val targetScale = 2f
                _scale.animateTo(
                    targetScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
}

/**
 * Remember a ZoomState instance.
 */
@Composable
fun rememberZoomState(
    initialScale: Float = 1f,
    minScale: Float = 1f,
    maxScale: Float = 5f
): ZoomState {
    val coroutineScope = rememberCoroutineScope()
    return remember(minScale, maxScale) {
        ZoomState(
            coroutineScope = coroutineScope,
            initialScale = initialScale,
            minScale = minScale,
            maxScale = maxScale
        )
    }
}
