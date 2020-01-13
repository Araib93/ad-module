package me.araib.module.ad.banner.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import me.araib.module.ad.R
import me.araib.module.ad.banner.BannerAdCallback
import me.araib.module.ad.banner.FacebookBannerAdListener

class FacebookBannerAdFragment : BannerAdFragment() {
    private val facebookAdId by lazy {
        arguments?.getBundle("data")?.getString("facebookAdId")
            ?: throw IllegalArgumentException("Facebook ad id not found")
    }

    internal var facebookCallback: BannerAdCallback.BannerFacebookCallback? = null
    internal var debug = true

    override fun loadBannerAd(view: ViewGroup?) {
        val facebookAdView = AdView(
            context,
            facebookAdId,
            AdSize.BANNER_HEIGHT_50
        )

        facebookAdView.setAdListener(object : FacebookBannerAdListener() {
            override fun onError(p0: Ad?, p1: AdError?) {
                super.onError(p0, p1)
                if (debug)
                    Log.e(
                        TAG,
                        "Facebook: ${p1?.errorMessage
                            ?: "Unable to load Facebook banner ad"} code: ${p1?.errorCode
                            ?: "Unknown"}"
                    )
                facebookCallback?.onBannerAdFailedToLoad?.invoke()
            }

            override fun onAdLoaded(p0: Ad?) {
                super.onAdLoaded(p0)
                if (debug)
                    Log.i(TAG, "Facebook: Banner ad loaded")
                facebookCallback?.onBannerAdLoaded?.invoke()
                // Implementation not required
            }
        })

        view?.addView(facebookAdView) ?: run {
            if (debug)
                Log.e(TAG, "Facebook: Banner ad view is null")
            facebookCallback?.onBannerAdFailedToLoad?.invoke()
        }

        facebookAdView.loadAd()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_facebook_banner_ad, parent, false) as FrameLayout?
        loadBannerAd(view)
        return view
    }
}