package com.example.dailydoodle.ui.components.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Data class representing a single navigation item
 */
data class CustomBottomNavigationItem(
    val icon: ImageVector,
    @StringRes val description: Int,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

/**
 * Default values for navigation bar sizing
 */
object DefaultNavigationValues {
    val containerHeight: Dp = 58.dp
}

/**
 * Convert Dp to pixels
 */
@Composable
fun Dp.toPx(): Int {
    val density = LocalDensity.current
    return with(density) { this@toPx.roundToPx() }
}

/**
 * Single navigation item with animated color
 */
@Composable
fun CustomBottomNavigationItem(properties: CustomBottomNavigationItem) {
    val contentColor by animateColorAsState(
        targetValue = if (properties.isSelected) 
            MaterialTheme.colorScheme.onPrimary 
        else 
            MaterialTheme.colorScheme.onSecondaryContainer,
        label = "contentColor"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { properties.onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = properties.icon,
            contentDescription = stringResource(properties.description),
            tint = contentColor
        )
    }
}

/**
 * Custom bottom navigation bar with animated indicator
 * 
 * @param items List of navigation items to display
 */
@Composable
fun CustomBottomNavigation(items: List<CustomBottomNavigationItem>) {
    var lastIndex by remember { mutableIntStateOf(0) }
    val selectedIndex = items.indexOfFirst { it.isSelected }.let {
        if (it == -1) lastIndex else it
    }

    LaunchedEffect(selectedIndex) {
        lastIndex = if (selectedIndex == -1) {
            lastIndex
        } else {
            selectedIndex
        }
    }

    val containerPaddings = 4.dp
    val spacerBetween = 6.dp
    val buttonWidth = DefaultNavigationValues.containerHeight.toPx() - containerPaddings.toPx() * 2

    val indicatorOffset by animateIntAsState(
        targetValue = lastIndex * (buttonWidth + spacerBetween.toPx()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "indicatorOffset"
    )

    Surface(
        shape = CircleShape,
        shadowElevation = 1.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .height(DefaultNavigationValues.containerHeight)
                .clip(shape = CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(containerPaddings),
        ) {
            // Background Indicator - animated sliding circle
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .offset { IntOffset(indicatorOffset, 0) }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            // Navigation items row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacerBetween)
            ) {
                items.forEach {
                    CustomBottomNavigationItem(properties = it)
                }
            }
        }
    }
}
