package me.araib.module.ad

import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import me.araib.core.utils.ExposedClass
import me.araib.core.utils.ExposedImplementFunction
import me.araib.core.utils.ExposedProvideFunction
import me.araib.core.utils.PossibleValues

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

    @ExposedProvideFunction(purpose = "For loading interstitial ads from Facebook fallback AdMob")
    @PossibleValues(
        name = "facebookAdId",
        values = ["String -> Interstitial ad id for Facebook"]
    )
    @PossibleValues(
        name = "adMobAdId",
        values = ["String -> Interstitial ad id for AdMob"]
    )
    fun loadInterstitialAd(facebookAdId: String, adMobAdId: String)

    @ExposedProvideFunction(purpose = "For showing interstitial ads from Facebook fallback AdMob, use after loadInterstitialAd")
    @PossibleValues(
        name = "facebookAdId",
        values = ["String -> Interstitial ad id for Facebook"]
    )
    @PossibleValues(
        name = "adMobAdId",
        values = ["String -> Interstitial ad id for AdMob"]
    )
    @PossibleValues(
        name = "onAdDismiss",
        values = [
            "():Unit -> Dismiss listener for interstitial ad",
            "null -> Remove dismiss listener"
        ]
    )
    fun showInterstitialAd(
        facebookAdId: String,
        adMobAdId: String,
        onAdDismiss: (() -> Unit)? = null
    )

    @ExposedProvideFunction(purpose = "For showing banner ad on the given layout")
    @PossibleValues(
        name = "fragmentManager",
        values = ["FragmentManager -> SupportFragmentManager or ChildFragmentManager used for transaction of ad fragment"]
    )
    @PossibleValues(
        name = "replaceLayout",
        values = ["Int -> R.id of the layout which is to be replaced by banner ad"]
    )
    @PossibleValues(
        name = "facebookAdId",
        values = ["String -> Banner ad id for Facebook"]
    )
    @PossibleValues(
        name = "adMobAdId",
        values = ["String -> Banner ad id for AdMob"]
    )
    @PossibleValues(
        name = "adMobAppId",
        values = ["String -> App id for AdMob"]
    )
    @PossibleValues(
        name = "onBannerFailedToLoad",
        values = [
            "():Unit -> Banner failed to load listener for modifying UI since no banner is present",
            "null -> Remove banner failed to load listener"
        ]
    )
    fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        facebookAdId: String,
        adMobAdId: String,
        adMobAppId: String,
        onBannerFailedToLoad: (() -> Unit)? = null
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