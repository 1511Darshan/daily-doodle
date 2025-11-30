package com.example.dailydoodle.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Analytics helper for Daily Doodle Chain.
 * 
 * Events to track per PRD:
 * - sign_up, feed_open, chain_opened, panel_added, panel_shared
 * - rewarded_ad_view, interstitial_ad_shown, purchase_made, report_submitted
 */
object Analytics {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        val bundle = params?.let {
            android.os.Bundle().apply {
                it.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                    }
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    // ============ User Events ============
    
    fun logSignUp(method: String) {
        logEvent("sign_up", mapOf("method" to method))
    }

    fun logSignIn(method: String) {
        logEvent("sign_in", mapOf("method" to method))
    }

    // ============ Content Events ============
    
    fun logFeedOpen(filter: String) {
        logEvent("feed_open", mapOf("filter" to filter))
    }

    fun logChainOpened(chainId: String) {
        logEvent("chain_opened", mapOf("chain_id" to chainId))
    }

    fun logPanelAdded(chainId: String) {
        logEvent("panel_added", mapOf("chain_id" to chainId))
    }

    fun logPanelShared(chainId: String) {
        logEvent("panel_shared", mapOf("chain_id" to chainId))
    }
    
    fun logChainCreated(chainId: String) {
        logEvent("chain_created", mapOf("chain_id" to chainId))
    }

    // ============ Ad Events ============
    
    /**
     * Log when a rewarded ad is viewed and reward is earned.
     * @param adType Type of reward: "hint", "premium_brush", "extra_try", "boost_visibility"
     */
    fun logRewardedAdView(adType: String) {
        logEvent("rewarded_ad_view", mapOf(
            "ad_type" to adType,
            "ad_format" to "rewarded"
        ))
    }
    
    /**
     * Log when a rewarded ad is requested/shown (even if not completed).
     */
    fun logRewardedAdRequested(adType: String) {
        logEvent("rewarded_ad_requested", mapOf("ad_type" to adType))
    }

    /**
     * Log when an interstitial ad is shown.
     */
    fun logInterstitialAdShown() {
        logEvent("interstitial_ad_shown", mapOf("ad_format" to "interstitial"))
    }
    
    /**
     * Log when an interstitial ad is blocked by frequency rules.
     */
    fun logInterstitialAdBlocked(reason: String) {
        logEvent("interstitial_ad_blocked", mapOf("reason" to reason))
    }
    
    /**
     * Log when a native ad is displayed in feed.
     */
    fun logNativeAdDisplayed(position: Int) {
        logEvent("native_ad_displayed", mapOf(
            "position" to position,
            "ad_format" to "native"
        ))
    }
    
    /**
     * Log when a banner ad is displayed.
     */
    fun logBannerAdDisplayed(screen: String) {
        logEvent("banner_ad_displayed", mapOf(
            "screen" to screen,
            "ad_format" to "banner"
        ))
    }
    
    /**
     * Log ad load failure for debugging.
     */
    fun logAdLoadFailed(adFormat: String, errorMessage: String) {
        logEvent("ad_load_failed", mapOf(
            "ad_format" to adFormat,
            "error" to errorMessage
        ))
    }

    // ============ Purchase Events ============
    
    /**
     * Log when a purchase is made via IAP.
     */
    fun logPurchaseMade(productId: String) {
        logEvent("purchase_made", mapOf(
            "product_id" to productId,
            "currency" to "USD"
        ))
    }
    
    /**
     * Log when user opens the shop.
     */
    fun logShopOpened() {
        logEvent("shop_opened", null)
    }
    
    /**
     * Log when user views a product in shop.
     */
    fun logProductViewed(productId: String) {
        logEvent("product_viewed", mapOf("product_id" to productId))
    }

    // ============ Moderation Events ============
    
    fun logReportSubmitted(panelId: String) {
        logEvent("report_submitted", mapOf("panel_id" to panelId))
    }
    
    fun logReportSubmittedWithReason(panelId: String, reason: String) {
        logEvent("report_submitted", mapOf(
            "panel_id" to panelId,
            "reason" to reason
        ))
    }
    
    // ============ Session Events ============
    
    fun logSessionStart() {
        logEvent("session_start", null)
    }
    
    fun logOnboardingCompleted() {
        logEvent("onboarding_completed", null)
    }
    
    fun logOnboardingSkipped() {
        logEvent("onboarding_skipped", null)
    }
}
