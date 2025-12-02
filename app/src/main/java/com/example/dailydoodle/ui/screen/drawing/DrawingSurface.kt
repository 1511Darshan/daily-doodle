package com.example.dailydoodle.ui.screen.drawing

import android.annotation.SuppressLint
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.withSave
import androidx.ink.authoring.compose.InProgressStrokes
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke as InkStroke

/**
 * A composable that renders the drawing surface with support for:
 * - Rendering existing strokes
 * - Drawing new strokes with InProgressStrokes
 * - Lasso eraser mode with visual feedback
 * - Highlighter transparency (multiply blend mode)
 * - Pinch to zoom and pan
 */
@SuppressLint("RestrictedApi")
@Composable
fun DrawingSurface(
    strokes: List<InkStroke>,
    canvasStrokeRenderer: CanvasStrokeRenderer,
    onStrokesFinished: (List<InkStroke>) -> Unit,
    onErase: (offsetX: Float, offsetY: Float) -> Unit,
    onEraseStart: () -> Unit,
    onEraseEnd: () -> Unit,
    currentBrush: Brush,
    onGetNextBrush: () -> Brush,
    isEraserMode: Boolean,
    modifier: Modifier = Modifier,
    zoomState: ZoomState? = null,
    eraserPath: List<Offset> = emptyList()
) {
    // Track if we're in a multi-touch gesture (for zoom/pan)
    var isMultiTouch by remember { mutableStateOf(false) }
    
    val zoomModifier = if (zoomState != null) {
        Modifier
            .pointerInput(zoomState) {
                awaitEachGesture {
                    // Wait for first pointer down
                    awaitFirstDown(requireUnconsumed = false)
                    
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }
                        
                        // Only handle zoom/pan with 2+ fingers
                        if (pointerCount >= 2) {
                            isMultiTouch = true
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()
                            val centroid = event.calculateCentroid()
                            
                            if (zoomChange != 1f) {
                                zoomState.onZoom(zoomChange, centroid)
                            }
                            if (panChange != Offset.Zero && zoomState.isZoomed) {
                                zoomState.onPan(panChange)
                            }
                            
                            // Consume the event to prevent drawing
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1 && isMultiTouch) {
                            // Still in multi-touch mode, wait for all fingers to lift
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1) {
                            isMultiTouch = false
                        }
                    } while (event.changes.any { it.pressed })
                    
                    isMultiTouch = false
                }
            }
            .pointerInput(zoomState) {
                detectTapGestures(
                    onDoubleTap = { position ->
                        zoomState.onDoubleTap(position)
                    }
                )
            }
    } else {
        Modifier
    }
    
    Box(
        modifier = modifier
            .then(zoomModifier)
            .graphicsLayer {
                if (zoomState != null) {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offset.x
                    translationY = zoomState.offset.y
                    transformOrigin = TransformOrigin.Center
                }
            }
    ) {
        // Render existing strokes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvas = drawContext.canvas.nativeCanvas
            strokes.forEach { stroke ->
                // Use Multiply blend mode for highlighter strokes for transparency effect
                val blendMode = if (stroke.brush.family == StockBrushes.highlighter()) {
                    BlendMode.Multiply
                } else {
                    BlendMode.SrcOver
                }
                drawContext.canvas.withSaveLayer(
                    drawContext.size.toRect(),
                    androidx.compose.ui.graphics.Paint().apply { 
                        this.blendMode = blendMode 
                    }
                ) {
                    canvas.withSave {
                        canvasStrokeRenderer.draw(
                            stroke = stroke,
                            canvas = this,
                            strokeToScreenTransform = Matrix()
                        )
                    }
                }
            }
            
            // Draw eraser path visualization
            if (eraserPath.size >= 2) {
                val path = Path().apply {
                    moveTo(eraserPath.first().x, eraserPath.first().y)
                    for (i in 1 until eraserPath.size) {
                        lineTo(eraserPath[i].x, eraserPath[i].y)
                    }
                }
                
                // Draw a checkered/dashed line to show eraser path
                drawPath(
                    path = path,
                    color = Color.Gray.copy(alpha = 0.7f),
                    style = Stroke(
                        width = 8f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
                
                // Draw inner line for better visibility
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 10f)
                    )
                )
            }
        }

        // Eraser mode or drawing mode (only when not in multi-touch)
        if (isEraserMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onEraseStart() },
                            onDragEnd = { onEraseEnd() }
                        ) { change, _ ->
                            onErase(change.position.x, change.position.y)
                            change.consume()
                        }
                    }
            )
        } else if (!isMultiTouch) {
            InProgressStrokes(
                defaultBrush = currentBrush,
                nextBrush = onGetNextBrush,
                onStrokesFinished = onStrokesFinished,
            )
        }
    }
}
