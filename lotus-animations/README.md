# Lotus App Animations & Theme Library

A collection of beautiful Jetpack Compose animations and complete theme extracted from the Lotus music player app.

## Structure

```
lotus-animations/
├── animatable/
│   └── RememberAnimatable.kt    # Saveable Animatable wrapper
├── seekbar/
│   └── WavingSeekBar.kt         # Animated wavy progress bar
├── topbar/
│   └── CollapsibleTopBar.kt     # Scroll-linked collapsible header
├── lyrics/
│   └── SyncedLyrics.kt          # Synced lyrics with animations
├── player/
│   └── DraggablePlayerSheet.kt  # Draggable bottom sheet player
├── navigation/
│   └── NavigationAnimations.kt  # Screen transition animations
├── shared/
│   └── SharedTransitionUtils.kt # Shared element transitions
├── common/
│   └── CommonAnimations.kt      # Reusable animation patterns
├── theme/
│   ├── Color.kt                 # Complete color palette (light/dark/AMOLED)
│   ├── Type.kt                  # Typography configuration
│   ├── Theme.kt                 # Main theme composable
│   ├── ThemeConfig.kt           # Theme enums and config
│   └── ColorUtils.kt            # Color utility functions
└── utils/
    └── OrientationUtils.kt      # Orientation detection
```

## Installation

1. Copy the `lotus-animations` folder to your project
2. Update package names from `com.yourapp.animations` to your actual package
3. Add required dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.compose.animation:animation:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.0")
}
```

## Usage Examples

### WavingSeekBar
```kotlin
WavingSeekBar(
    position = currentPosition,      // in milliseconds
    duration = totalDuration,        // in milliseconds
    isPlaying = isPlaying,
    onPositionChange = { newPosition ->
        player.seekTo(newPosition)
    }
)
```

### Collapsible Top Bar
```kotlin
LazyColumnWithCollapsibleTopBar(
    topBarContent = {
        Text("My App", modifier = Modifier.align(Alignment.Center))
    },
    collapseFraction = { fraction ->
        // 0 = fully expanded, 1 = fully collapsed
    }
) {
    items(myList) { item ->
        ListItem(item)
    }
}
```

### Synced Lyrics
```kotlin
SyncedLyricsLine(
    positionFlow = playbackPositionFlow,
    time = lineStartTime,
    nextTime = nextLineStartTime,
    line = "Lyrics text here",
    style = MaterialTheme.typography.headlineMedium,
    onClick = { seekTo(lineStartTime) },
    onBecomeCurrent = { scrollToThisLine() }
)
```

### Draggable Player Sheet
```kotlin
DraggablePlayerSheet(
    isExpanded = isPlayerExpanded,
    onExpandedChange = { isPlayerExpanded = it },
    onReset = { stopPlayback() },
    collapsedContent = { MiniPlayer() },
    expandedContent = { FullPlayer() }
)
```

### Navigation with Animations
```kotlin
AnimatedNavHost(
    navController = navController,
    startDestination = "home"
) {
    composable("home") { HomeScreen() }
    composable("details") { DetailsScreen() }
}
```

### Common Animation Patterns
```kotlin
// Animated sheet visibility
AnimatedSheetContent(visible = showSheet) {
    MySheet()
}

// Animated content changes
AnimatedStateContent(targetState = currentTrack) { track ->
    TrackInfo(track)
}

// Fading content
FadingContent(targetState = currentText) { text ->
    Text(text)
}
```

## Key Features

- **WavingSeekBar**: Cosine wave animation, rotating handle, RTL support
- **CollapsibleTopBar**: Nested scroll integration, spring animations
- **SyncedLyrics**: Color/scale animations, gradient progress, glow effects
- **BubblesLine**: Bouncing bubbles with infinite transitions
- **DraggablePlayerSheet**: Decay-based fling, corner radius animation
- **SharedTransitions**: Tab switching with shared element animations

## Credits

Animations and theme extracted from [Lotus](https://github.com/dn0ne/lotus) music player app.
Licensed under the same license as the original project.

---

## Theme Usage

### Basic Setup
```kotlin
// In your MainActivity or App composable
AppTheme(
    appearance = ThemeAppearance.System,  // or Light, Dark
    dynamicColor = true,                   // Use Android 12+ wallpaper colors
    amoledBlack = false                    // Pure black for OLED screens
) {
    // Your app content
    MyApp()
}

// Or use the simplified version
LotusTheme {
    MyApp()
}
```

### Color Palette
The theme includes a complete Material 3 color palette with:
- **Primary**: Green tones (nature/lotus inspired)
- **Secondary**: Coral/Salmon tones  
- **Tertiary**: Teal tones
- **Light theme**: Warm, off-white backgrounds
- **Dark theme**: Deep, greenish-gray backgrounds
- **AMOLED theme**: Pure black backgrounds for OLED screens

### Using Colors
```kotlin
// Access theme colors
val primary = MaterialTheme.colorScheme.primary
val surface = MaterialTheme.colorScheme.surfaceContainer

// Use utility functions
val darkened = myColor.darken(0.2f)
val lightened = myColor.lighten(0.2f)
val semiTransparent = myColor.withAlpha(0.5f)

// Get lyrics colors
val (containerColor, contentColor) = getLyricsColors(
    useDarkPalette = true,
    amoledDarkTheme = false
)
```

### Typography
Uses Material 3 typography scale with system default font (Roboto).
To use a custom font, see comments in `Type.kt`.

```kotlin
Text(
    text = "Title",
    style = MaterialTheme.typography.headlineMedium
)

Text(
    text = "Body text",
    style = MaterialTheme.typography.bodyLarge
)
```
