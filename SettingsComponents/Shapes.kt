package com.example.app.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom shapes for the app.
 */
val AppShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Creates a shape with modified end (right side) corners.
 * Useful for segmented button styling.
 */
fun CornerBasedShape.end(
    bottomEnd: Dp = 0.dp,
    topEnd: Dp = bottomEnd,
): CornerBasedShape =
    copy(bottomEnd = CornerSize(bottomEnd), topEnd = CornerSize(topEnd))

/**
 * Creates a shape with modified start (left side) corners.
 * Useful for segmented button styling.
 */
fun CornerBasedShape.start(
    bottomStart: Dp = 0.dp,
    topStart: Dp = bottomStart,
): CornerBasedShape =
    copy(bottomStart = CornerSize(bottomStart), topStart = CornerSize(topStart))

/**
 * Copies end corners from another shape.
 */
fun CornerBasedShape.end(
    end: CornerBasedShape,
): CornerBasedShape =
    copy(bottomEnd = end.bottomEnd, topEnd = end.topEnd)

/**
 * Copies start corners from another shape.
 */
fun CornerBasedShape.start(
    start: CornerBasedShape,
): CornerBasedShape =
    copy(bottomStart = start.bottomStart, topStart = start.topStart)
