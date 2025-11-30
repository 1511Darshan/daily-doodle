package com.example.dailydoodle.ui.screen.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.R
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.data.repository.ChainFilter
import com.example.dailydoodle.ui.admob.AdConfig
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.admob.NativeAdCard
import com.example.dailydoodle.ui.components.ChainCard
import com.example.dailydoodle.ui.viewmodel.FeedViewModel
import com.example.dailydoodle.ui.viewmodel.FeedUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onChainClick: (String) -> Unit,
    onAddPanelClick: (String) -> Unit,
    onCreateChainClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    
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
            TopAppBar(
                title = { Text("Daily Doodle Chain") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_person),
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateChainClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = "Create Chain",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No chains yet. Be the first to create one!")
                    }
                }
                is FeedUiState.Success -> {
                    ChainList(
                        chains = (uiState as FeedUiState.Success).chains,
                        currentUserId = currentUserId,
                        onChainClick = onChainClick,
                        onAddPanelClick = onAddPanelClick,
                        onDeleteClick = { chain -> chainToDelete = chain },
                        onFavoriteClick = { chain -> viewModel.toggleFavorite(chain) }
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

@Composable
fun ChainList(
    chains: List<Chain>,
    currentUserId: String?,
    onChainClick: (String) -> Unit,
    onAddPanelClick: (String) -> Unit,
    onDeleteClick: (Chain) -> Unit,
    onFavoriteClick: (Chain) -> Unit
) {
    // Calculate positions where ads should appear
    // Per PRD: Native ads at position 4 and 12 (0-indexed: 3 and 11)
    val adPositions = AdConfig.NATIVE_AD_FEED_POSITIONS
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(chains) { index, chain ->
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
                isOwner = chain.creatorId == currentUserId
            )
        }
    }
}
