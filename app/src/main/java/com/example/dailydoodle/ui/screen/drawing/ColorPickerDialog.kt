package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ColorPickerDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    selectedColor: Color = Color.Black,
    hasPremiumBrush: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = modifier,
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Color",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    // Standard colors
                    Text(
                        text = "Standard Colors",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ColorGrid(
                        colors = DrawingColors.standardColors,
                        selectedColor = selectedColor,
                        onColorSelected = onColorSelected
                    )
                    
                    // Premium colors section
                    if (hasPremiumBrush) {
                        HorizontalDivider()
                        Text(
                            text = "Premium Colors",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ColorGrid(
                            colors = DrawingColors.premiumColors,
                            selectedColor = selectedColor,
                            onColorSelected = onColorSelected
                        )
                    }
                    
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGrid(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.heightIn(max = 150.dp)
    ) {
        items(colors) { color ->
            ColorSwatch(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color, CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                }
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun BrushSizeDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSizeSelected: (Float) -> Unit,
    currentSize: Float = 5f,
    modifier: Modifier = Modifier
) {
    val sizes = listOf(2f, 5f, 10f, 15f, 20f, 25f, 30f)
    
    if (showDialog) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = modifier,
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Brush Size",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(sizes) { size ->
                            SizeSwatch(
                                size = size,
                                isSelected = size == currentSize,
                                onClick = { onSizeSelected(size) }
                            )
                        }
                    }
                    
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun SizeSwatch(
    size: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size.dp.coerceIn(4.dp, 30.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    CircleShape
                )
        )
    }
}
