package me.araib.module.ad.banners

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdView
import me.araib.module.ad.R

class FacebookBannerAdFragment : BannerAdFragment() {
    internal val facebookAdId by lazy {
        arguments?.getBundle("data")?.getString("facebookAdId")
            ?: throw IllegalArgumentException("Facebook ad id not found")
    }

    var onBannerFailedToLoad: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_facebook_banner_ad, parent, false) as FrameLayout?

        val facebookAdView = AdView(
            context,
            facebookAdId,
            com.facebook.ads.AdSize.BANNER_HEIGHT_50
        )

        facebookAdView.setAdListener(object : AdListener {
            override fun onAdClicked(p0: Ad?) {
                Log.i(TAG, "AdMob: Banner ad clicked")
                // Implementation not required
            }

            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e(TAG, "Facebook: ${p1?.errorMessage ?: "Unable to load Facebook banner ad"} code: ${p1?.errorCode?: "Unknown"}")
                onBannerFailedToLoad?.invoke()
            }

            override fun onAdLoaded(p0: Ad?) {
                Log.i(TAG, "Facebook: Banner ad loaded")
                // Implementation not required
            }

            override fun onLoggingImpression(p0: Ad?) {
                // Implementation not required
            }
        })

        if (view == null)
            Log.e(TAG, "Facebook: Banner ad view is null")
        else {
            view.addView(facebookAdView)
        }
        facebookAdView.loadAd()

        return view
    }
}