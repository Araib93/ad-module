package me.araib.module.ad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize

class AdFragment : Fragment() {
    companion object {
        private val TAG: String? = "Trait: Ad"
    }

    private val facebookAdId by lazy {
        arguments?.getBundle("data")?.getString("facebookAdId")
            ?: throw IllegalArgumentException("Facebook ad id not found")
    }
    private val adMobAdId by lazy {
        arguments?.getBundle("data")?.getString("adMobAdId")
            ?: throw IllegalArgumentException("AdMob ad id not found")
    }

    var onBannerFailedToLoad: (() -> Unit)? = null

    private lateinit var facebookAdView: com.facebook.ads.AdView
    private lateinit var adMobAdView: com.google.android.gms.ads.AdView

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ad, parent, false) as FrameLayout?
        facebookAdView = com.facebook.ads.AdView(
            context,
            facebookAdId,
            com.facebook.ads.AdSize.BANNER_HEIGHT_50
        )

        facebookAdView.setAdListener(object : AdListener {
            override fun onAdClicked(p0: Ad?) {
                // Implementation not required
            }

            override fun onError(p0: Ad?, p1: AdError?) {
                if (context == null) {
                    Log.d(TAG, "onError:Context Null")
                    return
                }
                Log.e(TAG, "Facebook: ${p1?.errorMessage ?: "AdError is null"}")
                adMobAdView = com.google.android.gms.ads.AdView(context).apply {
                    adSize = AdSize.BANNER
                    adUnitId = adMobAdId
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(p0: Int) {
                            super.onAdFailedToLoad(p0)
                            Log.e(TAG, "AdMob: $p0")
                            onBannerFailedToLoad?.invoke()
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.i(TAG, "AdMob: Ad loaded")
                        }
                    }
                }

                view?.removeView(facebookAdView)
                view?.addView(adMobAdView)
                Log.e(TAG, "onCreate: container is null while loading AdMob ad")
                adMobAdView.loadAd(AdRequest.Builder().build())
            }

            override fun onAdLoaded(p0: Ad?) {
                Log.i("Trait: Ad", "Facebook: Ad loaded")
                // Implementation not required
            }

            override fun onLoggingImpression(p0: Ad?) {
                // Implementation not required
            }
        })

        view?.addView(facebookAdView)
        Log.e(TAG, "onCreate: container is null while loading Facebook ad")
        facebookAdView.loadAd()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach:Called")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach:Called")
    }
}