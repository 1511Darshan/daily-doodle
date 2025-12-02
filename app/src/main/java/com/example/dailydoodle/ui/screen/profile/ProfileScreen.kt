package com.example.dailydoodle.ui.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.ui.viewmodel.AuthViewModel

/**
 * Golden ratio for card aspect ratios
 */
private const val PHI = 16F / 10F

/**
 * Standard border width for Kenko-style borders
 */
private val KenkoBorderWidth = 1.4.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onShopClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var doodleCount by remember { mutableStateOf(0) }
    
    // Get Firebase Auth UID directly
    val firebaseUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    
    // Fetch user's doodle count from their user document
    LaunchedEffect(firebaseUserId) {
        firebaseUserId?.let { userId ->
            android.util.Log.d("ProfileScreen", "Fetching doodle count for userId: $userId")
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                // Listen to user document for real-time updates
                firestore.collection("users").document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("ProfileScreen", "Error listening to user doc: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            doodleCount = snapshot.getLong("panelCount")?.toInt() ?: 0
                            android.util.Log.d("ProfileScreen", "User has $doodleCount doodles")
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Error counting doodles: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "You") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Current Plan/Profile Card
            CurrentPlanCard(
                userName = currentUser?.displayName ?: "Artist",
                email = currentUser?.email ?: "",
                streak = currentUser?.streak ?: 0,
                doodlesCount = doodleCount,
                onEditClick = { /* Navigate to edit profile */ },
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats Cards Row
            StatsCardsRow(
                doodlesCount = doodleCount,
                onCreateClick = onShopClick, // Navigate to create/shop
                onDoodlesClick = { /* Navigate to my doodles */ },
            )
            
            Spacer(modifier = Modifier.weight(1F))
            
            // Bottom quote
            Text(
                text = "draw together",
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
    userName: String,
    email: String,
    streak: Int,
    doodlesCount: Int,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.secondary
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PHI),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(KenkoBorderWidth, borderColor),
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
                // Doodle/artist icon
                Text(
                    text = "🎨",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Current Artist",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.weight(1F))
                FilledIconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
            
            HorizontalDivider(
                thickness = KenkoBorderWidth,
                color = borderColor,
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
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                        LocalContentColor provides MaterialTheme.colorScheme.outline,
                    ) {
                        Text(text = "$doodlesCount doodles")
                        Text(text = "$streak day streak")
                        Text(text = email)
                    }
                }
                
                // Decorative wave icon
                Icon(
                    imageVector = StackIcon,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.offset(x = 0.dp),
                )
            }
        }
    }
}

@Composable
private fun StatsCardsRow(
    doodlesCount: Int,
    onCreateClick: () -> Unit,
    onDoodlesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.secondary
    
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Doodles count card (left side)
        val leftShape = RoundedCornerShape(
            topStart = 28.dp,
            bottomStart = 28.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp
        )
        Surface(
            modifier = Modifier.weight(1.5F),
            shape = leftShape,
            border = BorderStroke(KenkoBorderWidth, borderColor),
            onClick = onDoodlesClick,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Doodles",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = doodlesCount.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
        
        // Add/Create button (right side)
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
                .clickable(onClick = onCreateClick)
                .border(
                    border = BorderStroke(KenkoBorderWidth, borderColor),
                    shape = rightShape,
                )
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = "Create",
            )
        }
    }
}

/**
 * Decorative stacked wave/diamond icon shown on the Current Plan card.
 */
