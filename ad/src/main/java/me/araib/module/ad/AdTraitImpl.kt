package me.araib.module.ad

import android.content.Context
import android.util.Log
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.ads.RequestConfiguration
import me.araib.core.BaseActivity
import me.araib.module.ad.banner.BannerAdPolicy
import me.araib.module.ad.banner.fragments.AdmobBannerAdFragment
import me.araib.module.ad.banner.fragments.BannerAdFragment
import me.araib.module.ad.banner.fragments.FacebookBannerAdFragment
import me.araib.module.ad.interstitial.FacebookInterstitialAdListener
import me.araib.module.ad.interstitial.InterstitialAdPolicy
import me.araib.module.ad.rewarded.RewardItem
import me.araib.module.ad.rewarded.RewardedAdPolicy
import com.facebook.ads.Ad as FacebookAd
import com.facebook.ads.AdError as FacebookAdError
import com.facebook.ads.AudienceNetworkAds as FacebookAudienceNetworkAds
import com.facebook.ads.InterstitialAd as FacebookInterstitialAd
import com.google.android.gms.ads.AdListener as AdmobAdListener
import com.google.android.gms.ads.AdRequest as AdmobAdRequest
import com.google.android.gms.ads.InterstitialAd as AdmobInterstitialAd
import com.google.android.gms.ads.MobileAds as AdmobMobileAds
import com.google.android.gms.ads.rewarded.RewardItem as AdmobRewardItem
import com.google.android.gms.ads.rewarded.RewardedAd as AdmobRewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback as AdmobRewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback as AdmobRewardedAdLoadCallback

class AdTraitImpl : LifecycleObserver, AdTrait {
    companion object {
        private val TAG: String? = "Trait: Ad"
    }

    private var context: Context? = null
    private lateinit var bannerAdFragment: BannerAdFragment

    override fun initAdTrait(context: Context) {
        this.context = context
        (this.context as LifecycleOwner).lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        (context as? LifecycleOwner)?.lifecycle?.removeObserver(this)
        context = null
    }

    private lateinit var facebookInterstitialAd: FacebookInterstitialAd
    private lateinit var admobInterstitialAd: AdmobInterstitialAd

