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

class AdmobBannerAdFragment : BannerAdFragment() {
    internal val adMobAdId by lazy {
        arguments?.getBundle("data")?.getString("adMobAdId")
            ?: throw IllegalArgumentException("AdMob ad id not found")
    }

    var onBannerAdFailedToLoad: (() -> Unit)? = null
    var onBannerAdLoaded: (() -> Unit)? = null

    override fun loadBannerAd(view: ViewGroup?) {
        val admobAdView = AdView(context)

        admobAdView.apply {
            adSize = AdSize.BANNER
            adUnitId = adMobAdId
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    Log.e(TAG, "AdMob: Unable to load AdMob banner ad code: $p0")
                    onBannerAdFailedToLoad?.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG, "AdMob: Banner ad loaded")
                    onBannerAdLoaded?.invoke()
                }
            }
        }

        view?.addView(admobAdView) ?: run {
            Log.e(TAG, "AdMob: Banner ad view is null")
            onBannerAdFailedToLoad?.invoke()
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