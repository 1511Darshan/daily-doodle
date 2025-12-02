package com.example.app.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout

/**
 * Golden ratio constant for card aspect ratios
 */
const val PHI = 16F / 10F

/**
 * Formats an integer with leading zeros.
 * e.g., normalizeInt(5) = "05"
 */
@Composable
fun normalizeInt(value: Int, padding: Char = '0', length: Int = 2): String {
    return remember(value) {
        value.toString().padStart(length, padding)
    }
}

/**
 * Rotates content to display vertically.
 * Used for vertical text labels.
 */
fun Modifier.vertical(towardsRight: Boolean = true) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }.rotate(90F * (if (towardsRight) 1 else -1))
