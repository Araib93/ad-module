package me.araib.module.ad

import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import me.araib.core.utils.ExposedClass
import me.araib.core.utils.ExposedImplementFunction
import me.araib.core.utils.ExposedProvideFunction
import me.araib.core.utils.PossibleValues
import me.araib.module.ad.banner.BannerAdPolicy
import me.araib.module.ad.interstitial.InterstitialAdPolicy
import me.araib.module.ad.rewarded.RewardedAdPolicy

@ExposedClass(
    author = "m.araib.shafiq@gmail.com",
    purpose = "Trait class extension for easy handling of Facebook and AdMob ads",
    provides = [
        "showBannerAd",
        "removeBannerAd",
        "showInterstitialAd"
    ],
    requires = [
        "initAdTrait"
    ]
)
interface AdTrait {
    @ExposedImplementFunction(purpose = "For initializing trait with context")
    @PossibleValues(
        name = "context",
        values = [
            "Context -> Sets lifecycle observer on this context"
        ]
    )
    fun initAdTrait(context: Context)

    @ExposedProvideFunction(purpose = "For loading interstitial ad according to provided InterstitialAdPolicy object")
    @PossibleValues(
        name = "interstitialAdPolicy",
        values = ["InterstitialAdPolicy -> Ad policy which defines which interstitial ad to load"]
    )
    fun loadInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy)

    @ExposedProvideFunction(purpose = "For showing interstitial ads according to provided InterstitialAdPolicy object")
    @PossibleValues(
        name = "interstitialAdPolicy",
        values = ["InterstitialAdPolicy -> Ad policy which defines which interstitial ad to show"]
    )
    @Throws(IllegalStateException::class)
    fun showInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy)

    @ExposedProvideFunction(purpose = "For loading rewarded ads according to provided Policy objects")
    @PossibleValues(
        name = "rewardedAdPolicy",
        values = ["RewardedAdPolicy -> Ad policy which defines which rewarded ad to load"]
    )
    fun loadRewardedAd(rewardedAdPolicy: RewardedAdPolicy)

    @ExposedProvideFunction(purpose = "For showing interstitial ads according to provided Policy objects")
    @PossibleValues(
        name = "rewardedAdPolicy",
        values = ["RewardedAdPolicy -> Ad policy which defines which rewarded ad to show"]
    )
    @Throws(IllegalStateException::class)
    fun showRewardedAd(rewardedAdPolicy: RewardedAdPolicy)

    @ExposedProvideFunction(purpose = "For showing banner ad on the given layout. Please call removeAd() before showing a new one")
    @PossibleValues(
        name = "fragmentManager",
        values = ["FragmentManager -> SupportFragmentManager or ChildFragmentManager used for transaction of ad fragment"]
    )
    @PossibleValues(
        name = "replaceLayout",
        values = ["Int -> R.id of the layout which is to be replaced by banner ad"]
    )
    @PossibleValues(
        name = "policy",
        values = ["BannerAdPolicy -> Ad policy which defines which banner ad to show"]
    )
    fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        bannerAdPolicy: BannerAdPolicy
    )

    @ExposedProvideFunction(
        purpose = "For removing banner ad",
        returns = [
            "true -> Ad removed successfully",
            "false -> Ad not added in first place"
        ]
    )
    @PossibleValues(
        name = "fragmentManager",
        values = ["FragmentManager -> SupportFragmentManager or ChildFragmentManager used for transaction of AdFragment"]
    )
    fun removeBannerAd(fragmentManager: FragmentManager): Boolean
}