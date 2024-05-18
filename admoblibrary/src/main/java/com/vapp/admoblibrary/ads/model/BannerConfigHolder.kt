package com.vapp.admoblibrary.ads.model

import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdView
import com.vapp.admoblibrary.ads.remote.BaseAdView
import java.util.concurrent.atomic.AtomicBoolean

class BannerConfigHolder(var ads: String) {
    var mAdView: AdView? = null
    val refreshHandler by lazy { Handler(Looper.getMainLooper()) }
}