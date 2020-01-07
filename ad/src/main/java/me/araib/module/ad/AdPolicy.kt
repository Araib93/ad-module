package me.araib.module.ad

import java.io.Serializable

data class AdPolicy(val adId: String, val type: Type, val appId: String? = null) : Serializable

enum class Type { FACEBOOK, ADMOB, MOPUB }