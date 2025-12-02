package com.example.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard border width for Kenko-style borders
 */
val KenkoBorderWidth: Dp = 1.4.dp

/**
 * Primary colored border
 */
val PrimaryBorder: BorderStroke
    @Composable
    get() = BorderStroke(KenkoBorderWidth, MaterialTheme.colorScheme.primary)

/**
 * Secondary colored border (used on cards)
 */
val SecondaryBorder: BorderStroke
    @Composable
    get() = BorderStroke(KenkoBorderWidth, MaterialTheme.colorScheme.secondary)

/**
 * Outline/secondary colored border
 */
val OutlineBorder: BorderStroke
    @Composable
    get() = BorderStroke(KenkoBorderWidth, MaterialTheme.colorScheme.secondary)

/**
 * On-surface colored border
 */
val OnSurfaceBorder: BorderStroke
    @Composable
    get() = BorderStroke(KenkoBorderWidth, MaterialTheme.colorScheme.onSurface)

/**
 * On-surface variant colored border
 */
val OnSurfaceVariantBorder: BorderStroke
    @Composable
    get() = BorderStroke(KenkoBorderWidth, MaterialTheme.colorScheme.onSurfaceVariant)
