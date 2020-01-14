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
    private val admobAdId by lazy {
        arguments?.getBundle("data")?.getString("admobAdId")
            ?: throw IllegalArgumentException("Admob ad id not found")
    }

    internal var admobCallback: BannerAdCallback.BannerAdmobCallback? = null
    internal var shouldShowLogs = false

    override fun loadBannerAd(view: ViewGroup?) {
        val admobAdView = AdView(context)

        admobAdView.apply {
            adSize = AdSize.BANNER
            adUnitId = admobAdId
            adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    if (shouldShowLogs)
                        Log.e(TAG, "Admob: Unable to load Admob banner ad code: $p0")
                    admobCallback?.onBannerAdFailedToLoad?.invoke()
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    if (shouldShowLogs)
                        Log.i(TAG, "Admob: Banner ad loaded")
                    admobCallback?.onBannerAdLoaded?.invoke()
                }
            }
        }

        view?.addView(admobAdView) ?: run {
            if (shouldShowLogs)
                Log.e(TAG, "Admob: Banner ad view is null")
            admobCallback?.onBannerAdFailedToLoad?.invoke()
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