private val StackIcon: ImageVector
    get() {
        if (_stack != null) {
            return _stack!!
        }
        _stack = ImageVector.Builder(
            name = "Stack",
            defaultWidth = 65.dp,
            defaultHeight = 100.dp,
            viewportWidth = 65f,
            viewportHeight = 100f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(91.0f, 39.814f)
                curveTo(91.0f, 37.94f, 89.368f, 36.224f, 86.421f, 34.983f)
                lineTo(73.658f, 29.625f)
                lineTo(86.421f, 24.266f)
                curveTo(89.368f, 23.025f, 91.0f, 21.309f, 91.0f, 19.435f)
                curveTo(91.0f, 17.561f, 89.368f, 15.845f, 86.421f, 14.604f)
                lineTo(55.842f, 1.802f)
                curveTo(50.132f, -0.601f, 40.842f, -0.601f, 35.132f, 1.802f)
                lineTo(4.579f, 14.604f)
                curveTo(1.632f, 15.845f, 0.0f, 17.561f, 0.0f, 19.435f)
                curveTo(0.0f, 21.309f, 1.632f, 23.025f, 4.579f, 24.266f)
                lineTo(17.342f, 29.625f)
                lineTo(4.579f, 34.983f)
                curveTo(1.632f, 36.224f, 0.0f, 37.94f, 0.0f, 39.814f)
                curveTo(0.0f, 41.688f, 1.632f, 43.404f, 4.579f, 44.645f)
                lineTo(17.342f, 50.003f)
                lineTo(4.579f, 55.362f)
                curveTo(1.632f, 56.603f, 0.0f, 58.319f, 0.0f, 60.193f)
                curveTo(0.0f, 62.067f, 1.632f, 63.783f, 4.579f, 65.023f)
                lineTo(17.342f, 70.382f)
                lineTo(4.579f, 75.741f)
                curveTo(1.632f, 76.981f, 0.0f, 78.697f, 0.0f, 80.572f)
                curveTo(0.0f, 82.446f, 1.632f, 84.162f, 4.579f, 85.402f)
                lineTo(35.132f, 98.205f)
                curveTo(37.974f, 99.393f, 41.737f, 100.0f, 45.474f, 100.0f)
                curveTo(49.21f, 100.0f, 52.974f, 99.393f, 55.816f, 98.205f)
                lineTo(86.368f, 85.402f)
                curveTo(89.316f, 84.162f, 90.947f, 82.446f, 90.947f, 80.572f)
                curveTo(90.947f, 78.697f, 89.316f, 76.981f, 86.368f, 75.741f)
                lineTo(73.605f, 70.382f)
                lineTo(86.368f, 65.023f)
                curveTo(89.316f, 63.783f, 90.947f, 62.067f, 90.947f, 60.193f)
                curveTo(90.947f, 58.319f, 89.316f, 56.603f, 86.368f, 55.362f)
                lineTo(73.632f, 50.003f)
                lineTo(86.395f, 44.645f)
                curveTo(89.368f, 43.404f, 91.0f, 41.688f, 91.0f, 39.814f)
                close()
                moveTo(5.079f, 23.052f)
                curveTo(2.684f, 22.048f, 1.289f, 20.729f, 1.289f, 19.435f)
                curveTo(1.289f, 18.142f, 2.658f, 16.822f, 5.079f, 15.819f)
                lineTo(35.658f, 3.016f)
                curveTo(38.368f, 1.881f, 41.947f, 1.3f, 45.5f, 1.3f)
                curveTo(49.079f, 1.3f, 52.632f, 1.881f, 55.342f, 3.016f)
                lineTo(85.895f, 15.819f)
                curveTo(88.289f, 16.822f, 89.684f, 18.142f, 89.684f, 19.435f)
                curveTo(89.684f, 20.729f, 88.316f, 22.048f, 85.895f, 23.052f)
                lineTo(71.921f, 28.912f)
                lineTo(55.842f, 22.18f)
                curveTo(50.132f, 19.778f, 40.842f, 19.778f, 35.132f, 22.18f)
                lineTo(19.053f, 28.912f)
                lineTo(5.079f, 23.052f)
                close()
                moveTo(70.237f, 29.625f)
                lineTo(55.342f, 35.854f)
                curveTo(49.921f, 38.125f, 41.079f, 38.125f, 35.658f, 35.854f)
                lineTo(20.763f, 29.625f)
                lineTo(35.658f, 23.395f)
                curveTo(38.368f, 22.26f, 41.947f, 21.679f, 45.5f, 21.679f)
                curveTo(49.079f, 21.679f, 52.632f, 22.26f, 55.342f, 23.395f)
                lineTo(70.237f, 29.625f)
                close()
                moveTo(85.921f, 76.955f)
                curveTo(88.316f, 77.958f, 89.711f, 79.278f, 89.711f, 80.572f)
                curveTo(89.711f, 81.865f, 88.342f, 83.185f, 85.921f, 84.188f)
                lineTo(55.342f, 96.991f)
                curveTo(49.921f, 99.261f, 41.079f, 99.261f, 35.658f, 96.991f)
                lineTo(5.079f, 84.188f)
                curveTo(2.684f, 83.185f, 1.289f, 81.865f, 1.289f, 80.572f)
                curveTo(1.289f, 79.278f, 2.658f, 77.958f, 5.079f, 76.955f)
                lineTo(19.053f, 71.095f)
                lineTo(35.132f, 77.826f)
                curveTo(37.974f, 79.014f, 41.737f, 79.621f, 45.474f, 79.621f)
                curveTo(49.21f, 79.621f, 52.974f, 79.014f, 55.816f, 77.826f)
                lineTo(71.895f, 71.095f)
                lineTo(85.921f, 76.955f)
                close()
                moveTo(20.763f, 70.382f)
                lineTo(35.658f, 64.152f)
                curveTo(38.368f, 63.017f, 41.947f, 62.437f, 45.5f, 62.437f)
                curveTo(49.079f, 62.437f, 52.632f, 63.017f, 55.342f, 64.152f)
                lineTo(70.237f, 70.382f)
                lineTo(55.342f, 76.612f)
                curveTo(49.921f, 78.882f, 41.079f, 78.882f, 35.658f, 76.612f)
                lineTo(20.763f, 70.382f)
                close()
                moveTo(85.921f, 56.576f)
                curveTo(88.316f, 57.579f, 89.711f, 58.899f, 89.711f, 60.193f)
                curveTo(89.711f, 61.486f, 88.342f, 62.806f, 85.921f, 63.809f)
                lineTo(71.947f, 69.669f)
                lineTo(55.842f, 62.938f)
                curveTo(50.132f, 60.536f, 40.842f, 60.536f, 35.132f, 62.938f)
                lineTo(19.053f, 69.669f)
                lineTo(5.079f, 63.809f)
                curveTo(2.684f, 62.806f, 1.289f, 61.486f, 1.289f, 60.193f)
                curveTo(1.289f, 58.899f, 2.658f, 57.579f, 5.079f, 56.576f)
                lineTo(19.053f, 50.716f)
                lineTo(35.132f, 57.447f)
                curveTo(37.974f, 58.635f, 41.737f, 59.242f, 45.474f, 59.242f)
                curveTo(49.21f, 59.242f, 52.974f, 58.635f, 55.816f, 57.447f)
                lineTo(71.895f, 50.716f)
                lineTo(85.921f, 56.576f)
                close()
                moveTo(20.763f, 50.003f)
                lineTo(35.658f, 43.773f)
                curveTo(38.368f, 42.638f, 41.947f, 42.058f, 45.5f, 42.058f)
                curveTo(49.079f, 42.058f, 52.632f, 42.638f, 55.342f, 43.773f)
                lineTo(70.237f, 50.003f)
                lineTo(55.342f, 56.233f)
                curveTo(49.921f, 58.503f, 41.079f, 58.503f, 35.658f, 56.233f)
                lineTo(20.763f, 50.003f)
                close()
                moveTo(71.947f, 49.291f)
                lineTo(55.868f, 42.559f)
                curveTo(50.158f, 40.157f, 40.868f, 40.157f, 35.158f, 42.559f)
                lineTo(19.079f, 49.291f)
                lineTo(5.105f, 43.43f)
                curveTo(2.711f, 42.427f, 1.316f, 41.107f, 1.316f, 39.814f)
                curveTo(1.316f, 38.52f, 2.684f, 37.201f, 5.105f, 36.197f)
                lineTo(19.079f, 30.337f)
                lineTo(35.158f, 37.069f)
                curveTo(38.0f, 38.257f, 41.763f, 38.864f, 45.5f, 38.864f)
                curveTo(49.237f, 38.864f, 53.0f, 38.257f, 55.842f, 37.069f)
                lineTo(71.921f, 30.337f)
                lineTo(85.895f, 36.197f)
                curveTo(88.289f, 37.201f, 89.684f, 38.52f, 89.684f, 39.814f)
                curveTo(89.684f, 41.107f, 88.316f, 42.427f, 85.895f, 43.43f)
                lineTo(71.947f, 49.291f)
                close()
            }
        }.build()
        return _stack!!
    }

private var _stack: ImageVector? = null
