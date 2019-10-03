package me.araib.module.ad

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_ad.view.*

class AdFragment : Fragment() {
    companion object {
        fun getInstance(
            facebookAdId: String,
            adMobAdId: String,
            onBannerFailedToLoad: (() -> Unit)? = null
        ): AdFragment {
            val fragment = AdFragment()
            fragment.arguments = bundleOf(
                "data" to bundleOf(
                    "facebookAdId" to facebookAdId,
                    "adMobAdId" to adMobAdId
                )
            )
            fragment.onBannerFailedToLoad = onBannerFailedToLoad
            return fragment
        }
    }

    var onBannerFailedToLoad: (() -> Unit)? = null

    private val facebookAdId by lazy {
        arguments?.getBundle("data")?.getString("facebookAdId")
            ?: throw IllegalArgumentException("Facebook ad id not found")
    }
    private val adMobAdId by lazy {
        arguments?.getBundle("data")?.getString("adMobAdId")
            ?: throw IllegalArgumentException("AdMob ad id not found")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ad, container, false)

        val facebookAdView =
            com.facebook.ads.AdView(context, facebookAdId, com.facebook.ads.AdSize.BANNER_HEIGHT_50)
        facebookAdView.setAdListener(object : AdListener {
            override fun onAdClicked(p0: Ad?) {
                // Implementation not required
            }

            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e("Trait: Ad", "Facebook: ${p1?.errorMessage ?: "AdError is null"}")
                val adMobAdView = AdView(context)
                adMobAdView.adSize = AdSize.BANNER
                adMobAdView.adUnitId = adMobAdId

                adMobAdView.adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(p0: Int) {
                        super.onAdFailedToLoad(p0)
                        Log.e("Trait: Ad", "Google: $p0")
                        onBannerFailedToLoad?.invoke()
                    }
                }

                view.container.removeView(facebookAdView)
                view.container.addView(adMobAdView)
                adMobAdView.loadAd(AdRequest.Builder().build())
            }

            override fun onAdLoaded(p0: Ad?) {
                // Implementation not required
            }

            override fun onLoggingImpression(p0: Ad?) {
                // Implementation not required
            }
        })

        view.container.addView(facebookAdView)
        facebookAdView.loadAd()

        return view
    }
}