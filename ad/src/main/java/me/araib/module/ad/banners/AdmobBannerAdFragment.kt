package me.araib.module.ad.banners

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

    var onBannerFailedToLoad: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admob_banner_ad, container, false) as FrameLayout?

        val admobAdView = AdView(context)

        admobAdView.apply {
            adSize = AdSize.BANNER
            adUnitId = adMobAdId
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    Log.e(TAG, "AdMob: Unable to load AdMob banner ad code: $p0")
                    onBannerFailedToLoad?.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG, "AdMob: Banner ad loaded")
                }
            }
        }

        if (view == null)
            Log.e(TAG, "AdMob: Banner ad view is null")
        else {
            view.addView(admobAdView)
        }
        admobAdView.loadAd(AdRequest.Builder().build())

        return view
    }
}