package com.example.dailydoodle.ui.screen.drawing

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes
import com.example.dailydoodle.R

/**
 * Vertical toolbox for phone layout - positioned on the right side of the screen.
 */
@Composable
fun VerticalDrawingToolbox(
    currentColor: Color,
    currentSize: Float,
    currentBrushType: BrushType,
    isEraserMode: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onColorPickerClick: () -> Unit,
    onSizePickerClick: () -> Unit,
    onBrushClick: () -> Unit,
    onBrushTypeChange: (BrushType) -> Unit,
    onBrushFamilyChange: (BrushFamily) -> Unit,
    onEraserClick: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    onPanelSizeClick: (() -> Unit)? = null
) {
    var brushMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val customBrushes = remember { CustomBrushes.getBrushes(context) }

    Surface(
        modifier = modifier,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Brush tool with dropdown
            Box {
                IconButton(
                    onClick = { 
                        onBrushClick()
                        brushMenuExpanded = true
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(getBrushTypeIcon(currentBrushType)),
                        contentDescription = "Brush",
                        modifier = Modifier
                            .background(
                                color = if (!isEraserMode) 
                                    MaterialTheme.colorScheme.inversePrimary 
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .padding(8.dp)
                    )
                }
                
                // Full brush dropdown menu with stock and custom brushes
                BrushesDropdownMenu(
                    expanded = brushMenuExpanded,
                    onDismissRequest = { brushMenuExpanded = false },
                    onStockBrushChange = { brushType ->
                        onBrushTypeChange(brushType)
                        brushMenuExpanded = false
                    },
                    onCustomBrushChange = { brushFamily ->
                        onBrushFamilyChange(brushFamily)
                        brushMenuExpanded = false
                    },
                    customBrushes = customBrushes
                )
            }
            
            // Color picker
            IconButton(
                onClick = onColorPickerClick,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(currentColor, CircleShape)
                )
            }
            
            // Size picker
            IconButton(
                onClick = onSizePickerClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_line_weight),
                    contentDescription = "Brush Size"
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .width(40.dp)
                    .padding(vertical = 4.dp)
            )
            
            // Undo
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_undo),
                    contentDescription = "Undo"
                )
            }
            
            // Redo
            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_redo),
                    contentDescription = "Redo"
                )
            }
            
            // Eraser
            IconButton(
                onClick = onEraserClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_eraser),
                    contentDescription = "Eraser",
                    modifier = Modifier
                        .background(
                            color = if (isEraserMode) 
                                MaterialTheme.colorScheme.inversePrimary 
                            else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                )
            }
            
            // Clear all
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clear_all),
                    contentDescription = "Clear All"
                )
            }
            
            // Panel size button
            if (onPanelSizeClick != null) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(40.dp)
                        .padding(vertical = 4.dp)
                )
                
                IconButton(
                    onClick = onPanelSizeClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_aspect_ratio),
                        contentDescription = "Panel Size"
                    )
                }
            }
        }
    }
}

/**
 * Dropdown menu for selecting brushes (stock and custom).
 */
@Composable
fun BrushesDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onStockBrushChange: (BrushType) -> Unit,
    onCustomBrushChange: (BrushFamily) -> Unit,
    customBrushes: List<CustomBrush>,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        // Stock brushes section
        Text(
            text = "Stock Brushes",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        DropdownMenuItem(
            text = { Text("Pen") },
            onClick = { onStockBrushChange(BrushType.PEN) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_pen), contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Marker") },
            onClick = { onStockBrushChange(BrushType.MARKER) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_marker), contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Highlighter") },
            onClick = { onStockBrushChange(BrushType.HIGHLIGHTER) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_highlighter), contentDescription = null)
            }
        )
        DropdownMenuItem(
            text = { Text("Dashed line") },
            onClick = { onStockBrushChange(BrushType.DASHED) },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_dashed_line), contentDescription = null)
            }
        )
        
        // Custom brushes section
        if (customBrushes.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Custom Brushes",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            customBrushes.forEach { customBrush ->
                DropdownMenuItem(
                    text = { Text(customBrush.name) },
                    onClick = { onCustomBrushChange(customBrush.brushFamily) },
                    leadingIcon = {
                        Icon(
                            painterResource(customBrush.icon),
                            contentDescription = customBrush.name
                        )
                    }
                )
            }
        }
    }
}

/**
 * Get the icon resource for a brush type.
 */
private fun getBrushTypeIcon(brushType: BrushType): Int {
    return when (brushType) {
        BrushType.PEN -> R.drawable.ic_pen
        BrushType.MARKER -> R.drawable.ic_marker
        BrushType.HIGHLIGHTER -> R.drawable.ic_highlighter
        BrushType.DASHED -> R.drawable.ic_dashed_line
    }
}

/**
 * Convert BrushType to Ink BrushFamily.
 */
fun BrushType.toBrushFamily(): BrushFamily {
    return when (this) {
        BrushType.PEN -> StockBrushes.pressurePen()
        BrushType.MARKER -> StockBrushes.marker()
        BrushType.HIGHLIGHTER -> StockBrushes.highlighter()
        BrushType.DASHED -> StockBrushes.marker() // Fallback, dashed not available
    }
}

/**
 * Horizontal color bar that appears at the bottom of the screen.
 */
@Composable
fun ColorBar(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(colors.size) { index ->
            val color = colors[index]
            Surface(
                onClick = { onColorSelected(color) },
                shape = CircleShape,
                color = color,
                modifier = Modifier.size(
                    if (color == selectedColor) 44.dp else 36.dp
                ),
                border = if (color == selectedColor) {
                    ButtonDefaults.outlinedButtonBorder(enabled = true)
                } else null
            ) {}
        }
    }
}

/**
 * Bottom action bar with submit, undo, clear and rewarded ad buttons.
 */
@Composable
fun DrawingBottomBar(
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onGetHint: () -> Unit,
    onGetPremiumBrush: () -> Unit,
    onSubmit: () -> Unit,
    hasPremiumBrush: Boolean,
    isSubmitting: Boolean,
    canUndo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(
                        painter = painterResource(R.drawable.ic_undo),
                        contentDescription = "Undo"
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clear_all),
                        contentDescription = "Clear"
                    )
                }
            }

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                shape = MaterialTheme.shapes.large
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Submit")
            }
        }
        
        // Rewarded ad buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onGetHint,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Get Hint 🎬")
            }
            
            if (!hasPremiumBrush) {
                OutlinedButton(
                    onClick = onGetPremiumBrush,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Premium Brush 🎬")
                }
            }
        }
    }
}
