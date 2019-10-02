package me.araib.module.ad

import android.content.Context
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import me.araib.core.utils.ExposedClass
import me.araib.core.utils.ExposedProvideFunction
import me.araib.core.utils.PossibleValues

@ExposedClass(
    author = "m.araib.shafiq@gmail.com",
    purpose = "Trait class extension for easy handling of Facebook and Google ads",
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
    fun initAdTrait(context: Context)

    @ExposedProvideFunction(purpose = "For showing interstitial ads from Facebook fallback Google")
    @PossibleValues(
        name = "facebookAdId",
        values = ["String -> Interstitial ad id for Facebook"]
    )
    @PossibleValues(
        name = "googleAdId",
        values = ["String -> Interstitial ad id for Google"]
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
        googleAdId: String,
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
        name = "googleAdId",
        values = ["String -> Banner ad id for Google"]
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
        googleAdId: String,
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