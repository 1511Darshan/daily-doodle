# Processing Components for Android App

This folder contains all the components needed to implement a processing/backup progress screen similar to the Android-DataBackup app.

## Files Overview

### Kotlin Files

| File | Description |
|------|-------------|
| `OperationState.kt` | Enum defining the states: IDLE, PROCESSING, DONE, ERROR, SKIP |
| `ProcessingModels.kt` | Data classes for ProcessingItem, ProcessingSubItem, ProcessingTask, and ProcessingUiState |
| `SegmentCircularProgressIndicator.kt` | The segmented circular progress indicator component |
| `ProcessingCard.kt` | Expandable card showing each item being processed |
| `ProcessingScreen.kt` | The main processing screen component |
| `AnimatedNavHost.kt` | Navigation wrapper with slide animations |
| `ProcessingViewModel.kt` | Example ViewModel demonstrating how to manage processing state |
| `ExampleUsage.kt` | Example showing how to use the ProcessingScreen |

### Resource Files

| File | Description |
|------|-------------|
| `res/drawable/ic_animated_tick.xml` | Animated checkmark for completion |
| `res/drawable/ic_hourglass_empty.xml` | Hourglass icon for preprocessing/postprocessing |

## Setup

1. Copy all `.kt` files to your project's source folder
2. Copy the `res/drawable` files to your app's `res/drawable` folder
3. Update the package name from `com.yourapp.processing` to match your app

## Required Dependencies

Add these to your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.animation:animation:1.5.0")
    implementation("androidx.compose.animation:animation-graphics:1.5.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
}
```

## Usage Example

```kotlin
@Composable
fun MyBackupScreen() {
    val viewModel: ProcessingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize with your items
    LaunchedEffect(Unit) {
        viewModel.initialize(
            listOf(
                ProcessingItem(
                    id = "item1",
                    title = "My App",
                    subItems = listOf(
                        ProcessingSubItem("step1", "Step 1"),
                        ProcessingSubItem("step2", "Step 2")
                    )
                )
            )
        )
    }
    
    ProcessingScreen(
        title = "Saving",
        finishedTitle = "Save completed",
        finishedSubtitle = "items saved",
        uiState = uiState,
        onContinue = { viewModel.startProcessing() },
        onFinish = { /* Navigate away */ },
        onBack = { /* Handle back */ }
    )
}
```

## Navigation with Animations

Use `AnimatedNavHost` for smooth page transitions:

```kotlin
val navController = rememberNavController()

AnimatedNavHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") { HomeScreen() }
    composable("processing") { ProcessingScreen(...) }
}
```

## Customization

### Colors
Modify `ProcessingColors` object in `ProcessingScreen.kt`:

```kotlin
object ProcessingColors {
    val Background = Color(0xFF121212)      // Dark background
    val Primary = Color.White               // Primary accent
    val SuccessGreen = Color(0xFF4CAF50)    // Success state
    // ... etc
}
```

### Progress Indicator
Customize the `SegmentCircularProgressIndicator`:

```kotlin
SegmentCircularProgressIndicator(
    segments = 6,           // Number of segments
    progress = 0.5f,        // 0f to 1f
    size = 152.dp,          // Indicator size
    strokeWidth = 8.dp,     // Stroke thickness
    trackColor = Color.Gray,
    progressColor = Color.White
)
```

## Screen Flow

1. **IDLE State**: Shows "Continue" button, preprocessing phase displayed
2. **PROCESSING State**: Button disabled, shows current item being processed
3. **DONE State**: Shows "Finish" and "Visit details" buttons, animated checkmark

## Animation Details

- **Page Transitions**: Horizontal slide with spring animation
- **Progress Indicator**: 800ms animated fill with 100ms delay
- **State Changes**: AnimatedContent for smooth icon/text transitions
- **Expand/Collapse**: AnimatedVisibility for card expansion
