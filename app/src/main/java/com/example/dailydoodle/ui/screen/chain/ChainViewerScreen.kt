package com.example.dailydoodle.ui.screen.chain

import androidx.compose.foundation.layout.*
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dailydoodle.data.model.Panel
import com.example.dailydoodle.ui.admob.AdMobManager
import com.example.dailydoodle.ui.viewmodel.ChainViewModel
import com.example.dailydoodle.util.Analytics
import com.example.dailydoodle.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainViewerScreen(
    chainId: String,
    onAddPanelClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ChainViewModel = viewModel()
) {
    val chain by viewModel.chain.collectAsState()
    val panels by viewModel.panels.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var panelViewCount by remember { mutableStateOf(0) }

    LaunchedEffect(chainId) {
        viewModel.loadChain(chainId)
    }

    // Load interstitial ad
    LaunchedEffect(Unit) {
        AdMobManager.loadInterstitialAd(context)
    }

    // Show interstitial after viewing 5 panels
    LaunchedEffect(panelViewCount) {
        if (panelViewCount > 0 && panelViewCount % 5 == 0) {
            AdMobManager.showInterstitialAd(
                context = context,
                onAdClosed = {
                    Analytics.logInterstitialAdShown()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chain?.seedPrompt ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (panels.isNotEmpty()) {
                        IconButton(onClick = {
                            ShareUtils.shareChain(context, chainId, panels)
                            Analytics.logPanelShared(chainId)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (chain?.status == com.example.dailydoodle.data.model.ChainStatus.OPEN) {
                FloatingActionButton(onClick = onAddPanelClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Panel")
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is com.example.dailydoodle.ui.viewmodel.ChainUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.example.dailydoodle.ui.viewmodel.ChainUiState.Success -> {
                if (panels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("No panels yet. Be the first to add one!")
                            Button(onClick = onAddPanelClick) {
                                Text("Add First Panel")
                            }
                        }
                    }
                } else {
                    PanelPager(
                        panels = panels,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        onPageChanged = { panelViewCount++ }
                    )
                }
            }
            is com.example.dailydoodle.ui.viewmodel.ChainUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState as com.example.dailydoodle.ui.viewmodel.ChainUiState.Error).message)
                }
            }
        }
    }
}

@Composable
fun PanelPager(
    panels: List<Panel>,
    modifier: Modifier = Modifier,
    onPageChanged: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 0)

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged()
    }

    HorizontalPager(
        count = panels.size,
        state = pagerState,
        modifier = modifier
    ) { page ->
        PanelView(
            panel = panels[page],
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PanelView(
    panel: Panel,
    modifier: Modifier = Modifier
) {
    // Log the image URL for debugging
    android.util.Log.d("PanelView", "Loading panel ${panel.id}, imageUrl: ${panel.imageUrl}")
    
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (panel.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(panel.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = panel.caption,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onLoading = {
                            isLoading = true
                            loadError = null
                            android.util.Log.d("PanelView", "Loading image: ${panel.imageUrl}")
                        },
                        onError = { error ->
                            isLoading = false
                            loadError = error.result.throwable.message ?: "Unknown error"
                            android.util.Log.e("PanelView", "Failed to load image: ${panel.imageUrl}", error.result.throwable)
                        },
                        onSuccess = {
                            isLoading = false
                            loadError = null
                            android.util.Log.d("PanelView", "Successfully loaded image: ${panel.imageUrl}")
                        }
                    )
                    
                    // Show loading indicator
                    if (isLoading) {
                        CircularProgressIndicator()
                    }
                    
                    // Show error message
                    if (loadError != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Failed to load image",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = loadError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Show placeholder if no image URL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No image available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (panel.caption.isNotEmpty()) {
                Text(
                    text = panel.caption,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "by ${panel.authorName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}