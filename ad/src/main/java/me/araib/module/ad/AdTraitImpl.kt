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

class AdTraitImpl : LifecycleObserver, AdTrait {
    companion object {
        private val TAG: String? = "Trait: Ad"
    }

    private var context: Context? = null
    private lateinit var adFragment: AdFragment

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

    override fun loadInterstitialAd(
        facebookAdId: String,
        adMobAdId: String
    ) {
        if (::facebookInterstitialAd.isInitialized) {
            facebookInterstitialAd.destroy()
        }
        facebookInterstitialAd = com.facebook.ads.InterstitialAd(context, facebookAdId)
        facebookInterstitialAd.loadAd()

        val adMobInterstitialAd = com.google.android.gms.ads.InterstitialAd(context)
        adMobInterstitialAd.adUnitId = adMobAdId
        adMobInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun showInterstitialAd(
        facebookAdId: String,
        adMobAdId: String,
        onAdDismiss: (() -> Unit)?
    ) {
        check(::facebookInterstitialAd.isInitialized && ::adMobInterstitialAd.isInitialized) {
            "loadInterstitialAd() not called before showInterstitialAd()"
        }

        when {
            facebookInterstitialAd.isAdLoaded -> {
                facebookInterstitialAd.setAdListener(object : FacebookInterstitialAdListener() {
                    override fun onInterstitialDismissed(p0: Ad?) {
                        Log.i(TAG, "Facebook: Interstitial ad dismissed")
                        loadInterstitialAd(
                            facebookAdId = facebookAdId,
                            adMobAdId = adMobAdId)
                        onAdDismiss?.invoke()
                    }

                    override fun onError(p0: Ad?, p1: AdError?) {
                        Log.e(
                            TAG,
                            "Facebook: ${p1?.errorMessage
                                ?: "Unable to load Facebook interstitial ad"} code: ${p1?.errorCode
                                ?: "Unknown"}"
                        )
                        loadInterstitialAd(
                            facebookAdId = facebookAdId,
                            adMobAdId = adMobAdId
                        )
                    }
                })
                facebookInterstitialAd.show()
            }
            adMobInterstitialAd.isLoaded -> {
                adMobInterstitialAd.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(p0: Int) {
                        super.onAdFailedToLoad(p0)
                        Log.e(TAG, "AdMob: Unable to load AdMob interstitial ad code: $p0")
                        loadInterstitialAd(
                            facebookAdId = facebookAdId,
                            adMobAdId = adMobAdId
                        )
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.i(TAG, "AdMob: Interstitial ad closed")
                        onAdDismiss?.invoke()
                    }
                }
                adMobInterstitialAd.show()
            }
            else -> Log.e(TAG, "showInterstitialAd() called but ads not loaded")
        }
    }

    override fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        facebookAdId: String,
        adMobAdId: String,
        adMobAppId: String,
        onBannerFailedToLoad: (() -> Unit)?
    ) {
        context?.let { context ->
            AudienceNetworkAds.initialize(context.applicationContext)
            MobileAds.initialize(context.applicationContext, adMobAdId)

            adFragment = AdFragment().apply {
                this.arguments = bundleOf(
                    "data" to bundleOf(
                        "facebookAdId" to facebookAdId,
                        "adMobAdId" to adMobAdId
                    )
                )
                this.onBannerFailedToLoad = onBannerFailedToLoad
            }

            fragmentManager
                .beginTransaction()
                .replace(replaceLayout, adFragment)
                .commit()
        }
    }

    override fun removeBannerAd(fragmentManager: FragmentManager): Boolean {
        if (::adFragment.isInitialized && adFragment.isAdded) {
            fragmentManager
                .beginTransaction()
                .remove(adFragment)
                .commit()
            return true
        }
        return false
    }
}