package com.example.dailydoodle.ui.admob

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Composable wrapper for AdMob Banner Ad.
 * Use on Profile/Settings screens per PRD.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER
) {
    val context = LocalContext.current
    
    // Skip if user is ad-free
    if (AdMobManager.isAdFreeUser()) {
        return
    }
    
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            AdMobManager.createBannerAdView(ctx, adSize = adSize).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { adView ->
            // AdView is already configured in factory
        }
    )
}

/**
 * Composable for displaying Native Ad in Feed.
 * Styled to blend with feed content per PRD (non-intrusive).
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    
    // Skip if user is ad-free
    if (AdMobManager.isAdFreeUser()) {
        return
    }
    
    LaunchedEffect(Unit) {
        AdMobManager.loadNativeAd(
            context = context,
            onAdLoaded = { ad ->
                nativeAd = ad
                isLoading = false
            },
            onAdFailedToLoad = { error ->
                isLoading = false
                loadFailed = true
            }
        )
    }
    
    // Don't show anything if loading failed or still loading
    if (loadFailed || isLoading) {
        return
    }
    
    nativeAd?.let { ad ->
        DisposableEffect(ad) {
            onDispose {
                ad.destroy()
            }
        }
        
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Ad indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sponsored",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Ad content
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon
                    ad.icon?.drawable?.let { drawable ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    android.widget.ImageView(ctx).apply {
                                        setImageDrawable(drawable)
                                        scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                    }
                                }
                            )
                        }
                    }
                    
                    // Text content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        ad.headline?.let { headline ->
                            Text(
                                text = headline,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        ad.body?.let { body ->
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Call to action
                ad.callToAction?.let { cta ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Native ad handles clicks */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(cta)
                    }
                }
            }
        }
    }
}

/**
 * Rewarded ad button with loading state.
 * Use for "Get Hint" or "Unlock Premium Brush" per PRD.
 */
@Composable
fun RewardedAdButton(
    text: String,
    rewardType: String,
    onRewarded: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var isAdReady by remember { mutableStateOf(AdMobManager.isRewardedAdReady()) }
    
    // Load ad if not ready
    LaunchedEffect(Unit) {
        if (!isAdReady) {
            AdMobManager.loadRewardedAd(
                context = context,
                onAdLoaded = { isAdReady = true }
            )
        }
    }
    
    OutlinedButton(
        onClick = {
            isLoading = true
            AdMobManager.showRewardedAd(
                context = context,
                rewardType = rewardType,
                onRewarded = { _ ->
                    isLoading = false
                    onRewarded()
                },
                onAdClosed = {
                    isLoading = false
                },
                onAdFailedToShow = { _ ->
                    isLoading = false
                }
            )
        },
        modifier = modifier,
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
        if (!isLoading) {
            Spacer(modifier = Modifier.width(4.dp))
            Text("🎬", fontSize = 14.sp)
        }
    }
}
