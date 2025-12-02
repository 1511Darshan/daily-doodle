package com.example.dailydoodle.ui.screen.feed

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.R
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.repository.ChainFilter
import com.example.dailydoodle.ui.admob.AdConfig
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.admob.NativeAdCard
import com.example.dailydoodle.ui.components.ChainCard
import com.example.dailydoodle.ui.components.TickerText
import com.example.dailydoodle.ui.components.onboarding.OnboardingDialog
import com.example.dailydoodle.ui.viewmodel.FeedViewModel
import com.example.dailydoodle.ui.viewmodel.FeedUiState

private const val PREFS_NAME = "dailydoodle_prefs"
private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding_dialog"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onChainClick: (String) -> Unit,
    onAddPanelClick: (String) -> Unit,
    onCreateChainClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    
    // Onboarding dialog state
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var showOnboardingDialog by remember { 
        mutableStateOf(!prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)) 
    }
    
    // Delete confirmation dialog state
    var chainToDelete by remember { mutableStateOf<Chain?>(null) }
    
    // Snackbar for error messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error snackbar
    LaunchedEffect(deleteError) {
        deleteError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearDeleteError()
        }
    }
    
    // Onboarding dialog
    if (showOnboardingDialog) {
        OnboardingDialog(
            onDismiss = {
                showOnboardingDialog = false
                prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
            },
            onFinish = {
                showOnboardingDialog = false
                prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
            }
        )
    }

    // Delete confirmation dialog
    chainToDelete?.let { chain ->
        AlertDialog(
            onDismissRequest = { chainToDelete = null },
            title = { Text("Delete Chain?") },
            text = { Text("Are you sure you want to delete \"${chain.seedPrompt.ifEmpty { "Untitled Chain" }}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChain(chain)
                        chainToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { chainToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Kenko-style top bar with app name and settings
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_app_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                        )
                        Text(
                            text = "DAILYDOODLE",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrolling marquee ticker at top
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            TickerText(
                texts = arrayOf("Create", "Draw", "Share", "Inspire", "Connect"),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == ChainFilter.RECENT,
                    onClick = { viewModel.loadChains(ChainFilter.RECENT) },
                    label = { Text("Recent") }
                )
                FilterChip(
                    selected = selectedFilter == ChainFilter.POPULAR,
                    onClick = { viewModel.loadChains(ChainFilter.POPULAR) },
                    label = { Text("Popular") }
                )
                FilterChip(
                    selected = selectedFilter == ChainFilter.FEATURED,
                    onClick = { viewModel.loadChains(ChainFilter.FEATURED) },
                    label = { Text("Featured") }
                )
            }

            when (uiState) {
                is FeedUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is FeedUiState.Empty -> {
                    // Kenko-style empty state with big headline
                    EmptyFeedContent(onCreateChainClick = onCreateChainClick)
                }
                is FeedUiState.Success -> {
                    ChainList(
                        chains = (uiState as FeedUiState.Success).chains,
                        currentUserId = currentUserId,
                        onChainClick = onChainClick,
                        onAddPanelClick = onAddPanelClick,
                        onDeleteClick = { chain -> chainToDelete = chain },
                        onFavoriteClick = { chain -> viewModel.toggleFavorite(chain) },
                        onCreateChainClick = onCreateChainClick
                    )
                }
                is FeedUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((uiState as FeedUiState.Error).message)
                    }
                }
            }
        }
    }
}

/**
 * Kenko-style empty state with large headline and CTA button
 */
@Composable
private fun ColumnScope.EmptyFeedContent(
    onCreateChainClick: () -> Unit
) {
    Spacer(modifier = Modifier.weight(1f))
    
    // Large headline text
    Text(
        modifier = Modifier
            .padding(horizontal = 24.dp),
        text = "Start by Creating a Chain",
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 64.sp,
            lineHeight = 60.sp,
            fontWeight = FontWeight.Bold,
            lineBreak = LineBreak.Heading,
        ),
        color = MaterialTheme.colorScheme.primary,
    )
    
    Spacer(modifier = Modifier.weight(1f))
    
    // Create Chain button
    Button(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        onClick = onCreateChainClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = PaddingValues(
            vertical = 20.dp,
            horizontal = 32.dp,
        ),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = "Create Chain",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
        )
    }
    
    // Bottom tagline
    Text(
        text = "draw together",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = 24.dp),
    )
}

@Composable
fun ChainList(
    chains: List<Chain>,
    currentUserId: String?,
    onChainClick: (String) -> Unit,
    onAddPanelClick: (String) -> Unit,
    onDeleteClick: (Chain) -> Unit,
    onFavoriteClick: (Chain) -> Unit,
    onCreateChainClick: () -> Unit
) {
    // Calculate positions where ads should appear
    // Per PRD: Native ads at position 4 and 12 (0-indexed: 3 and 11)
    val adPositions = AdConfig.NATIVE_AD_FEED_POSITIONS
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp // Extra padding for bottom navigation bar
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(
            items = chains,
            key = { _, chain -> chain.id }
        ) { index, chain ->
            // Show native ad before this item if it's at an ad position
            if (adPositions.contains(index) && !AdMobManager.isAdFreeUser()) {
                NativeAdCard(
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            ChainCard(
                chain = chain,
                onClick = { onChainClick(chain.id) },
                onAddPanelClick = { onAddPanelClick(chain.id) },
                onDeleteClick = { onDeleteClick(chain) },
                onFavoriteClick = { onFavoriteClick(chain) },
                showActions = true,
                isOwner = chain.creatorId == currentUserId,
                modifier = Modifier.animateItem()
            )
        }
    }
}
