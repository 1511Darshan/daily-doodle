package com.example.dailydoodle.ui.screen.drawing

import android.annotation.SuppressLint
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.graphics.withSave
import androidx.ink.authoring.compose.InProgressStrokes
import androidx.ink.brush.Brush
import androidx.ink.brush.StockBrushes
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke

/**
 * A composable that renders the drawing surface with support for:
 * - Rendering existing strokes
 * - Drawing new strokes with InProgressStrokes
 * - Eraser mode with touch detection
 * - Highlighter transparency (multiply blend mode)
 */
@SuppressLint("RestrictedApi")
@Composable
fun DrawingSurface(
    strokes: List<Stroke>,
    canvasStrokeRenderer: CanvasStrokeRenderer,
    onStrokesFinished: (List<Stroke>) -> Unit,
    onErase: (offsetX: Float, offsetY: Float) -> Unit,
    onEraseStart: () -> Unit,
    onEraseEnd: () -> Unit,
    currentBrush: Brush,
    onGetNextBrush: () -> Brush,
    isEraserMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
        }

        // Eraser mode or drawing mode
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
        } else {
            InProgressStrokes(
                defaultBrush = currentBrush,
                nextBrush = onGetNextBrush,
                onStrokesFinished = onStrokesFinished,
            )
        }
    }
}
