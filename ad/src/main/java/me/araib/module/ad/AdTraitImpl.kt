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
        if (::facebookInterstitialAd.isInitialized) {
            facebookInterstitialAd.destroy()
        }
        facebookInterstitialAd = FacebookInterstitialAd(context, interstitialAdPolicy.facebookAdId)

        facebookInterstitialAd.setAdListener(object : FacebookInterstitialAdListener() {
            override fun onAdLoaded(p0: FacebookAd?) {
                super.onAdLoaded(p0)
                if (interstitialAdPolicy.debug)
                    Log.i(TAG, "Facebook: Interstitial ad loaded")
                interstitialAdPolicy.facebookCallback?.onInterstitialAdLoaded?.invoke()
            }

            override fun onInterstitialDismissed(p0: FacebookAd?) {
                super.onInterstitialDismissed(p0)
                if (interstitialAdPolicy.debug)
                    Log.i(TAG, "Facebook: Interstitial ad dismissed")
                interstitialAdPolicy.facebookCallback?.onInterstitialAdDismissed?.invoke()
                loadFacebookInterstitialAd(interstitialAdPolicy)
            }

            override fun onError(p0: FacebookAd?, p1: FacebookAdError?) {
                super.onError(p0, p1)
                if (interstitialAdPolicy.debug)
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
    }

    private fun loadAdmobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        context?.let { context ->
            AdmobMobileAds.initialize(context.applicationContext, interstitialAdPolicy.admobAppId)
            admobInterstitialAd = AdmobInterstitialAd(context)
            admobInterstitialAd.adUnitId = interstitialAdPolicy.admobAdId

            admobInterstitialAd.adListener = object : AdmobAdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (interstitialAdPolicy.debug)
                        Log.i(TAG, "Admob: Interstitial ad loaded")
                    interstitialAdPolicy.admobCallback?.onInterstitialAdLoaded?.invoke()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    if (interstitialAdPolicy.debug)
                        Log.i(TAG, "Admob: Interstitial ad closed")
                    loadAdmobInterstitialAd(interstitialAdPolicy)
                    interstitialAdPolicy.admobCallback?.onInterstitialAdClosed?.invoke()
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    if (interstitialAdPolicy.debug)
                        Log.e(TAG, "Admob: Unable to load Admob banner ad code: $p0")
                    interstitialAdPolicy.admobCallback?.onInterstitialAdFailedToLoad?.invoke()
                }
            }

            admobInterstitialAd.loadAd(AdmobAdRequest.Builder().build())
        } ?: run {
            if (interstitialAdPolicy.debug)
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
            "loadInterstitialAd() not called before showInterstitialAd()"
        }
        if (facebookInterstitialAd.isAdLoaded) {
            facebookInterstitialAd.show()
            loadFacebookInterstitialAd(interstitialAdPolicy)
        } else {
            if (interstitialAdPolicy.debug)
                Log.e(TAG, "Facebook: Interstitial ad not loaded yet")
            interstitialAdPolicy.facebookCallback?.onInterstitialAdError
        }
    }

    private fun showAdmobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        check(::admobInterstitialAd.isInitialized) {
            "loadInterstitialAd() not called before showInterstitialAd()"
        }
        if (admobInterstitialAd.isLoaded) {
            admobInterstitialAd.show()
            loadAdmobInterstitialAd(interstitialAdPolicy)
        } else {
            if (interstitialAdPolicy.debug)
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
            AdmobMobileAds.initialize(context?.applicationContext, rewardedAdPolicy.admobAppId)
            admobRewardedAd = AdmobRewardedAd(context, rewardedAdPolicy.admobAdId)
            val rewardedAdLoadCallback = object : AdmobRewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    super.onRewardedAdLoaded()
                    if (rewardedAdPolicy.debug)
                        Log.i(TAG, "Admob: Rewarded ad loaded")
                    rewardedAdPolicy.admobCallback?.onRewardedAdLoaded?.invoke()
                }

                override fun onRewardedAdFailedToLoad(p0: Int) {
                    super.onRewardedAdFailedToLoad(p0)
                    if (rewardedAdPolicy.debug)
                        Log.e(TAG, "Admob: Unable to show Admob Rewarded Ad code: $p0")
                    rewardedAdPolicy.admobCallback?.onRewardedAdFailedToLoad?.invoke()
                }
            }
            admobRewardedAd.loadAd(AdmobAdRequest.Builder().build(), rewardedAdLoadCallback)
        } ?: run {
            if (rewardedAdPolicy.debug)
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
            "loadRewardedAd() not called before showRewardedAd()"
        }
        if (admobRewardedAd.isLoaded) {
            val rewardedAdCallback = object : AdmobRewardedAdCallback() {
                override fun onUserEarnedReward(rewardItem: AdmobRewardItem) {
                    val reward =
                        RewardItem.AdmobRewardItem(rewardItem.amount, rewardItem.type)
                    if (rewardedAdPolicy.debug)
                        Log.i(TAG, "Admob: Reward earned")
                    rewardedAdPolicy.admobCallback?.onRewardEarned?.invoke(reward)
                }

                override fun onRewardedAdFailedToShow(p0: Int) {
                    super.onRewardedAdFailedToShow(p0)
                    if (rewardedAdPolicy.debug)
                        Log.e(TAG, "Admob: Unable to show Admob Rewarded Ad code: $p0")
                    rewardedAdPolicy.admobCallback?.onRewardedAdFailedToShow?.invoke()
                }

                override fun onRewardedAdClosed() {
                    super.onRewardedAdClosed()
                    if (rewardedAdPolicy.debug)
                        Log.i(TAG, "Admob: Rewarded ad closed")
                    rewardedAdPolicy.admobCallback?.onRewardedAdClosed?.invoke()
                }
            }
            admobRewardedAd.show(context as? BaseActivity, rewardedAdCallback)
            loadAdmobRewardedAd(rewardedAdPolicy)
        } else {
            if (rewardedAdPolicy.debug)
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
            AdmobMobileAds.initialize(context.applicationContext, bannerAdPolicy.admobAppId)
            bannerAdFragment = AdmobBannerAdFragment()
                .apply {
                    this.arguments = bundleOf(
                        "data" to bundleOf(
                            "admobAdId" to bannerAdPolicy.admobAdId
                        )
                    )
                    this.debug = bannerAdPolicy.debug
                    this.admobCallback = bannerAdPolicy.admobCallback
                }
        } ?: run {
            if (bannerAdPolicy.debug)
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
                    this.debug = bannerAdPolicy.debug
                    this.facebookCallback = bannerAdPolicy.facebookCallback
                }
        } ?: run {
            if (bannerAdPolicy.debug)
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