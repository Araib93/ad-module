package me.araib.module.ad

import android.content.Context
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest

class AdTraitImpl : LifecycleObserver, AdTrait {
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

    override fun showInterstitialAd(
        facebookAdId: String,
        googleAdId: String,
        onAdDismiss: (() -> Unit)?
    ) {
        val facebookInterstitialAd = com.facebook.ads.InterstitialAd(context, facebookAdId)

        facebookInterstitialAd.setAdListener(object : com.facebook.ads.InterstitialAdListener {
            override fun onInterstitialDisplayed(p0: Ad?) {
                // Implementation not required
            }

            override fun onAdClicked(p0: Ad?) {
                // Implementation not required
            }

            override fun onInterstitialDismissed(p0: Ad?) {
                onAdDismiss?.invoke()
            }

            override fun onError(p0: Ad?, p1: AdError?) {
                val googleInterstitialAd = com.google.android.gms.ads.InterstitialAd(context)

                googleInterstitialAd.adUnitId = googleAdId

                googleInterstitialAd.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(p0: Int) {
                        super.onAdFailedToLoad(p0)
                        Log.e("Trait: Ad", "Unable to load ad")
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        onAdDismiss?.invoke()
                    }
                }

                googleInterstitialAd.loadAd(AdRequest.Builder().build())
            }

            override fun onAdLoaded(p0: Ad?) {
                // Implementation not required
            }

            override fun onLoggingImpression(p0: Ad?) {
                // Implementation not required
            }

        })

        facebookInterstitialAd.loadAd()
    }

    override fun showBannerAd(
        fragmentManager: FragmentManager,
        @IdRes replaceLayout: Int,
        facebookAdId: String,
        googleAdId: String,
        onBannerFailedToLoad: (() -> Unit)?
    ) {
        adFragment = AdFragment.getInstance(facebookAdId, googleAdId, onBannerFailedToLoad)
        fragmentManager
            .beginTransaction()
            .replace(replaceLayout, adFragment)
            .commit()
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