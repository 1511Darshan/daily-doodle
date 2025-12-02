package com.example.dailydoodle.ui.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.dailydoodle.R

/**
 * Enum representing the different navigation sections in the app
 */
enum class NavigationSection {
    MAIN,
    ARCHIVE,
    TRASH,
    SETTINGS
}

/**
 * Floating Action Button for creating new doodles
 */
@Composable
fun CreateDoodleFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.create_new_doodle),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Main bottom app bar provider that contains the navigation bar and FAB
 * 
 * @param currentSection The currently selected navigation section
 * @param onSectionSelected Callback when a navigation section is selected
 * @param onCreateClick Callback when the create FAB is clicked
 * @param modifier Modifier for the container
 */
@Composable
fun BottomAppBarProvider(
    currentSection: NavigationSection,
    onSectionSelected: (NavigationSection) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible by AppNavigationBarState.isVisible

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation Bar
                CustomBottomNavigation(
                    items = listOf(
                        CustomBottomNavigationItem(
                            icon = Icons.Filled.Home,
                            description = R.string.nav_doodles,
                            isSelected = currentSection == NavigationSection.MAIN,
                            onClick = { onSectionSelected(NavigationSection.MAIN) }
                        ),
                        CustomBottomNavigationItem(
                            icon = Icons.Filled.Star,
                            description = R.string.nav_archive,
                            isSelected = currentSection == NavigationSection.ARCHIVE,
                            onClick = { onSectionSelected(NavigationSection.ARCHIVE) }
                        ),
                        CustomBottomNavigationItem(
                            icon = Icons.Filled.Delete,
                            description = R.string.nav_trash,
                            isSelected = currentSection == NavigationSection.TRASH,
                            onClick = { onSectionSelected(NavigationSection.TRASH) }
                        ),
                        CustomBottomNavigationItem(
                            icon = Icons.Filled.Settings,
                            description = R.string.nav_settings,
                            isSelected = currentSection == NavigationSection.SETTINGS,
                            onClick = { onSectionSelected(NavigationSection.SETTINGS) }
                        )
                    )
                )

                // FAB
                CreateDoodleFAB(
                    onClick = onCreateClick,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }
        }
    }
}
