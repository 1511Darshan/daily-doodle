package com.example.dailydoodle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Large screen title with underline decoration.
 * Matches the Lotus app style - big text with a colored underline.
 */
@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    underlineColor: Color = MaterialTheme.colorScheme.primary,
    underlineWidth: Dp = 48.dp,
    underlineHeight: Dp = 3.dp
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Underline
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(underlineWidth)
                .height(underlineHeight)
                .clip(RoundedCornerShape(underlineHeight / 2))
                .background(underlineColor)
        )
    }
}

/**
 * Large screen title without underline - for use in TopAppBar
 */
@Composable
fun LargeTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

/**
 * Section title with smaller underline
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    underlineColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Underline
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(32.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(underlineColor)
        )
    }
}
