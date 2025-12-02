package com.yourapp.processing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card component that displays a processing item with expandable sub-items.
 * Shows the item's icon, title, state, progress count, and expand/collapse arrow.
 */
@Composable
fun ProcessingCard(
    modifier: Modifier = Modifier,
    title: String,
    state: OperationState = OperationState.IDLE,
    iconContent: @Composable (() -> Unit)? = null,
    expanded: Boolean = false,
    items: List<ProcessingSubItem> = emptyList(),
    processingIndex: Int = -1,
    onExpandClick: () -> Unit = {},
) {
    val successCount by remember(items) { 
        mutableIntStateOf(items.count { it.state == OperationState.DONE || it.state == OperationState.SKIP }) 
    }
    val failedCount by remember(items) { 
        mutableIntStateOf(items.count { it.state == OperationState.ERROR }) 
    }
    val totalCount by remember(items.size) { mutableIntStateOf(items.size) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) Color(0xFF2D2D2D) else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = state == OperationState.DONE) { onExpandClick() },
                color = if (expanded) Color(0xFF3D3D3D) else Color(0xFF1A1A1A),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    if (iconContent != null) {
                        iconContent()
                    }

                    // Title
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // State icon
                    AnimatedContent(targetState = state, label = "StateIcon") { currentState ->
                        when (currentState) {
                            OperationState.DONE -> StateIcon(state = currentState)
                            OperationState.ERROR -> StateIcon(state = currentState)
                            else -> {}
                        }
                    }

                    // Progress count
                    if (totalCount > 0 && state != OperationState.IDLE) {
                        Text(
                            text = "${successCount + failedCount}/$totalCount",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        )
                    }

                    // Expand/Processing indicator
                    AnimatedContent(targetState = state, label = "ExpandIcon") { currentState ->
                        when (currentState) {
                            OperationState.DONE -> {
                                Icon(
                                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                    tint = Color.White
                                )
                            }
                            OperationState.PROCESSING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeCap = StrokeCap.Round,
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                            else -> {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        }
                    }
                }
            }

            // Expanded sub-items
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StateIcon(
                                state = item.state,
                                isProcessing = processingIndex == index
                            )
                            
                            Text(
                                modifier = Modifier.weight(1f),
                                text = item.title,
                                color = Color(0xFFAAAAAA),
                                fontSize = 14.sp
                            )
                            
                            if (item.content.isNotEmpty()) {
                                Text(
                                    text = item.content,
                                    color = Color(0xFF888888),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays an icon representing the current operation state
 */
@Composable
fun StateIcon(
    state: OperationState,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    when {
        isProcessing -> {
            CircularProgressIndicator(
                modifier = modifier.size(24.dp),
                strokeCap = StrokeCap.Round,
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
        state == OperationState.DONE -> {
            Icon(
                modifier = modifier
                    .size(24.dp)
                    .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                imageVector = Icons.Rounded.Check,
                contentDescription = "Done",
                tint = Color.White
            )
        }
        state == OperationState.ERROR -> {
            Icon(
                modifier = modifier
                    .size(24.dp)
                    .background(Color(0xFFF44336), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                imageVector = Icons.Rounded.Close,
                contentDescription = "Error",
                tint = Color.White
            )
        }
        state == OperationState.SKIP -> {
            Icon(
                modifier = modifier
                    .size(24.dp)
                    .background(Color(0xFFFF9800), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                imageVector = Icons.Rounded.Remove,
                contentDescription = "Skipped",
                tint = Color.White
            )
        }
        else -> {
            Spacer(modifier = modifier.size(24.dp))
        }
    }
}
