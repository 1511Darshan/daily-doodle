package com.example.dailydoodle.ui.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem

/**
 * Central AdMob manager for Daily Doodle Chain.
 * Handles all ad types with frequency controls per PRD monetization plan.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false

    // Session tracking for frequency limits (per PRD: ≤2 interstitials/session, 90s apart)
    private var sessionInterstitialCount = 0
    private var lastInterstitialTime = 0L

    // Ad-free status (for users who purchased "Remove Ads")
    private var isAdFreeUser = false

    fun initialize(context: Context) {
        if (isInitialized) return
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
            isInitialized = true
        }
        
        // Reset session counters
        resetSession()
    }

    /**
     * Reset session counters. Call this on app start or when session resets.
     */
    fun resetSession() {
        sessionInterstitialCount = 0
        lastInterstitialTime = 0L
    }

    /**
     * Set ad-free status for users who purchased "Remove Ads" IAP.
     */
    fun setAdFreeUser(isAdFree: Boolean) {
        isAdFreeUser = isAdFree
    }

    fun isAdFreeUser(): Boolean = isAdFreeUser

    /**
     * Check if an interstitial can be shown based on frequency rules.
     */
    fun canShowInterstitial(): Boolean {
        if (isAdFreeUser) return false
        if (sessionInterstitialCount >= AdConfig.MAX_INTERSTITIALS_PER_SESSION) return false
        
        val timeSinceLastAd = System.currentTimeMillis() - lastInterstitialTime
        if (lastInterstitialTime > 0 && timeSinceLastAd < AdConfig.MIN_INTERSTITIAL_INTERVAL_MS) {
            return false
        }
        
        return true
    }

    fun loadRewardedAd(
        context: Context,
        adUnitId: String = AdConfig.TEST_REWARDED_AD_UNIT_ID,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    onAdFailedToLoad(error)
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun showRewardedAd(
        context: Context,
        rewardType: String = AdConfig.RewardType.HINT,
        onRewarded: (RewardItem) -> Unit = {},
        onAdClosed: () -> Unit = {},
        onAdFailedToShow: (AdError) -> Unit = {}
    ) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    onAdClosed()
                    // Reload for next time
                    loadRewardedAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                    rewardedAd = null
                    onAdFailedToShow(error)
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad shown for reward type: $rewardType")
                }
            }
            ad.show(context as Activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.type}, amount: ${rewardItem.amount}")
                onRewarded(rewardItem)
            }
        } ?: run {
            Log.w(TAG, "Rewarded ad not ready, loading...")
            // Ad not loaded, try to load and show
            loadRewardedAd(context) {
                showRewardedAd(context, rewardType, onRewarded, onAdClosed, onAdFailedToShow)
            }
        }
    }

    fun loadInterstitialAd(
        context: Context,
        adUnitId: String = AdConfig.TEST_INTERSTITIAL_AD_UNIT_ID,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ) {
        if (isAdFreeUser) {
            Log.d(TAG, "User is ad-free, skipping interstitial load")
            return
        }
        
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    onAdFailedToLoad(error)
                }
            }
        )
    }

    fun isInterstitialAdReady(): Boolean = interstitialAd != null && canShowInterstitial()

    /**
     * Show interstitial ad with frequency controls.
     * Per PRD: ≤2 per session, min 90s apart.
     */
    fun showInterstitialAd(
        context: Context,
        onAdClosed: () -> Unit = {},
        onAdFailedToShow: (AdError) -> Unit = {}
    ) {
        if (!canShowInterstitial()) {
            Log.d(TAG, "Interstitial blocked by frequency rules. Count: $sessionInterstitialCount, Last: ${System.currentTimeMillis() - lastInterstitialTime}ms ago")
            onAdClosed() // Call closed callback so navigation can continue
            return
        }
        
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    sessionInterstitialCount++
                    lastInterstitialTime = System.currentTimeMillis()
                    onAdClosed()
                    // Reload for next time
                    loadInterstitialAd(context)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                    interstitialAd = null
                    onAdFailedToShow(error)
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad shown. Session count: ${sessionInterstitialCount + 1}")
                }
            }
            ad.show(context as Activity)
        } ?: run {
            Log.w(TAG, "Interstitial ad not ready")
            onAdClosed() // Allow navigation to continue
        }
    }

    /**
     * Create a banner AdView. For use in Profile/Settings screens.
     */
    fun createBannerAdView(
        context: Context, 
        adUnitId: String = AdConfig.TEST_BANNER_AD_UNIT_ID,
        adSize: AdSize = AdSize.BANNER
    ): AdView {
        if (isAdFreeUser) {
            // Return empty AdView for ad-free users
            return AdView(context).apply {
                visibility = android.view.View.GONE
            }
        }
        
        val adView = AdView(context)
        adView.adUnitId = adUnitId
        adView.setAdSize(adSize)
        adView.loadAd(AdRequest.Builder().build())
        Log.d(TAG, "Banner ad created and loading")
        return adView
    }

    /**
     * Load a native ad for feed placement.
     */
    fun loadNativeAd(
        context: Context,
        adUnitId: String = AdConfig.TEST_NATIVE_AD_UNIT_ID,
        onAdLoaded: (NativeAd) -> Unit = {},
        onAdFailedToLoad: (LoadAdError) -> Unit = {}
    ) {
        if (isAdFreeUser) {
            Log.d(TAG, "User is ad-free, skipping native ad load")
            return
        }
        
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                Log.d(TAG, "Native ad loaded successfully")
                onAdLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed to load: ${error.message}")
                    onAdFailedToLoad(error)
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()
        
        adLoader.loadAd(AdRequest.Builder().build())
    }

    /**
     * Get debug info about current ad state.
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("=== AdMob Debug Info ===")
            appendLine("Initialized: $isInitialized")
            appendLine("Ad-Free User: $isAdFreeUser")
            appendLine("Rewarded Ad Ready: ${rewardedAd != null}")
            appendLine("Interstitial Ad Ready: ${interstitialAd != null}")
            appendLine("Session Interstitial Count: $sessionInterstitialCount/${AdConfig.MAX_INTERSTITIALS_PER_SESSION}")
            appendLine("Can Show Interstitial: ${canShowInterstitial()}")
            if (lastInterstitialTime > 0) {
                appendLine("Time Since Last Interstitial: ${(System.currentTimeMillis() - lastInterstitialTime) / 1000}s")
            }
        }
    }
}
