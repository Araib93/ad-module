package me.araib.module.ad.rewarded

sealed class RewardItem {
    data class AdmobRewardItem(val amount: Int, val type: String): RewardItem()
    data class FacebookRewardItem(val amount: Int, val type: String): RewardItem()
    data class MopubRewardItem(val amount: Int, val type: String): RewardItem()
}