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
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import me.araib.module.ad.banners.BannerAdFragment
import me.araib.module.ad.banners.AdmobBannerAdFragment
import me.araib.module.ad.banners.FacebookBannerAdFragment

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

    override fun loadInterstitialAd(vararg policies: AdPolicy) {
        for(policy in policies) {
            when (policy.type) {
                Type.FACEBOOK -> {
                    loadFacebookInterstitialAd(policy.adId)
                }
                Type.ADMOB -> {
                    loadAdMobInterstitialAd(policy.adId)
                }
                Type.MOPUB -> {
                    throw IllegalArgumentException("MoPub support is currently in progress")
                }
            }
        }
    }

    private fun loadFacebookInterstitialAd(facebookAdId: String) {
        if (::facebookInterstitialAd.isInitialized) {
            facebookInterstitialAd.destroy()
        }
        facebookInterstitialAd = com.facebook.ads.InterstitialAd(context, facebookAdId)
        facebookInterstitialAd.loadAd()
    }

    private fun loadAdMobInterstitialAd(adMobAdId: String) {
        adMobInterstitialAd = com.google.android.gms.ads.InterstitialAd(context)
        adMobInterstitialAd.adUnitId = adMobAdId
        adMobInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun showInterstitialAd(vararg policies: AdPolicy, onAdDismiss: (() -> Unit)?, onInterstitialFailedToLoad: (() -> Unit)?) {
        for (policy in policies) {
            when (policy.type) {
                Type.FACEBOOK -> {
                    check(::facebookInterstitialAd.isInitialized) {
                        "loadInterstitialAd() not called before showInterstitialAd()"
                    }
                    if(facebookInterstitialAd.isAdLoaded){
                        facebookInterstitialAd.setAdListener(object : FacebookInterstitialAdListener() {
                            override fun onInterstitialDismissed(p0: Ad?) {
                                Log.i(TAG, "Facebook: Interstitial ad dismissed")
                                loadFacebookInterstitialAd(policy.adId)
                                onAdDismiss?.invoke()
                            }

                            override fun onError(p0: Ad?, p1: AdError?) {
                                Log.e(TAG, "Facebook: ${p1?.errorMessage ?: "Unable to load Facebook banner ad"} code: ${p1?.errorCode?: "Unknown"}")
                                onInterstitialFailedToLoad?.invoke()
                                super.onError(p0, p1)
                            }
                        })
                        facebookInterstitialAd.show()
                        loadFacebookInterstitialAd(policy.adId)
                    } else {
                        Log.e(TAG, "Facebook: Interstitial ad not loaded yet")
                    }
                }
                Type.ADMOB -> {
                    check(::adMobInterstitialAd.isInitialized) {
                        "loadInterstitialAd() not called before showInterstitialAd()"
                    }
                     if(adMobInterstitialAd.isLoaded) {
                        adMobInterstitialAd.adListener = object : AdListener() {
                            override fun onAdClosed() {
                                super.onAdClosed()
                                Log.i(TAG, "AdMob: Interstitial ad closed")
                                loadAdMobInterstitialAd(policy.adId)
                                onAdDismiss?.invoke()
                            }

                            override fun onAdFailedToLoad(p0: Int) {
                                Log.e(TAG, "AdMob: Unable to load AdMob banner ad code: $p0")
                                onInterstitialFailedToLoad?.invoke()
                                super.onAdFailedToLoad(p0)
                            }
                        }
                        adMobInterstitialAd.show()
                        loadAdMobInterstitialAd(policy.adId)
                    } else {
                         Log.e(TAG, "AdMob: Interstitial ad not loaded yet")
                    }
                }
                Type.MOPUB -> {
                    throw IllegalArgumentException("MoPub support is currently in progress")
                }
            }
        }
    }

    override fun showBannerAd(
        fragmentManager: FragmentManager, @IdRes replaceLayout: Int,
        policy: AdPolicy,
        onBannerFailedToLoad: (() -> Unit)?
    ) {
        context?.let { context ->
                when(policy.type) {
                    Type.FACEBOOK -> {
                        AudienceNetworkAds.initialize(context.applicationContext)
                        bannerAdFragment = FacebookBannerAdFragment().apply {
                            this.arguments = bundleOf(
                                "data" to bundleOf(
                                    "facebookAdId" to policy.adId
                                )
                            )
                            this.onBannerFailedToLoad = onBannerFailedToLoad
                        }
                    }
                    Type.ADMOB -> {
                        MobileAds.initialize(context.applicationContext, policy.appId)
                        bannerAdFragment = AdmobBannerAdFragment().apply {
                            this.arguments = bundleOf(
                                "data" to bundleOf(
                                    "adMobAdId" to policy.adId
                                )
                            )
                            this.onBannerFailedToLoad = onBannerFailedToLoad
                        }
                    }
                    Type.MOPUB -> {
                        throw IllegalArgumentException("MoPub support is currently in progress")
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