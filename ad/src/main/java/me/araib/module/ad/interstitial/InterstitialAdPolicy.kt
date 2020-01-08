package me.araib.module.ad.interstitial

import java.io.Serializable

sealed class InterstitialAdPolicy() : Serializable {
    data class InterstitialAdmobPolicy(
        val admobAdId: String,
        val admobCallback: InterstitialAdCallback.InterstitialAdmobCallback? = null
    ) : InterstitialAdPolicy(), Serializable

    data class InterstitialFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: InterstitialAdCallback.InterstitialFacebookCallback? = null
    ) : InterstitialAdPolicy(),
        Serializable

    data class InterstitialMopubPolicy(
        val mopubCallback: InterstitialAdCallback.InterstitialMopubCallback? = null
    ) : InterstitialAdPolicy(), Serializable
}