package me.araib.module.ad.interstitial

sealed class InterstitialAdCallback {
    class InterstitialAdmobCallback(
        val onInterstitialAdLoaded: (() -> Unit)?,
        val onInterstitialAdFailedToLoad: (() -> Unit)?,
        val onInterstitialAdClosed: (() -> Unit)?
    ) : InterstitialAdCallback() {
    }

    class InterstitialFacebookCallback(
        val onInterstitialAdLoaded: (() -> Unit)?,
        val onInterstitialAdError: (() -> Unit)?,
        val onInterstitialAdDismissed: (() -> Unit)?
    ) : InterstitialAdCallback()

    class InterstitialMopubCallback(
        val onInterstitialAdLoaded: (() -> Unit)?,
        val onInterstitialAdFailedToLoad: (() -> Unit)?,
        val onInterstitialAdDismiss: (() -> Unit)?
    ) : InterstitialAdCallback()
}