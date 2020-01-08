package me.araib.module.ad.rewarded

import java.io.Serializable

sealed class RewardedAdPolicy : Serializable {
    data class RewardedAdmobPolicy(
        val adMobAdId: String,
        val admobCallback: RewardedAdCallback.RewardedAdmobCallback? = null
    ) : RewardedAdPolicy(), Serializable

    data class RewardedFacebookPolicy(
        val facebookAdId: String,
        val facebookCallback: RewardedAdCallback.RewardedFacebookCallback? = null
    ) : RewardedAdPolicy(), Serializable

    data class RewardedMopubPolicy(
        val mopubAdId: String,
        val mopubCallback: RewardedAdCallback.RewardedMopubCallback? = null
    ) : RewardedAdPolicy(), Serializable
}

