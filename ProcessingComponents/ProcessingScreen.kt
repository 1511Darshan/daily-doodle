package com.yourapp.processing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define colors used in the processing screen
object ProcessingColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceContainer = Color(0xFF1A1A1A)
    val OnSurface = Color.White
    val OnSurfaceVariant = Color(0xFFAAAAAA)
    val Primary = Color.White
    val PrimaryContainer = Color(0xFF3D3D3D)
    val SecondaryContainer = Color(0xFF3D3D3D)
    val OnSecondaryContainer = Color.White
    val SuccessGreen = Color(0xFF4CAF50)
}

/**
 * Main Processing Screen that displays the backup/save progress
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationGraphicsApi::class)
@Composable
fun ProcessingScreen(
    title: String = "Processing",
    finishedTitle: String = "Backup completed",
    finishedSubtitle: String = "apps backed up",
    uiState: ProcessingUiState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onVisitDetails: (() -> Unit)? = null,
    onBack: () -> Unit,
    itemIcon: @Composable ((ProcessingItem) -> Unit)? = null
) {
    val progress: Float by remember(uiState.task?.processingIndex, uiState.dataItems.size) {
        mutableFloatStateOf(
            if (uiState.task != null && uiState.dataItems.isNotEmpty())
                uiState.task.processingIndex.toFloat() / (uiState.dataItems.size + 1)
            else
                0f
        )
    }

    Scaffold(
        containerColor = ProcessingColors.Background,
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = when (uiState.state) {
                                OperationState.DONE -> finishedTitle
                                else -> title
                            },
                            color = ProcessingColors.OnSurface
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = ProcessingColors.OnSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ProcessingColors.Background
                    )
                )
                // Linear progress indicator
                if (uiState.state != OperationState.IDLE) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = if (progress.isNaN()) 0f else progress,
                        animationSpec = tween(durationMillis = 300),
                        label = "LinearProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = ProcessingColors.Primary,
                        trackColor = ProcessingColors.PrimaryContainer,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ProcessingColors.SurfaceContainer)
            ) {
                HorizontalDivider(color = Color(0xFF2D2D2D))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // Visit details button (only when done)
                    AnimatedVisibility(visible = uiState.state == OperationState.DONE && onVisitDetails != null) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            onClick = { onVisitDetails?.invoke() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProcessingColors.SecondaryContainer,
                                contentColor = ProcessingColors.OnSecondaryContainer
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Visit details")
                        }
                    }

                    // Continue/Finish button
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.state == OperationState.IDLE || uiState.state == OperationState.DONE,
                        onClick = {
                            if (uiState.state == OperationState.DONE) onFinish()
                            else onContinue()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProcessingColors.Primary,
                            contentColor = ProcessingColors.Background,
                            disabledContainerColor = Color(0xFF3D3D3D),
                            disabledContentColor = Color(0xFF888888)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = if (uiState.state == OperationState.DONE) "Finish" else "Continue",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ProcessingColors.Background)
        ) {
            // Status display area
            ProcessingStatusArea(
                uiState = uiState,
                finishedTitle = finishedTitle,
                finishedSubtitle = finishedSubtitle,
                itemIcon = itemIcon
            )

            // Items list
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                color = ProcessingColors.SurfaceContainer
            ) {
                val lazyListState = rememberLazyListState()

                LaunchedEffect(uiState.task?.processingIndex) {
                    uiState.task?.let { task ->
                        runCatching {
                            if (task.processingIndex > 0) {
                                lazyListState.animateScrollToItem(task.processingIndex - 1)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(
                        items = uiState.dataItems,
                        key = { it.id }
                    ) { item ->
                        var expanded by rememberSaveable(uiState.task, item.id) {
                            mutableStateOf((uiState.task?.processingIndex?.minus(1) ?: -1) == uiState.dataItems.indexOf(item))
                        }

                        ProcessingCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            title = item.title,
                            state = item.state,
                            iconContent = if (itemIcon != null) {
                                { itemIcon(item) }
                            } else null,
                            expanded = expanded,
                            items = item.subItems,
                            processingIndex = item.processingIndex,
                            onExpandClick = {
                                if (uiState.state == OperationState.DONE) {
                                    expanded = !expanded
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * The status display area showing the circular progress indicator, title, and subtitle
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
private fun ProcessingStatusArea(
    uiState: ProcessingUiState,
    finishedTitle: String,
    finishedSubtitle: String,
    itemIcon: @Composable ((ProcessingItem) -> Unit)?
) {
    val task = uiState.task
    val dataItems = uiState.dataItems

    var statusTitle by remember { mutableStateOf("") }
    var statusSubtitle by remember { mutableStateOf("") }
    var segments by remember { mutableStateOf(1) }
    var segmentProgress by remember { mutableFloatStateOf(0f) }

    // Determine current state
    val processingIndex = task?.processingIndex ?: 0
    val currentItem = if (processingIndex > 0 && processingIndex <= dataItems.size) {
        dataItems.getOrNull(processingIndex - 1)
    } else null

    // Update status based on processing index
    LaunchedEffect(processingIndex, uiState) {
        when {
            processingIndex == 0 -> {
                // Preprocessing
                statusTitle = "Preprocessing"
                statusSubtitle = uiState.preItems.getOrNull(task?.preprocessingIndex ?: -1)?.title 
                    ?: "Necessary preparations"
                segments = maxOf(1, uiState.preItems.size)
                segmentProgress = uiState.preItemsProgress
            }
            processingIndex == dataItems.size + 1 -> {
                // Post-processing
                statusTitle = "Post-processing"
                statusSubtitle = uiState.postItems.getOrNull(task?.postProcessingIndex ?: -1)?.title 
                    ?: "Necessary remaining data processing"
                segments = maxOf(1, uiState.postItems.size)
                segmentProgress = uiState.postItemsProgress
            }
            processingIndex == dataItems.size + 2 || uiState.state == OperationState.DONE -> {
                // Finished
                statusTitle = finishedTitle
                val successCount = task?.successCount ?: dataItems.size
                statusSubtitle = "$successCount $finishedSubtitle"
                segments = 1
                segmentProgress = 1f
            }
            currentItem != null -> {
                // Processing an item
                statusTitle = currentItem.title
                val subItem = currentItem.subItems.getOrNull(currentItem.processingIndex)
                statusSubtitle = subItem?.let { 
                    it.title + if (it.content.isNotEmpty()) " (${it.content})" else ""
                } ?: "Necessary remaining data processing"
                segments = maxOf(1, currentItem.subItems.size)
                segmentProgress = currentItem.progress
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular progress with icon
        Box(
            modifier = Modifier.size(152.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState.state == OperationState.DONE,
                label = "StatusIcon"
            ) { isDone ->
                if (isDone) {
                    // Completed checkmark badge
                    CompletedBadge(modifier = Modifier.size(152.dp))
                } else {
                    Box(
                        modifier = Modifier.size(152.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circle with icon
                        Box(
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(ProcessingColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentItem != null && itemIcon != null) {
                                // Show item icon
                                Box(modifier = Modifier.size(64.dp)) {
                                    itemIcon(currentItem)
                                }
                            } else {
                                // Show hourglass icon
                                Icon(
                                    imageVector = Icons.Rounded.HourglassEmpty,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = ProcessingColors.OnSurface
                                )
                            }
                        }

                        // Segmented progress indicator
                        SegmentCircularProgressIndicator(
                            modifier = Modifier.size(152.dp),
                            segments = segments,
                            progress = segmentProgress
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status title
        Text(
            text = statusTitle,
            color = ProcessingColors.OnSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status subtitle
        Text(
            text = statusSubtitle,
            color = ProcessingColors.OnSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Completed badge with checkmark - the "seal" style badge shown when processing is complete
 */
@Composable
fun CompletedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // You can use the animated vector drawable here if you have it,
        // or use this simple version with a static icon
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(SealShape())
                .background(ProcessingColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Completed",
                modifier = Modifier.size(64.dp),
                tint = ProcessingColors.Background
            )
        }
    }
}

/**
 * Custom seal/badge shape for the completed state.
 * For a simpler implementation, we use a circle here.
 * You can replace this with a custom Path for the wavy seal effect.
 */
@Composable
private fun SealShape() = CircleShape
