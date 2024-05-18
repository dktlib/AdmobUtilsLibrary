package com.vapp.admoblibrary.ads.remote

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.vapp.admoblibrary.ads.model.BannerConfigHolder
import com.vapp.admoblibrary.ads.remote.BannerPlugin.Companion.log

@SuppressLint("ViewConstructor")
internal class BannerAdView(
    private val activity: Activity,
    adUnitId: String,
    private val bannerType: BannerPlugin.BannerType,
    refreshRateSec: Int?,
    private val cbFetchIntervalSec: Int,val bannerRemoteConfig: BannerRemoteConfig,val bannerConfigHolder: BannerConfigHolder
) : BaseAdView(activity, refreshRateSec) {
    companion object{
        var lastCBRequestTime = 0L
    }
    private var hasSetAdSize = false


    init {
        bannerConfigHolder.mAdView = AdView(activity)
        bannerConfigHolder.mAdView?.adUnitId = adUnitId
        addView(bannerConfigHolder.mAdView, getCenteredLayoutParams(this))
    }

    private fun getCenteredLayoutParams(container: ViewGroup) = when (container) {
        is FrameLayout -> LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER
        }
        is LinearLayout -> LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER
        }
        else -> LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    override fun loadAdInternal(onDone: () -> Unit) {
        if (!hasSetAdSize) {
            doOnLayout {
                val adSize = getAdSize(bannerType)
                bannerConfigHolder.mAdView?.setAdSize(adSize)
                bannerConfigHolder.mAdView?.updateLayoutParams {
                    width = adSize.getWidthInPixels(activity)
                    height = adSize.getHeightInPixels(activity)
                }
                hasSetAdSize = true
                doLoadAd(onDone)
            }
        } else {
            doLoadAd(onDone)
        }
    }

    private fun getAdSize(bannerType: BannerPlugin.BannerType): AdSize {
        return when (bannerType) {
            BannerPlugin.BannerType.Standard -> AdSize.BANNER
            BannerPlugin.BannerType.Adaptive,
            BannerPlugin.BannerType.CollapsibleBottom,
            BannerPlugin.BannerType.CollapsibleTop -> {
                val displayMetrics = activity.resources.displayMetrics

                var adWidthPx = width.toFloat()
                if (adWidthPx == 0f) {
                    adWidthPx = displayMetrics.widthPixels.toFloat()
                }

                val density = displayMetrics.density
                val adWidth = (adWidthPx / density).toInt()

                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
            }
        }
    }

    private fun doLoadAd(onDone: () -> Unit) {
        var isCollapsibleBannerRequest = false
        val adRequestBuilder = AdRequest.Builder()
        when (bannerType) {
            BannerPlugin.BannerType.CollapsibleTop,
            BannerPlugin.BannerType.CollapsibleBottom -> {
                log("shouldRequestCollapsible() = ${shouldRequestCollapsible()}")
                if (shouldRequestCollapsible()) {
                    val position =
                        if (bannerType == BannerPlugin.BannerType.CollapsibleTop) "top" else "bottom"
                    adRequestBuilder.addNetworkExtrasBundle(
                        AdMobAdapter::class.java, Bundle().apply {
                            putString("collapsible", position)
                        }
                    )
                    isCollapsibleBannerRequest = true
                }
            }
            else -> {}
        }

        if (isCollapsibleBannerRequest) {
            lastCBRequestTime = System.currentTimeMillis()
        }
        bannerConfigHolder.mAdView?.onPaidEventListener = OnPaidEventListener { adValue -> bannerRemoteConfig.onAdPaid(adValue,bannerConfigHolder.mAdView!!) }
        bannerConfigHolder.mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                bannerConfigHolder.mAdView?.adListener = object : AdListener() {}
                onDone()
                BannerPlugin.shimmerFrameLayout?.stopShimmer()
                bannerRemoteConfig.onBannerAdLoaded(getAdSize(bannerType))

            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                bannerConfigHolder.mAdView?.adListener = object : AdListener() {}
                onDone()
                BannerPlugin.shimmerFrameLayout?.stopShimmer()
                bannerRemoteConfig.onAdFail()
            }
        }
        bannerConfigHolder.mAdView?.loadAd(adRequestBuilder.build())
    }

    private fun shouldRequestCollapsible(): Boolean {
        return System.currentTimeMillis() - lastCBRequestTime >= cbFetchIntervalSec * 1000L
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) {
            bannerConfigHolder.mAdView?.resume()
        } else {
            bannerConfigHolder.mAdView?.pause()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bannerConfigHolder.mAdView?.adListener = object : AdListener() {}
        bannerConfigHolder.mAdView?.destroy()
    }
}