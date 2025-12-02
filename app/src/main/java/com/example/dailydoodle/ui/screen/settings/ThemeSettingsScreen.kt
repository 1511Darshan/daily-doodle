package com.example.dailydoodle.ui.screen.settings

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.dailydoodle.LocalThemePreferences
import com.example.dailydoodle.ui.components.AnimatedWave
import com.example.dailydoodle.ui.theme.ThemeMode
import kotlinx.coroutines.launch

/**
 * Theme Settings Screen with Theme selector and Color Palettes picker.
 * Kenko-style design with animated wave decorations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBackClick: () -> Unit,
) {
    val themePreferences = LocalThemePreferences.current
    val selectedTheme by themePreferences.themeMode.collectAsState(initial = ThemeMode.System)
    val selectedPalette by themePreferences.colorPalette.collectAsState(initial = ColorPalette.Default)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text(text = "Settings") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HorizontalDivider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Theme Section
            CategoryHeader(title = "Theme")
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSelector(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                selectedTheme = selectedTheme,
                onClick = { theme ->
                    scope.launch {
                        themePreferences.setThemeMode(theme)
                    }
                },
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Color Palettes Section
            CategoryHeader(title = "Color Palettes")
            Spacer(modifier = Modifier.height(12.dp))
            ColorPaletteSelector(
                selectedColorPalette = selectedPalette,
                onClickPalette = { palette ->
                    scope.launch {
                        themePreferences.setColorPalette(palette)
                    }
                },
            )
            
            Spacer(modifier = Modifier.weight(1F))
            
            // Bottom quote
            Text(
                text = "draw together",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedWave(
            modifier = Modifier.fillMaxWidth(),
            amplitude = 6f,
            durationMillis = 5_000,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun ColorPaletteSelector(
    selectedColorPalette: ColorPalette,
    onClickPalette: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        ColorPalette.entries.forEach { colorPalette ->
            ColorPaletteItem(
                isSelected = selectedColorPalette == colorPalette,
                colorPalette = colorPalette,
                modifier = Modifier.clickable { onClickPalette(colorPalette) },
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun ColorPaletteItem(
    isSelected: Boolean,
    colorPalette: ColorPalette,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = isSelected, label = null)
    val corner by transition.animateDp(label = "") {
        if (it) 24.dp else 16.dp
    }
    val background by transition.animateColor(label = "") {
        if (it) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    }
    val contentColor by transition.animateColor(label = "") {
        if (it) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    }
    
    Column(
        modifier = Modifier
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(corner, corner, 16.dp, 16.dp)
            }
            .drawBehind { drawRect(background) }
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(80.dp)) {
            ColorPaletteSample(colorPalette = colorPalette)
            Crossfade(targetState = isSelected, label = "") {
                if (it) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = colorPalette.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun ColorPaletteSample(
    colorPalette: ColorPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .then(modifier)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Circle split into 3 parts: top-left, top-right, bottom
        Box(modifier = Modifier.size(56.dp)) {
            // Top-left color (primary) - quarter circle
            Spacer(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(topStart = 28.dp))
                    .background(colorPalette.primary),
            )
            // Top-right color (secondary) - quarter circle
            Spacer(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(topEnd = 28.dp))
                    .background(colorPalette.secondary),
            )
            // Bottom color (tertiary) - half circle
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(colorPalette.tertiary),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selectedTheme: ThemeMode,
    onClick: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        ThemeMode.entries.forEachIndexed { index, theme ->
            val isSelected = selectedTheme == theme
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                ThemeMode.entries.lastIndex -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 50.dp, bottomEnd = 50.dp)
                else -> RoundedCornerShape(4.dp)
            }
            SegmentedButton(
                selected = isSelected,
                onClick = { onClick(theme) },
                shape = shape,
                colors = themeButtonColors,
                modifier = Modifier.padding(2.dp),
            ) {
                Text(text = theme.displayName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val themeButtonColors: SegmentedButtonColors
    @Composable
    get() = SegmentedButtonDefaults.colors(
        activeBorderColor = Color.Transparent,
        inactiveBorderColor = Color.Transparent,
        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
