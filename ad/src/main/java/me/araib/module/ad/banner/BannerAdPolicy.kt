package me.araib.module.ad.banner

import java.io.Serializable

sealed class BannerAdPolicy : Serializable {
    class BannerAdmobPolicy(
        val admobAppId: String,
        val admobAdId: String,
        val admobCallback: BannerAdCallback.BannerAdmobCallback? = null,
        val shouldShowLogs: Boolean = false
    ) : BannerAdPolicy(), Serializable
    class BannerFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: BannerAdCallback.BannerFacebookCallback? = null,
        val shouldShowLogs: Boolean = false
    ) : BannerAdPolicy(), Serializable
    class BannerMopubPolicy(
        val mopubAppId: String,
        val mopubAdId: String,
        val mopubCallback: BannerAdCallback.BannerMopubCallback? = null,
        val shouldShowLogs: Boolean = false
    ) : BannerAdPolicy(), Serializable
}