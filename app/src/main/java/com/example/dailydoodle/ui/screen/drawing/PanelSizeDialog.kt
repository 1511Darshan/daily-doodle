package com.example.dailydoodle.ui.screen.drawing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Data class representing panel dimensions.
 */
data class PanelSize(
    val width: Int,
    val height: Int,
    val name: String = "Custom"
) {
    val aspectRatio: String
        get() {
            val gcd = gcd(width, height)
            return "${width / gcd}:${height / gcd}"
        }
    
    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
}

/**
 * Preset panel sizes.
 */
object PanelSizePresets {
    val SQUARE = PanelSize(1080, 1080, "Square")
    val LANDSCAPE_16_9 = PanelSize(1920, 1080, "Landscape 16:9")
    val PORTRAIT_9_16 = PanelSize(1080, 1920, "Portrait 9:16")
    val LANDSCAPE_4_3 = PanelSize(1440, 1080, "Landscape 4:3")
    val PORTRAIT_3_4 = PanelSize(1080, 1440, "Portrait 3:4")
    val COMIC_PANEL = PanelSize(1280, 960, "Comic Panel")
    
    val presets = listOf(
        SQUARE,
        LANDSCAPE_16_9,
        PORTRAIT_9_16,
        LANDSCAPE_4_3,
        PORTRAIT_3_4,
        COMIC_PANEL
    )
    
    val DEFAULT = SQUARE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelSizeDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSizeSelected: (PanelSize) -> Unit,
    currentSize: PanelSize = PanelSizePresets.DEFAULT,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf<PanelSize?>(
        PanelSizePresets.presets.find { it.width == currentSize.width && it.height == currentSize.height }
    ) }
    var customWidth by remember { mutableStateOf(currentSize.width.toString()) }
    var customHeight by remember { mutableStateOf(currentSize.height.toString()) }
    var isCustom by remember { mutableStateOf(selectedPreset == null) }
    
    // Calculate current aspect ratio for display
    val currentWidth = customWidth.toIntOrNull() ?: 1080
    val currentHeight = customHeight.toIntOrNull() ?: 1080
    val currentAspectRatio = remember(currentWidth, currentHeight) {
        val gcd = gcd(currentWidth, currentHeight)
        if (gcd > 0) "${currentWidth / gcd}:${currentHeight / gcd}" else "1:1"
    }
    
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0.85f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(150)),
                exit = scaleOut(targetScale = 0.9f) + fadeOut(animationSpec = tween(100))
            ) {
                Card(
                    modifier = modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp),
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
                    // Aspect ratio display header
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "aspect ratio : ",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentAspectRatio,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Preset dropdown
                    var presetExpanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = presetExpanded,
                        onExpandedChange = { presetExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (isCustom) "Custom" else (selectedPreset?.name ?: "Custom"),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preset") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = presetExpanded,
                            onDismissRequest = { presetExpanded = false }
                        ) {
                            // Custom option
                            DropdownMenuItem(
                                text = { Text("Custom") },
                                onClick = {
                                    isCustom = true
                                    selectedPreset = null
                                    presetExpanded = false
                                }
                            )
                            
                            HorizontalDivider()
                            
                            // Preset options
                            PanelSizePresets.presets.forEach { preset ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(preset.name)
                                            Text(
                                                "${preset.width}×${preset.height}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPreset = preset
                                        isCustom = false
                                        customWidth = preset.width.toString()
                                        customHeight = preset.height.toString()
                                        presetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Width and Height inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = customWidth,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 5) {
                                    customWidth = value
                                    isCustom = true
                                    selectedPreset = null
                                }
                            },
                            label = { Text("Width") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = customHeight,
                            onValueChange = { value ->
                                if (value.all { it.isDigit() } && value.length <= 5) {
                                    customHeight = value
                                    isCustom = true
                                    selectedPreset = null
                                }
                            },
                            label = { Text("Height") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text("CANCEL")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        TextButton(
                            onClick = {
                                val width = customWidth.toIntOrNull() ?: 1080
                                val height = customHeight.toIntOrNull() ?: 1080
                                // Clamp to reasonable bounds
                                val clampedWidth = width.coerceIn(100, 4096)
                                val clampedHeight = height.coerceIn(100, 4096)
                                onSizeSelected(PanelSize(clampedWidth, clampedHeight, if (isCustom) "Custom" else selectedPreset?.name ?: "Custom"))
                            },
                            enabled = customWidth.toIntOrNull() != null && customHeight.toIntOrNull() != null
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
            }
        }
    }
}

private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
