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
        loadFacebookInterstitialAd(facebookAdId)
        loadAdMobInterstitialAd(adMobAdId)
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
                        loadFacebookInterstitialAd(facebookAdId)
                        onAdDismiss?.invoke()
                    }
                })
                facebookInterstitialAd.show()
                loadFacebookInterstitialAd(facebookAdId)
            }
            adMobInterstitialAd.isLoaded -> {
                adMobInterstitialAd.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.i(TAG, "AdMob: Interstitial ad closed")
                        loadAdMobInterstitialAd(adMobAdId)
                        onAdDismiss?.invoke()
                    }
                }
                adMobInterstitialAd.show()
                loadAdMobInterstitialAd(adMobAdId)
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