package com.example.dailydoodle.ui.screen.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailydoodle.data.model.Chain
import com.example.dailydoodle.ui.components.ChainCard
import com.example.dailydoodle.ui.viewmodel.FavoritesViewModel
import com.example.dailydoodle.ui.viewmodel.FavoritesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onChainClick: (String) -> Unit,
    onAddPanelClick: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    
    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Delete confirmation dialog
    var chainToDelete by remember { mutableStateOf<Chain?>(null) }
    
    chainToDelete?.let { chain ->
        AlertDialog(
            onDismissRequest = { chainToDelete = null },
            title = { Text("Move to Trash?") },
            text = { Text("\"${chain.seedPrompt.ifEmpty { "Untitled Chain" }}\" will be moved to trash. You can restore it within 30 days.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.moveToTrash(chain)
                        chainToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Move to Trash")
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
                title = {
                    Text(
                        text = "Favorites",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is FavoritesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is FavoritesUiState.Empty -> {
                EmptyFavoritesContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            
            is FavoritesUiState.Success -> {
                val chains = (uiState as FavoritesUiState.Success).chains
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = chains,
                        key = { it.id }
                    ) { chain ->
                        ChainCard(
                            chain = chain,
                            onClick = { onChainClick(chain.id) },
                            onAddPanelClick = { onAddPanelClick(chain.id) },
                            onDeleteClick = { chainToDelete = chain },
                            onFavoriteClick = { viewModel.removeFromFavorites(chain) },
                            showActions = true,
                            isOwner = chain.creatorId == currentUserId,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
            
            is FavoritesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState as FavoritesUiState.Error).message)
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Favorites Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the heart icon on any chain to add it to your favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}
