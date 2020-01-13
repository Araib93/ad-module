package me.araib.module.ad.banner

import java.io.Serializable

sealed class BannerAdPolicy : Serializable {
    class BannerAdmobPolicy(
        val adMobAppId: String,
        val adMobAdId: String,
        val adMobCallback: BannerAdCallback.BannerAdmobCallback? = null,
        val debug: Boolean = false
    ) : BannerAdPolicy(), Serializable
    class BannerFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: BannerAdCallback.BannerFacebookCallback? = null,
        val debug: Boolean = false
    ) : BannerAdPolicy(), Serializable
    class BannerMopubPolicy(
        val mopubAppId: String,
        val mopubAdId: String,
        val mopubCallback: BannerAdCallback.BannerMopubCallback? = null,
        val debug: Boolean = false
    ) : BannerAdPolicy(), Serializable
}