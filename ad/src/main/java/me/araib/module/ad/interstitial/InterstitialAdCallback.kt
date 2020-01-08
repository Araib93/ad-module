package me.araib.module.ad.interstitial

sealed class InterstitialAdCallback {
    class InterstitialAdmobCallback(
        val onAdDismiss: (() -> Unit)?,
        val onInterstitialFailedToLoad: (() -> Unit)?
    ) : InterstitialAdCallback()

    class InterstitialFacebookCallback(
        val onAdLoaded: (() -> Unit)?,
        val onAdDismiss: (() -> Unit)?,
        val onError: (() -> Unit)?
    ) : InterstitialAdCallback()

    class InterstitialMopubCallback(
        val onAdDismiss: (() -> Unit)?,
        val onInterstitialFailedToLoad: (() -> Unit)?
    ) : InterstitialAdCallback()
}