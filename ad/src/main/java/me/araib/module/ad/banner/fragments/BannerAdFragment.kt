package me.araib.module.ad.banner.fragments

import android.view.ViewGroup
import androidx.fragment.app.Fragment

open abstract class BannerAdFragment : Fragment() {
    companion object {
        internal val TAG: String? = "Trait: Ad"
    }

    abstract fun loadBannerAd(view: ViewGroup?)
}