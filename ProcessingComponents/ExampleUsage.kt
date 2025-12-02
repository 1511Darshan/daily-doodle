package com.yourapp.processing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Example usage of the ProcessingScreen component.
 * This shows how to integrate it into your app.
 */
@Composable
fun ProcessingScreenExample(
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val viewModel: ProcessingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Initialize with sample data
    LaunchedEffect(Unit) {
        viewModel.initialize(
            listOf(
                ProcessingItem(
                    id = "app1",
                    title = "Cahier",
                    subItems = listOf(
                        ProcessingSubItem("apk", "Backup APK", "2.5 MB"),
                        ProcessingSubItem("user", "Backup USER", "1.2 MB"),
                        ProcessingSubItem("user_de", "Backup USER_DE", "0.5 MB"),
                        ProcessingSubItem("data", "Backup DATA", "10.3 MB"),
                        ProcessingSubItem("obb", "Backup OBB", "0 B"),
                        ProcessingSubItem("media", "Backup MEDIA", "5.1 MB")
                    )
                ),
                ProcessingItem(
                    id = "app2",
                    title = "My Notes",
                    subItems = listOf(
                        ProcessingSubItem("apk", "Backup APK", "5.0 MB"),
                        ProcessingSubItem("user", "Backup USER", "2.0 MB"),
                        ProcessingSubItem("data", "Backup DATA", "15.0 MB")
                    )
                ),
                ProcessingItem(
                    id = "app3",
                    title = "Calculator",
                    subItems = listOf(
                        ProcessingSubItem("apk", "Backup APK", "1.0 MB"),
                        ProcessingSubItem("data", "Backup DATA", "0.1 MB")
                    )
                )
            )
        )
    }

    ProcessingScreen(
        title = "Backup",
        finishedTitle = "Backup completed",
        finishedSubtitle = "apps backed up",
        uiState = uiState,
        onContinue = { viewModel.startProcessing() },
        onFinish = onFinish,
        onVisitDetails = { /* Navigate to details */ },
        onBack = onBack,
        itemIcon = { item ->
            // Example: Show a placeholder icon for each item
            // In a real app, you would load the actual app icon here
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6200EE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title.first().toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

/**
 * Simple demo screen to test the components
 */
@Composable
fun ProcessingDemoScreen() {
    var showProcessing by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showProcessing) {
        ProcessingScreenExample(
            onBack = { showProcessing = false },
            onFinish = { showProcessing = false }
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ProcessingColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = "Processing Demo",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(onClick = { showProcessing = true }) {
                    Text("Start Processing")
                }
            }
        }
    }
}

// Extension for mutableStateOf
private var <T> androidx.compose.runtime.MutableState<T>.value: T
    get() = this.value
    set(value) { this.value = value }