    override fun loadInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy) {
        when (interstitialAdPolicy) {
            is InterstitialAdPolicy.InterstitialFacebookPolicy -> {
                loadFacebookInterstitialAd(interstitialAdPolicy)
            }
            is InterstitialAdPolicy.InterstitialAdmobPolicy -> {
                loadAdmobInterstitialAd(interstitialAdPolicy)
            }
            is InterstitialAdPolicy.InterstitialMopubPolicy -> {
                throw IllegalArgumentException("MoPub Interstitial Ad support is currently in progress")
            }
        }
    }

    private fun loadFacebookInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialFacebookPolicy) {
        context?.let { context ->
            FacebookAudienceNetworkAds.initialize(context.applicationContext)
            if (::facebookInterstitialAd.isInitialized) {
                facebookInterstitialAd.destroy()
            }
            facebookInterstitialAd = FacebookInterstitialAd(context, interstitialAdPolicy.facebookAdId)

            facebookInterstitialAd.setAdListener(object : FacebookInterstitialAdListener() {
                override fun onAdLoaded(p0: FacebookAd?) {
                    super.onAdLoaded(p0)
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Facebook: Interstitial ad loaded")
                    interstitialAdPolicy.facebookCallback?.onInterstitialAdLoaded?.invoke()
                }

                override fun onInterstitialDismissed(p0: FacebookAd?) {
                    super.onInterstitialDismissed(p0)
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Facebook: Interstitial ad dismissed")
                    interstitialAdPolicy.facebookCallback?.onInterstitialAdDismissed?.invoke()
                    if(interstitialAdPolicy.shouldShowAgain)
                        loadFacebookInterstitialAd(interstitialAdPolicy)
                }

                override fun onError(p0: FacebookAd?, p1: FacebookAdError?) {
                    super.onError(p0, p1)
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.e(
                            TAG,
                            "Facebook: ${p1?.errorMessage
                                ?: "Unable to load Facebook banner ad"} code: ${p1?.errorCode
                                ?: "Unknown"}"
                        )
                    interstitialAdPolicy.facebookCallback?.onInterstitialAdError?.invoke()
                }
            })
            facebookInterstitialAd.loadAd()
        } ?: run {
            if (interstitialAdPolicy.shouldShowLogs)
                Log.e(TAG, "Facebook: Interstitial ad failed to load, Context is null")
            interstitialAdPolicy.facebookCallback?.onInterstitialAdError?.invoke()
        }
    }

    private fun loadAdmobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        context?.let { context ->
            AdmobMobileAds.initialize(context.applicationContext, interstitialAdPolicy.admobAppId).apply {

                //requesting ads based under child and family protection policy
                if (interstitialAdPolicy.isAdRestricted)
                    AdmobMobileAds.setRequestConfiguration(
                        RequestConfiguration.Builder()
                            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                            .setMaxAdContentRating("G")
                            .build()
                    )
            }
            admobInterstitialAd = AdmobInterstitialAd(context)
            admobInterstitialAd.adUnitId = interstitialAdPolicy.admobAdId

            admobInterstitialAd.adListener = object : AdmobAdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Admob: Interstitial ad loaded")
                    interstitialAdPolicy.admobCallback?.onInterstitialAdLoaded?.invoke()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Admob: Interstitial ad closed")
                    interstitialAdPolicy.admobCallback?.onInterstitialAdClosed?.invoke()
                    if (interstitialAdPolicy.shouldLoadAgain)
                        loadAdmobInterstitialAd(interstitialAdPolicy)
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    if (interstitialAdPolicy.shouldShowLogs)
                        Log.e(TAG, "Admob: Unable to load Admob banner ad code: $p0")
                    interstitialAdPolicy.admobCallback?.onInterstitialAdFailedToLoad?.invoke()
                }
            }

            admobInterstitialAd.loadAd(AdmobAdRequest.Builder().build())
        } ?: run {
            if (interstitialAdPolicy.shouldShowLogs)
                Log.e(TAG, "Admob: Interstitial ad failed to load, Context is null")
            interstitialAdPolicy.admobCallback?.onInterstitialAdFailedToLoad?.invoke()
        }
    }

    override fun showInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy) {
        when (interstitialAdPolicy) {
            is InterstitialAdPolicy.InterstitialFacebookPolicy -> showFacebookInterstitialAd(
                interstitialAdPolicy
            )
            is InterstitialAdPolicy.InterstitialAdmobPolicy -> showAdmobInterstitialAd(
                interstitialAdPolicy
            )
            is InterstitialAdPolicy.InterstitialMopubPolicy -> {
                throw IllegalArgumentException("MoPub Interstitial Ad support is currently in progress")
            }
        }
    }

    private fun showFacebookInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialFacebookPolicy) {
        check(::facebookInterstitialAd.isInitialized) {
            "Facebook: loadInterstitialAd() not called before showInterstitialAd()"
        }
        if (facebookInterstitialAd.isAdLoaded) {
            facebookInterstitialAd.show()
        } else {
            if (interstitialAdPolicy.shouldShowLogs)
                Log.e(TAG, "Facebook: Interstitial ad not loaded yet")
            interstitialAdPolicy.facebookCallback?.onInterstitialAdError
        }
    }

    private fun showAdmobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        check(::admobInterstitialAd.isInitialized) {
            "Admob: loadInterstitialAd() not called before showInterstitialAd()"
        }
        if (admobInterstitialAd.isLoaded) {
            admobInterstitialAd.show()
        } else {
            if (interstitialAdPolicy.shouldShowLogs)
                Log.e(TAG, "Admob: Interstitial ad not loaded yet")
            interstitialAdPolicy.admobCallback?.onInterstitialAdFailedToLoad
        }
    }

    private lateinit var admobRewardedAd: AdmobRewardedAd

    override fun loadRewardedAd(rewardedAdPolicy: RewardedAdPolicy) {
        when (rewardedAdPolicy) {
            is RewardedAdPolicy.RewardedFacebookPolicy -> {
                throw IllegalArgumentException("Facebook rewarded ad support is currently in progress")
            }
            is RewardedAdPolicy.RewardedAdmobPolicy -> {
                loadAdmobRewardedAd(rewardedAdPolicy)
            }
            is RewardedAdPolicy.RewardedMopubPolicy -> {
                throw IllegalArgumentException("MoPub rewarded ad support is currently in progress")
            }
        }
    }

    private fun loadAdmobRewardedAd(rewardedAdPolicy: RewardedAdPolicy.RewardedAdmobPolicy) {
        context?.let { context ->
            AdmobMobileAds.initialize(context.applicationContext, rewardedAdPolicy.admobAppId).apply {

                //requesting ads based under child and family protection policy
                if (rewardedAdPolicy.isAdRestricted)
                    AdmobMobileAds.setRequestConfiguration(
                        RequestConfiguration.Builder()
                            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                            .setMaxAdContentRating("G")
                            .build()
                    )
            }
            admobRewardedAd = AdmobRewardedAd(context, rewardedAdPolicy.admobAdId)
            val rewardedAdLoadCallback = object : AdmobRewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    super.onRewardedAdLoaded()
                    if (rewardedAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Admob: Rewarded ad loaded")
                    rewardedAdPolicy.admobCallback?.onRewardedAdLoaded?.invoke()
                }

                override fun onRewardedAdFailedToLoad(p0: Int) {
                    super.onRewardedAdFailedToLoad(p0)
                    if (rewardedAdPolicy.shouldShowLogs)
                        Log.e(TAG, "Admob: Unable to show Admob Rewarded Ad code: $p0")
                    rewardedAdPolicy.admobCallback?.onRewardedAdFailedToLoad?.invoke()
                }
            }
            admobRewardedAd.loadAd(AdmobAdRequest.Builder().build(), rewardedAdLoadCallback)
        } ?: run {
            if (rewardedAdPolicy.shouldShowLogs)
                Log.e(TAG, "Admob: Rewarded ad failed to load, Context is null")
            rewardedAdPolicy.admobCallback?.onRewardedAdFailedToLoad?.invoke()

        }
    }

    override fun showRewardedAd(rewardedAdPolicy: RewardedAdPolicy) {
        when (rewardedAdPolicy) {
            is RewardedAdPolicy.RewardedFacebookPolicy -> {
                throw IllegalArgumentException("Facebook rewarded ad support is currently in progress")
            }
            is RewardedAdPolicy.RewardedAdmobPolicy -> {
                showAdmobRewardedAd(rewardedAdPolicy)
            }
            is RewardedAdPolicy.RewardedMopubPolicy -> {
                throw IllegalArgumentException("MoPub rewarded ad support is currently in progress")
            }
        }
    }

    private fun showAdmobRewardedAd(rewardedAdPolicy: RewardedAdPolicy.RewardedAdmobPolicy) {
        check(::admobRewardedAd.isInitialized) {
            "Admob: loadRewardedAd() not called before showRewardedAd()"
        }
        if (admobRewardedAd.isLoaded) {
            val rewardedAdCallback = object : AdmobRewardedAdCallback() {
                override fun onUserEarnedReward(rewardItem: AdmobRewardItem) {
                    val reward =
                        RewardItem.AdmobRewardItem(rewardItem.amount, rewardItem.type)
                    if (rewardedAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Admob: Reward earned")
                    rewardedAdPolicy.admobCallback?.onRewardEarned?.invoke(reward)
                }

                override fun onRewardedAdFailedToShow(p0: Int) {
                    super.onRewardedAdFailedToShow(p0)
                    if (rewardedAdPolicy.shouldShowLogs)
                        Log.e(TAG, "Admob: Unable to show Admob Rewarded Ad code: $p0")
                    rewardedAdPolicy.admobCallback?.onRewardedAdFailedToShow?.invoke()
                }

                override fun onRewardedAdClosed() {
                    super.onRewardedAdClosed()
                    if (rewardedAdPolicy.shouldShowLogs)
                        Log.i(TAG, "Admob: Rewarded ad closed")
                    rewardedAdPolicy.admobCallback?.onRewardedAdClosed?.invoke()
                    if(rewardedAdPolicy.shouldShowAgain)
                        loadAdmobRewardedAd(rewardedAdPolicy)
                }
            }
            admobRewardedAd.show(context as? BaseActivity, rewardedAdCallback)
        } else {
            if (rewardedAdPolicy.shouldShowLogs)
                Log.e(TAG, "Admob: Rewarded ad not loaded yet")
        }
    }

    override fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        bannerAdPolicy: BannerAdPolicy
    ) {
        when (bannerAdPolicy) {
            is BannerAdPolicy.BannerFacebookPolicy -> {
                showFacebookBannerAd(bannerAdPolicy)
            }
            is BannerAdPolicy.BannerAdmobPolicy -> {
                showAdmobBannerAd(bannerAdPolicy)
            }
            is BannerAdPolicy.BannerMopubPolicy -> {
                throw IllegalArgumentException("MoPub Banner Ad support is currently in progress")
            }
        }

        fragmentManager
            .beginTransaction()
            .replace(replaceLayout, bannerAdFragment)
            .commit()
    }

    private fun showAdmobBannerAd(bannerAdPolicy: BannerAdPolicy.BannerAdmobPolicy) {
        context?.let { context ->
            AdmobMobileAds.initialize(context.applicationContext, bannerAdPolicy.admobAppId).apply {

                //requesting ads based under child and family protection policy
                if (bannerAdPolicy.isAdRestricted)
                    AdmobMobileAds.setRequestConfiguration(
                        RequestConfiguration.Builder()
                            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                            .setMaxAdContentRating("G")
                            .build()
                    )
            }
            bannerAdFragment = AdmobBannerAdFragment()
                .apply {
                    this.arguments = bundleOf(
                        "data" to bundleOf(
                            "admobAdId" to bannerAdPolicy.admobAdId
                        )
                    )
                    this.shouldShowLogs = bannerAdPolicy.shouldShowLogs
                    this.admobCallback = bannerAdPolicy.admobCallback
                }
        } ?: run {
            if (bannerAdPolicy.shouldShowLogs)
                Log.e(TAG, "Admob: Banner ad failed to show, Context is null")
            bannerAdPolicy.admobCallback?.onBannerAdFailedToLoad?.invoke()
        }
    }

    private fun showFacebookBannerAd(bannerAdPolicy: BannerAdPolicy.BannerFacebookPolicy) {
        context?.let { context ->
            FacebookAudienceNetworkAds.initialize(context.applicationContext)
            bannerAdFragment = FacebookBannerAdFragment()
                .apply {
                    this.arguments = bundleOf(
                        "data" to bundleOf(
                            "facebookAdId" to bannerAdPolicy.facebookAdId
                        )
                    )
                    this.shouldShowLogs = bannerAdPolicy.shouldShowLogs
                    this.facebookCallback = bannerAdPolicy.facebookCallback
                }
        } ?: run {
            if (bannerAdPolicy.shouldShowLogs)
                Log.e(TAG, "Facebook: Banner ad failed to show, Context is null")
            bannerAdPolicy.facebookCallback?.onBannerAdFailedToLoad?.invoke()
        }
    }

    override fun removeBannerAd(fragmentManager: FragmentManager): Boolean {
        if (::bannerAdFragment.isInitialized && bannerAdFragment.isAdded) {
            fragmentManager
                .beginTransaction()
                .remove(bannerAdFragment)
                .commit()
            return true
        }
        return false
    }
}