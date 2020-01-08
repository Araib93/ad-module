package me.araib.module.ad.banner

import java.io.Serializable

sealed class BannerAdPolicy : Serializable {
    class BannerAdmobPolicy(
        val adMobAppId: String,
        val adMobAdId: String,
        val adMobCallback: BannerAdCallback.BannerFacebookCallback? = null
    ) : BannerAdPolicy(), Serializable
    class BannerFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: BannerAdCallback.BannerFacebookCallback? = null
    ) : BannerAdPolicy(), Serializable
    class BannerMopubPolicy(
        val mopubCallback: BannerAdCallback.BannerMopubCallback? = null
    ) : BannerAdPolicy(), Serializable
}