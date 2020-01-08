package me.araib.module.ad.interstitial

import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener

open class FacebookInterstitialAdListener : InterstitialAdListener {
    override fun onInterstitialDisplayed(p0: Ad?) {
        // override as required
    }

    override fun onAdClicked(p0: Ad?) {
        // override as required
    }

    override fun onInterstitialDismissed(p0: Ad?) {
        // override as required
    }

    override fun onError(p0: Ad?, p1: AdError?) {
        // override as required
    }

    override fun onAdLoaded(p0: Ad?) {
        // override as required
    }

    override fun onLoggingImpression(p0: Ad?) {
        // override as required
    }
}