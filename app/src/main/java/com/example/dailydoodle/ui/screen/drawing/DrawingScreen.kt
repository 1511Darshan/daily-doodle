package com.example.dailydoodle.ui.screen.drawing

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    
    val authRepository = AppModule.authRepository
    val userId = authRepository.currentUserId ?: return
    val userName = remember { authRepository.currentUser?.displayName ?: "User" }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var showSizePicker by rememberSaveable { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }
    val localStrokes = remember { mutableStateListOf<Stroke>() }

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
            // Drawing canvas area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                    modifier = Modifier.fillMaxSize()
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
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )

                // Loading overlay
                if (uiState.isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            // Color bar
            ColorBar(
                colors = if (uiState.hasPremiumBrush) {
                    DrawingColors.standardColors + DrawingColors.premiumColors
                } else {
                    DrawingColors.standardColors
                },
                selectedColor = currentColor,
                onColorSelected = { color ->
                    currentColor = color
                    viewModel.setStrokeColor(color)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
                        val bitmap = createBitmapFromStrokes(
                            strokes = localStrokes.toList(),
                            canvasStrokeRenderer = canvasStrokeRenderer,
                            width = canvasSize.width.coerceAtLeast(1080),
                            height = canvasSize.height.coerceAtLeast(810)
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
 */
@SuppressLint("RestrictedApi")
private fun createBitmapFromStrokes(
    strokes: List<Stroke>,
    canvasStrokeRenderer: CanvasStrokeRenderer,
    width: Int,
    height: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    
    strokes.forEach { stroke ->
        canvasStrokeRenderer.draw(
            stroke = stroke,
            canvas = canvas,
            strokeToScreenTransform = Matrix()
        )
    }
    
    return bitmap
}