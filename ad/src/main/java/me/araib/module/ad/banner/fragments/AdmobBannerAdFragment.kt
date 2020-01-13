package me.araib.module.ad.banner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import me.araib.module.ad.R
import me.araib.module.ad.banner.BannerAdCallback

class AdmobBannerAdFragment : BannerAdFragment() {
    private val adMobAdId by lazy {
        arguments?.getBundle("data")?.getString("adMobAdId")
            ?: throw IllegalArgumentException("AdMob ad id not found")
    }

    internal var adMobCallback: BannerAdCallback.BannerAdmobCallback? = null
    internal var debug = false

    override fun loadBannerAd(view: ViewGroup?) {
        val admobAdView = AdView(context)

        admobAdView.apply {
            adSize = AdSize.BANNER
            adUnitId = adMobAdId
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    if (debug)
                        Log.e(TAG, "AdMob: Unable to load AdMob banner ad code: $p0")
                    adMobCallback?.onBannerAdFailedToLoad?.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (debug)
                        Log.i(TAG, "AdMob: Banner ad loaded")
                    adMobCallback?.onBannerAdLoaded?.invoke()
                }
            }
        }

        view?.addView(admobAdView) ?: run {
            if (debug)
                Log.e(TAG, "AdMob: Banner ad view is null")
            adMobCallback?.onBannerAdFailedToLoad?.invoke()
        }

        admobAdView.loadAd(AdRequest.Builder().build())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_admob_banner_ad, container, false) as FrameLayout?
        loadBannerAd(view)
        return view
    }
}