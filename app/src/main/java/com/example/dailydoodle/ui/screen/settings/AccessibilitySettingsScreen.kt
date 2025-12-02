package com.example.dailydoodle.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onBackClick: () -> Unit,
) {
    // Local state for toggles (can be connected to preferences later)
    var textScaling by remember { mutableStateOf(100) }
    var biggerTitles by remember { mutableStateOf(false) }
    var whiteTextInDarkMode by remember { mutableStateOf(false) }
    var nonSubduedPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .verticalScroll(rememberScrollState())
        ) {
            // Text size section
            SectionHeader(title = "Text size")
            
            AccessibilityCard {
                // Text scaling
                AccessibilityClickableItemWithTextIcon(
                    iconText = "TT",
                    title = "Text scaling",
                    subtitle = "$textScaling%",
                    onClick = { /* Open text scaling dialog */ }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Bigger titles
                AccessibilityToggleItemWithTextIcon(
                    iconText = "T",
                    title = "Bigger titles",
                    subtitle = "Show bigger titles on the the notes tiles and in the editor",
                    isChecked = biggerTitles,
                    onCheckedChange = { biggerTitles = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Text color section
            SectionHeader(title = "Text color")
            
            AccessibilityCard {
                // White text in dark mode
                AccessibilityToggleItemWithTextIcon(
                    iconText = "A",
                    title = "White text in dark mode",
                    subtitle = "Use a white color for the text in dark mode",
                    isChecked = whiteTextInDarkMode,
                    onCheckedChange = { whiteTextInDarkMode = it }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Non-subdued preview
                AccessibilityToggleItemWithTextIcon(
                    iconText = "◧",
                    title = "Non-subdued preview",
                    subtitle = "Disable the subdued text color of the notes content preview",
                    isChecked = nonSubduedPreview,
                    onCheckedChange = { nonSubduedPreview = it }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom tagline
            Text(
                text = "draw together",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun AccessibilityCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        content()
    }
}

@Composable
private fun TextIcon(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = if (text.length > 1) 14.sp else 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccessibilityClickableItemWithTextIcon(
    iconText: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextIcon(text = iconText)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AccessibilityToggleItemWithTextIcon(
    iconText: String,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextIcon(text = iconText)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
