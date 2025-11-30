package com.example.dailydoodle.ui.admob

/**
 * Ad configuration and constants for Daily Doodle Chain.
 * 
 * Per PRD monetization plan:
 * - Rewarded ads: opt-in only (unlock hint, premium brush)
 * - Interstitial: ≤2 per session, min 90s apart
 * - Native/banner: non-intrusive, no sticky banners on drawing or viewer
 */
object AdConfig {
    // Test Ad Unit IDs - Replace with actual IDs from AdMob console for production
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // Frequency rules per PRD
    const val MAX_INTERSTITIALS_PER_SESSION = 2
    const val MIN_INTERSTITIAL_INTERVAL_MS = 90_000L // 90 seconds

    // Feed native ad positions (0-indexed)
    val NATIVE_AD_FEED_POSITIONS = listOf(3, 11) // Positions 4 and 12 (1-indexed)

    // Chain viewer interstitial trigger
    const val PANELS_BEFORE_INTERSTITIAL = 5

    // Reward types
    object RewardType {
        const val HINT = "hint"
        const val PREMIUM_BRUSH = "premium_brush"
        const val EXTRA_TRY = "extra_try"
        const val BOOST_VISIBILITY = "boost_visibility"
    }
}
