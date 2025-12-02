package com.example.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// Import your custom icons:
// import com.example.app.ui.icons.StackIcon
// import com.example.app.ui.icons.PlanIcon

/**
 * Golden ratio for card aspect ratios
 */
const val PHI = 16F / 10F

/**
 * Profile/You screen with Current Plan card and Exercise card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    planName: String = "3-Day Full Body (Strength Focus)",
    exercises: Int = 0,
    workDays: Int = 0,
    restDays: Int = 7,
    numberOfExercises: Int = 36,
    onBackPress: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPlanClick: () -> Unit = {},
    onAddExerciseClick: () -> Unit = {},
    onExercisesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "You") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Current Plan Card
            CurrentPlanCard(
                onPlanClick = onPlanClick,
                name = planName,
                exercises = exercises,
                workDays = workDays,
                restDays = restDays,
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Exercise Card
            ExerciseCard(
                numberOfExercises = numberOfExercises,
                onAddClick = onAddExerciseClick,
                onExercisesClick = onExercisesClick,
            )
            
            Spacer(modifier = Modifier.weight(1F))
            
            // Bottom quote
            Text(
                text = "health is wealth",
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
private fun CurrentPlanCard(
    onPlanClick: () -> Unit,
    name: String,
    exercises: Int,
    workDays: Int,
    restDays: Int,
    modifier: Modifier = Modifier,
) {
    val borderWidth = 1.4.dp
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PHI),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        border = androidx.compose.foundation.BorderStroke(
            borderWidth,
            MaterialTheme.colorScheme.secondary
        ),
        onClick = onPlanClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 2.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Replace with your plan icon
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_agenda),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Current Plan",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.weight(1F))
                FilledIconButton(onClick = onPlanClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            
            HorizontalDivider(
                thickness = borderWidth,
                color = MaterialTheme.colorScheme.secondary,
            )
            
            // Content row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp),
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                        LocalContentColor provides MaterialTheme.colorScheme.outline,
                    ) {
                        Text(text = "$exercises exercises")
                        Text(text = "${workDays.toString().padStart(2, '0')} days")
                        Text(text = "${restDays.toString().padStart(2, '0')} rest days")
                    }
                }
                
                // Decorative wave icon
                Icon(
                    imageVector = StackIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.offset(x = 0.dp),
                )
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    numberOfExercises: Int,
    onAddClick: () -> Unit,
    onExercisesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderWidth = 1.4.dp
    
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Exercise count card (left side)
        val leftShape = RoundedCornerShape(
            topStart = 28.dp,
            bottomStart = 28.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp
        )
        Surface(
            modifier = Modifier.weight(1.5F),
            shape = leftShape,
            border = androidx.compose.foundation.BorderStroke(
                borderWidth,
                MaterialTheme.colorScheme.secondary
            ),
            onClick = onExercisesClick,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Exercise",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = numberOfExercises.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
        
        // Add button (right side)
        val rightShape = RoundedCornerShape(
            topStart = 16.dp,
            bottomStart = 16.dp,
            topEnd = 28.dp,
            bottomEnd = 28.dp
        )
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight()
                .clip(rightShape)
                .clickable(onClick = onAddClick)
                .border(
                    border = androidx.compose.foundation.BorderStroke(
                        borderWidth,
                        MaterialTheme.colorScheme.secondary
                    ),
                    shape = rightShape,
                )
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = "Add",
            )
        }
    }
}
