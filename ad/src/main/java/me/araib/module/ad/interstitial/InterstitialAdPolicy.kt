package me.araib.module.ad.interstitial

import java.io.Serializable

sealed class InterstitialAdPolicy() : Serializable {
    data class InterstitialAdmobPolicy(
        val admobAppId: String,
        val admobAdId: String,
        val admobCallback: InterstitialAdCallback.InterstitialAdmobCallback? = null,
        var shouldLoadAgain: Boolean = false,
        var shouldShowLogs: Boolean = false
    ) : InterstitialAdPolicy(), Serializable

    data class InterstitialFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: InterstitialAdCallback.InterstitialFacebookCallback? = null,
        var shouldShowAgain: Boolean = false,
        var shouldShowLogs: Boolean = false
    ) : InterstitialAdPolicy(),
        Serializable

    data class InterstitialMopubPolicy(
        val mopubAdId: String,
        val mopubCallback: InterstitialAdCallback.InterstitialMopubCallback? = null,
        var shouldShowAgain: Boolean = false,
        var shouldShowLogs: Boolean = false
    ) : InterstitialAdPolicy(), Serializable
}