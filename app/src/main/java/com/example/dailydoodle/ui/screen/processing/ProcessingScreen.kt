package com.example.dailydoodle.ui.screen.processing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dailydoodle.R
import com.example.dailydoodle.ui.component.AnimatedTickIcon
import com.example.dailydoodle.ui.component.SegmentedCircularProgressIndicator

/**
 * A processing/upload screen similar to Android-DataBackup.
 * Shows segmented circular progress, status, and a list of processing items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    state: ProcessingScreenState,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.overallProgress,
        label = "OverallProgress"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(state.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
                // Linear progress bar at the top
                if (state.overallState == OperationState.PROCESSING) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (state.overallProgress > 0f && state.overallState != OperationState.DONE) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            ProcessingBottomBar(
                state = state.overallState,
                onContinue = onContinue,
                onFinish = onFinish,
                onRetry = onRetry
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Center status area with circular progress
            ProcessingStatusArea(
                state = state,
                modifier = Modifier.fillMaxWidth()
            )

            // Items list
            if (state.items.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 1.dp
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            ProcessingItemCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingStatusArea(
    state: ProcessingScreenState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(152.dp),
            contentAlignment = Alignment.Center
        ) {
            // Show different content based on state
            androidx.compose.animation.AnimatedVisibility(
                visible = state.overallState == OperationState.DONE,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                AnimatedTickIcon(size = 148.dp)
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.overallState == OperationState.ERROR,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.overallState != OperationState.DONE && state.overallState != OperationState.ERROR,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Hourglass or custom icon in the center
                    Icon(
                        painter = painterResource(R.drawable.ic_hourglass),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    // Segmented circular progress around it
                    SegmentedCircularProgressIndicator(
                        segments = state.segments,
                        progress = state.segmentProgress,
                        size = 152.dp,
                        strokeWidth = 8.dp
                    )
                }
            }
        }

        // Status title
        Text(
            text = state.statusTitle,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Status subtitle
        Text(
            text = state.statusSubtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun ProcessingBottomBar(
    state: OperationState,
    onContinue: () -> Unit,
    onFinish: () -> Unit,
    onRetry: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Show retry button on error
        AnimatedVisibility(visible = state == OperationState.ERROR && onRetry != null) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onRetry?.invoke() }
            ) {
                Text("Retry")
            }
        }

        // Main action button
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = state == OperationState.IDLE || state == OperationState.DONE || state == OperationState.ERROR,
            onClick = {
                when (state) {
                    OperationState.DONE, OperationState.ERROR -> onFinish()
                    else -> onContinue()
                }
            }
        ) {
            when (state) {
                OperationState.PROCESSING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Processing...")
                }
                OperationState.DONE -> Text("Finish")
                OperationState.ERROR -> Text("Close")
                else -> Text("Continue")
            }
        }
    }
}

@Composable
private fun ProcessingItemCard(
    item: ProcessingItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (item.state) {
                OperationState.DONE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                OperationState.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                OperationState.PROCESSING -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status icon
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when (item.state) {
                    OperationState.DONE -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    OperationState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    OperationState.PROCESSING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    else -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_hourglass),
                            contentDescription = "Waiting",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Title and subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.errorMessage != null) {
                    Text(
                        text = item.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Progress percentage for processing items
            if (item.state == OperationState.PROCESSING && item.progress > 0f) {
                Text(
                    text = "${(item.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
