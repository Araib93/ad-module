package me.araib.module.ad.banner

sealed class BannerAdCallback {
    class BannerAdmobCallback(
        val onBannerFailedToLoad: (() -> Unit)?
    ) : BannerAdCallback()

    class BannerFacebookCallback(
        val onBannerFailedToLoad: (() -> Unit)?
    ) : BannerAdCallback()

    class BannerMopubCallback(
        val onBannerFailedToLoad: (() -> Unit)?
    ) : BannerAdCallback()
}