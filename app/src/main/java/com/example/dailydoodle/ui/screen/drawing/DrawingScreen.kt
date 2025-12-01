package com.example.dailydoodle.ui.screen.drawing

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.R
import com.example.dailydoodle.di.AppModule
import com.example.dailydoodle.ui.admob.AdConfig
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.util.Analytics
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    chainId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DrawingCanvasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strokes by viewModel.strokesFlow.collectAsStateWithLifecycle()
    val currentBrush by viewModel.currentBrushFlow.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val isEraserMode by viewModel.isEraserMode.collectAsStateWithLifecycle()
    val eraserPath by viewModel.eraserPath.collectAsStateWithLifecycle()
    
    val authRepository = AppModule.authRepository
    val userId = authRepository.currentUserId ?: return
    val userName = remember { authRepository.currentUser?.displayName ?: "User" }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showSizePicker by rememberSaveable { mutableStateOf(false) }
    var showPanelSizeDialog by rememberSaveable { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }
    val localStrokes = remember { mutableStateListOf<Stroke>() }
    
    // Zoom state for pinch-to-zoom
    val zoomState = rememberZoomState(minScale = 1f, maxScale = 5f)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Sync strokes
    LaunchedEffect(strokes) {
        if (localStrokes != strokes) {
            localStrokes.clear()
            localStrokes.addAll(strokes)
        }
    }

    // Load rewarded ad on mount
    LaunchedEffect(Unit) {
        AdMobManager.loadRewardedAd(context)
    }

    // Handle success state
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onComplete()
        }
    }

    // Color picker dialog
    ColorPickerDialog(
        showDialog = showColorPicker,
        onDismissRequest = { showColorPicker = false },
        onColorSelected = { color ->
            currentColor = color
            viewModel.setStrokeColor(color)
            showColorPicker = false
        },
        selectedColor = currentColor,
        hasPremiumBrush = uiState.hasPremiumBrush
    )

    // Size picker dialog
    BrushSizeDialog(
        showDialog = showSizePicker,
        onDismissRequest = { showSizePicker = false },
        onSizeSelected = { size ->
            viewModel.setStrokeSize(size)
            showSizePicker = false
        },
        currentSize = uiState.strokeSize
    )

    // Panel size dialog
    PanelSizeDialog(
        showDialog = showPanelSizeDialog,
        onDismissRequest = { showPanelSizeDialog = false },
        onSizeSelected = { panelSize ->
            viewModel.setPanelSize(panelSize.width, panelSize.height)
            showPanelSizeDialog = false
        },
        currentSize = PanelSize(uiState.panelWidth, uiState.panelHeight)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Panel") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
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
        ) {
            // Drawing canvas area - outer container with dark background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                // Calculate aspect ratio from panel dimensions
                val panelAspectRatio = uiState.panelWidth.toFloat() / uiState.panelHeight.toFloat()
                
                // Inner white canvas constrained to the panel aspect ratio
                Box(
                    modifier = Modifier
                        .aspectRatio(panelAspectRatio)
                        .fillMaxSize()
                        .background(Color.White)
                        .onSizeChanged { canvasSize = it }
                ) {
                    // The drawing surface - always show, use getCurrentBrush() as fallback
                    val brush = currentBrush ?: viewModel.getCurrentBrush()
                    DrawingSurface(
                        strokes = localStrokes,
                        canvasStrokeRenderer = canvasStrokeRenderer,
                        onStrokesFinished = { newStrokes ->
                            localStrokes.addAll(newStrokes)
                            viewModel.onStrokesFinished(newStrokes)
                        },
                        onErase = viewModel::erase,
                        onEraseStart = viewModel::startErase,
                        onEraseEnd = viewModel::endErase,
                        currentBrush = brush,
                        onGetNextBrush = viewModel::getCurrentBrush,
                        isEraserMode = isEraserMode,
                        zoomState = zoomState,
                        eraserPath = eraserPath,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Zoom indicator (like PixelLab)
                ZoomIndicator(
                    zoomPercentage = zoomState.zoomPercentage,
                    isZoomed = zoomState.isZoomed,
                    onResetZoom = { zoomState.reset() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 12.dp)
                )

                // Vertical toolbox on the right side
                VerticalDrawingToolbox(
                    currentColor = currentColor,
                    currentSize = uiState.strokeSize,
                    currentBrushType = uiState.brushType,
                    isEraserMode = isEraserMode,
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onColorPickerClick = { showColorPicker = true },
                    onSizePickerClick = { showSizePicker = true },
                    onBrushClick = { viewModel.setEraserEnabled(false) },
                    onBrushTypeChange = { brushType -> viewModel.setBrushType(brushType) },
                    onBrushFamilyChange = { brushFamily -> viewModel.setBrushFamily(brushFamily) },
                    onEraserClick = { viewModel.toggleEraser() },
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onClear = viewModel::clearCanvas,
                    onPanelSizeClick = { showPanelSizeDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )

                // Upload overlay with DataBackup-style animations
                UploadOverlay(
                    isVisible = uiState.isUploading || uiState.isSuccess,
                    progress = uiState.uploadProgress,
                    stage = uiState.uploadStage,
                    isSuccess = uiState.isSuccess
                )
            }

            // Wavy progress bar - shown when uploading
            AnimatedVisibility(
                visible = uiState.isUploading && !uiState.isSuccess,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    WavingProgressBar(
                        progress = uiState.uploadProgress,
                        waveHeight = 6.dp,
                        strokeWidth = 3.dp,
                        handleSize = 18.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom action bar
            DrawingBottomBar(
                onUndo = viewModel::undo,
                onClear = viewModel::clearCanvas,
                onGetHint = {
                    AdMobManager.showRewardedAd(
                        context = context,
                        rewardType = AdConfig.RewardType.HINT,
                        onRewarded = { _ ->
                            Analytics.logRewardedAdView(AdConfig.RewardType.HINT)
                            viewModel.showHintOverlay()
                        },
                        onAdClosed = {}
                    )
                },
                onGetPremiumBrush = {
                    AdMobManager.showRewardedAd(
                        context = context,
                        rewardType = AdConfig.RewardType.PREMIUM_BRUSH,
                        onRewarded = { _ ->
                            Analytics.logRewardedAdView(AdConfig.RewardType.PREMIUM_BRUSH)
                            viewModel.unlockPremiumBrush()
                        },
                        onAdClosed = {}
                    )
                },
                onSubmit = {
                    scope.launch {
                        // Log canvas size for debugging
                        android.util.Log.d("DrawingScreen", "Canvas size: ${canvasSize.width}x${canvasSize.height}")
                        android.util.Log.d("DrawingScreen", "Output size: ${uiState.panelWidth}x${uiState.panelHeight}")
                        android.util.Log.d("DrawingScreen", "Strokes count: ${localStrokes.size}")
                        
                        // Use actual canvas size for proper scaling
                        val bitmap = createBitmapFromStrokes(
                            strokes = localStrokes.toList(),
                            canvasStrokeRenderer = canvasStrokeRenderer,
                            canvasWidth = canvasSize.width,
                            canvasHeight = canvasSize.height,
                            outputWidth = uiState.panelWidth,
                            outputHeight = uiState.panelHeight
                        )
                        viewModel.submitPanel(chainId, userId, userName, bitmap)
                    }
                },
                hasPremiumBrush = uiState.hasPremiumBrush,
                isSubmitting = uiState.isUploading,
                canUndo = canUndo
            )
        }

        // Error snackbar
        uiState.errorMessage?.let { error ->
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }
}

/**
 * Creates a bitmap from the strokes using the CanvasStrokeRenderer.
 * Since the canvas now matches the panel aspect ratio, we simply scale to output dimensions.
 */
@SuppressLint("RestrictedApi")
private fun createBitmapFromStrokes(
    strokes: List<Stroke>,
    canvasStrokeRenderer: CanvasStrokeRenderer,
    canvasWidth: Int,
    canvasHeight: Int,
    outputWidth: Int,
    outputHeight: Int
): Bitmap {
    android.util.Log.d("DrawingScreen", "createBitmap - canvas: ${canvasWidth}x${canvasHeight}, output: ${outputWidth}x${outputHeight}")
    
    // Create bitmap at the output size
    val bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    
    // If canvas size is invalid, draw strokes as-is
    if (canvasWidth <= 0 || canvasHeight <= 0) {
        android.util.Log.w("DrawingScreen", "Invalid canvas size, drawing without scaling")
        strokes.forEach { stroke ->
            canvasStrokeRenderer.draw(
                stroke = stroke,
                canvas = canvas,
                strokeToScreenTransform = Matrix()
            )
        }
        return bitmap
    }
    
    // Calculate scale to map canvas coordinates to output bitmap
    val scaleX = outputWidth.toFloat() / canvasWidth.toFloat()
    val scaleY = outputHeight.toFloat() / canvasHeight.toFloat()
    
    android.util.Log.d("DrawingScreen", "Scale factors: scaleX=$scaleX, scaleY=$scaleY")
    
    // Create transform matrix to scale strokes to output size
    val transform = Matrix()
    transform.setScale(scaleX, scaleY)
    
    strokes.forEach { stroke ->
        canvasStrokeRenderer.draw(
            stroke = stroke,
            canvas = canvas,
            strokeToScreenTransform = transform
        )
    }
    
    return bitmap
}

/**
 * Zoom indicator showing current zoom percentage.
 * Tap to reset zoom when zoomed in.
 */
@Composable
private fun ZoomIndicator(
    zoomPercentage: Int,
    isZoomed: Boolean,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isZoomed) {
                    Modifier.clickable(onClick = onResetZoom)
                } else {
                    Modifier
                }
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_zoom_in),
                contentDescription = "Zoom",
                modifier = Modifier.size(18.dp),
                tint = if (isZoomed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${zoomPercentage}%",
                style = MaterialTheme.typography.labelLarge,
                color = if (isZoomed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}