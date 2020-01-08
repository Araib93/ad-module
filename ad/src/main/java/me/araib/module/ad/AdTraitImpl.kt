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
import com.facebook.ads.InterstitialAd as FacebookInterstitialAd
import com.facebook.ads.Ad as FacebookAd
import com.facebook.ads.AdError as FacebookAdError
import com.facebook.ads.AudienceNetworkAds as FacebookAudienceNetworkAds
import com.google.android.gms.ads.AdListener as AdmobAdListener
import com.google.android.gms.ads.AdRequest as AdmobAdRequest
import com.google.android.gms.ads.MobileAds as AdmobMobileAds
import com.google.android.gms.ads.rewarded.RewardItem as AdmobRewardItem
import com.google.android.gms.ads.rewarded.RewardedAd as AdmobRewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback as AdmobRewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback as AdmobRewardedAdLoadCallback
import me.araib.core.BaseActivity
import me.araib.module.ad.banner.*
import me.araib.module.ad.banner.fragments.AdmobBannerAdFragment
import me.araib.module.ad.banner.fragments.BannerAdFragment
import me.araib.module.ad.banner.fragments.FacebookBannerAdFragment
import me.araib.module.ad.interstitial.FacebookInterstitialAdListener
import me.araib.module.ad.interstitial.InterstitialAdPolicy
import me.araib.module.ad.rewarded.RewardItem
import me.araib.module.ad.rewarded.RewardedAdPolicy

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

    private lateinit var facebookInterstitialAd: com.facebook.ads.InterstitialAd
    private lateinit var adMobInterstitialAd: com.google.android.gms.ads.InterstitialAd

    override fun loadInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy) {
        when (interstitialAdPolicy) {
            is InterstitialAdPolicy.InterstitialFacebookPolicy-> {
                loadFacebookInterstitialAd(interstitialAdPolicy)
            }
            is InterstitialAdPolicy.InterstitialAdmobPolicy -> {
                loadAdMobInterstitialAd(interstitialAdPolicy)
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
        facebookInterstitialAd.setAdListener(object: FacebookInterstitialAdListener() {
            override fun onAdLoaded(p0: com.facebook.ads.Ad?) {
                super.onAdLoaded(p0)
                interstitialAdPolicy.facebookCallback?.onAdLoaded?.invoke()
            }

            override fun onError(p0: com.facebook.ads.Ad?, p1: com.facebook.ads.AdError?) {
                super.onError(p0, p1)
                interstitialAdPolicy.facebookCallback?.onError?.invoke()
            }
        })
        facebookInterstitialAd.loadAd()
    }

    private fun loadAdMobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        adMobInterstitialAd = com.google.android.gms.ads.InterstitialAd(context)
        adMobInterstitialAd.adUnitId = interstitialAdPolicy.admobAdId
        adMobInterstitialAd.loadAd(AdmobAdRequest.Builder().build())
    }

    override fun showInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy) {
        when (interstitialAdPolicy) {
            is InterstitialAdPolicy.InterstitialFacebookPolicy -> showFacebookInterstitialAd(interstitialAdPolicy)
            is InterstitialAdPolicy.InterstitialAdmobPolicy -> showAdMobInterstitialAd(interstitialAdPolicy)
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
            facebookInterstitialAd.setAdListener(object :
                FacebookInterstitialAdListener() {
                override fun onInterstitialDismissed(p0: FacebookAd?) {
                    Log.i(TAG, "Facebook: Interstitial ad dismissed")
                    interstitialAdPolicy.facebookCallback?.onAdDismiss?.invoke()
                    loadFacebookInterstitialAd(interstitialAdPolicy)
                }

                override fun onError(p0: FacebookAd?, p1: FacebookAdError?) {
                    Log.e(
                        TAG,
                        "Facebook: ${p1?.errorMessage
                            ?: "Unable to load Facebook banner ad"} code: ${p1?.errorCode
                            ?: "Unknown"}"
                    )
                    interstitialAdPolicy.facebookCallback?.onError?.invoke()
                    super.onError(p0, p1)
                }
            })
            facebookInterstitialAd.show()
            loadFacebookInterstitialAd(interstitialAdPolicy)
        } else {
            Log.e(TAG, "Facebook: Interstitial ad not loaded yet")
        }
    }

    private fun showAdMobInterstitialAd(interstitialAdPolicy: InterstitialAdPolicy.InterstitialAdmobPolicy) {
        check(::adMobInterstitialAd.isInitialized) {
            "loadInterstitialAd() not called before showInterstitialAd()"
        }
        if (adMobInterstitialAd.isLoaded) {
            adMobInterstitialAd.adListener = object : AdmobAdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    Log.i(TAG, "AdMob: Interstitial ad closed")
                    loadAdMobInterstitialAd(interstitialAdPolicy)
                    interstitialAdPolicy.admobCallback?.onAdDismiss?.invoke()
                }

                override fun onAdFailedToLoad(p0: Int) {
                    Log.e(TAG, "AdMob: Unable to load AdMob banner ad code: $p0")
                    interstitialAdPolicy.admobCallback?.onInterstitialFailedToLoad?.invoke()
                    super.onAdFailedToLoad(p0)
                }
            }
            adMobInterstitialAd.show()
            loadAdMobInterstitialAd(interstitialAdPolicy)
        } else {
            Log.e(TAG, "AdMob: Interstitial ad not loaded yet")
        }
    }

    private lateinit var adMobRewardedAd: com.google.android.gms.ads.rewarded.RewardedAd

    override fun loadRewardedAd(rewardedAdPolicy: RewardedAdPolicy) {
            when (rewardedAdPolicy) {
                is RewardedAdPolicy.RewardedFacebookPolicy -> {
                    throw IllegalArgumentException("Facebook rewarded ad support is currently in progress")
                }
                is RewardedAdPolicy.RewardedAdmobPolicy -> {
                    loadAdMobRewardedAd(rewardedAdPolicy)
                }
                is RewardedAdPolicy.RewardedMopubPolicy -> {
                    throw IllegalArgumentException("MoPub rewarded ad support is currently in progress")
                }
            }
    }

    private fun loadAdMobRewardedAd(rewardedAdPolicy: RewardedAdPolicy.RewardedAdmobPolicy) {
        adMobRewardedAd = AdmobRewardedAd(context, rewardedAdPolicy.adMobAdId)
        val rewardedAdLoadCallback = object : AdmobRewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                super.onRewardedAdLoaded()
                rewardedAdPolicy.admobCallback?.onRewardedAdLoaded?.invoke()
            }

            override fun onRewardedAdFailedToLoad(p0: Int) {
                super.onRewardedAdFailedToLoad(p0)
                rewardedAdPolicy.admobCallback?.onRewardedAdLoadFailed?.invoke()
            }
        }
        adMobRewardedAd.loadAd(AdmobAdRequest.Builder().build(), rewardedAdLoadCallback)
    }

    override fun showRewardedAd(rewardedAdPolicy: RewardedAdPolicy) {
            when (rewardedAdPolicy) {
                is RewardedAdPolicy.RewardedFacebookPolicy -> {
                    throw IllegalArgumentException("Facebook rewarded ad support is currently in progress")
                }
                is RewardedAdPolicy.RewardedAdmobPolicy -> {
                    check(::adMobRewardedAd.isInitialized) {
                        "loadRewardedAd() not called before showRewardedAd()"
                    }
                    if (adMobRewardedAd.isLoaded) {
                        val rewardedAdCallback = object : AdmobRewardedAdCallback() {
                            override fun onUserEarnedReward(rewardItem: AdmobRewardItem) {
                                val reward = RewardItem.AdmobRewardItem(rewardItem.amount, rewardItem.type)
                                rewardedAdPolicy.admobCallback?.onRewardEarned?.invoke(reward)
                            }

                            override fun onRewardedAdFailedToShow(p0: Int) {
                                super.onRewardedAdFailedToShow(p0)
                                Log.e(TAG, "AdMob: Unable to show AdMob Rewarded Ad code: $p0")
                                rewardedAdPolicy.admobCallback?.onRewardedAdFailedToShow?.invoke()
                            }

                            override fun onRewardedAdClosed() {
                                super.onRewardedAdClosed()
                                rewardedAdPolicy.admobCallback?.onRewardedAdClosed?.invoke()
                            }
                        }
                        adMobRewardedAd.show(context as? BaseActivity, rewardedAdCallback)
                        loadAdMobRewardedAd(rewardedAdPolicy)
                    } else {
                        Log.e(TAG, "AdMob: Rewarded ad not loaded yet")
                    }

                }
                is RewardedAdPolicy.RewardedMopubPolicy -> {
                    throw IllegalArgumentException("MoPub rewarded ad support is currently in progress")
                }
        }
    }

    override fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        bannerAdPolicy: BannerAdPolicy
    ) {
        context?.let { context ->
            when (bannerAdPolicy) {
                is BannerAdPolicy.BannerFacebookPolicy -> {
                    FacebookAudienceNetworkAds.initialize(context.applicationContext)
                    bannerAdFragment = FacebookBannerAdFragment()
                        .apply {
                        this.arguments = bundleOf(
                            "data" to bundleOf(
                                "facebookAdId" to bannerAdPolicy.facebookAdId
                            )
                        )
                        this.onBannerAdFailedToLoad = bannerAdPolicy.facebookCallback?.onBannerFailedToLoad
                    }
                }
                is BannerAdPolicy.BannerAdmobPolicy -> {
                    AdmobMobileAds.initialize(context.applicationContext, bannerAdPolicy.adMobAppId)
                    bannerAdFragment = AdmobBannerAdFragment()
                        .apply {
                        this.arguments = bundleOf(
                            "data" to bundleOf(
                                "adMobAdId" to bannerAdPolicy.adMobAdId
                            )
                        )
                        this.onBannerAdFailedToLoad = bannerAdPolicy.adMobCallback?.onBannerFailedToLoad
                    }
                }
                is BannerAdPolicy.BannerMopubPolicy-> {
                    throw IllegalArgumentException("MoPub Banner Ad support is currently in progress")
                }
            }

            fragmentManager
                .beginTransaction()
                .replace(replaceLayout, bannerAdFragment)
                .commit()
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