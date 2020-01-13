package me.araib.module.ad.banner

sealed class BannerAdCallback {
    class BannerAdmobCallback(
        val onBannerAdFailedToLoad: (() -> Unit)?,
        val onBannerAdLoaded: (() -> Unit)?
    ) : BannerAdCallback()

    class BannerFacebookCallback(
        val onBannerAdFailedToLoad: (() -> Unit)?,
        val onBannerAdLoaded: (() -> Unit)?
    ) : BannerAdCallback()

    class BannerMopubCallback(
        val onBannerAdFailedToLoad: (() -> Unit)?,
        val onBannerAdLoaded: (() -> Unit)?
    ) : BannerAdCallback()
}