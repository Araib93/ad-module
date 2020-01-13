package me.araib.module.ad.rewarded

sealed class RewardedAdCallback {
    class RewardedAdmobCallback(
        val onRewardEarned: ((RewardItem.AdmobRewardItem) -> Unit)?,
        val onRewardedAdFailedToShow: (() -> Unit)?,
        val onRewardedAdClosed: (() -> Unit)?,
        val onRewardedAdLoaded: (() -> Unit)?,
        val onRewardedAdFailedToLoad: (() -> Unit)?
    ) : RewardedAdCallback()

    class RewardedFacebookCallback(
        val onRewardedAdLoaded: (() -> Unit)?,
        val onRewardedAdLoadFailed: (() -> Unit)?
    ) : RewardedAdCallback()

    class RewardedMopubCallback(
        val onRewardedAdLoaded: (() -> Unit)?,
        val onRewardedAdLoadFailed: (() -> Unit)?
    ) : RewardedAdCallback()
}