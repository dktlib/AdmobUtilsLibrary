package com.vapp.admoblibrary.ads.remote

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.vapp.admoblibrary.ads.remote.BannerPlugin.Companion.log
import kotlin.math.max

internal abstract class BaseAdView(
    context: Context,
    private val refreshRateSec: Int?
) : FrameLayout(context) {

    private var nextRefreshTime = 0L
    private val refreshHandler by lazy { Handler(Looper.getMainLooper()) }

    private var isPausedOrDestroy = false

    fun loadAd() {
        log("LoadAd ...")
        nextRefreshTime = 0L // Not allow scheduling until ad request is done
        stopBannerRefreshScheduleIfNeed()

        loadAdInternal {
            log("On load ad done ...")
            calculateNextBannerRefresh()
            if (!isPausedOrDestroy) scheduleNextBannerRefreshIfNeed()
        }
    }

    protected abstract fun loadAdInternal(onDone: () -> Unit)

    @CallSuper
    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) onResume()
        else onPause()
    }

    @CallSuper
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onDestroy()
    }

    private fun onResume() {
        isPausedOrDestroy = false
        scheduleNextBannerRefreshIfNeed()
    }

    private fun onPause() {
        isPausedOrDestroy = true
        stopBannerRefreshScheduleIfNeed()
    }

    private fun onDestroy() {
        isPausedOrDestroy = true
        stopBannerRefreshScheduleIfNeed()
    }

    private fun calculateNextBannerRefresh() {
        if (refreshRateSec == null) return
        nextRefreshTime = System.currentTimeMillis() + refreshRateSec * 1000L
    }

    private fun scheduleNextBannerRefreshIfNeed() {
        if (refreshRateSec == null) return
        if (nextRefreshTime <= 0L) return

        val delay = max(0L, nextRefreshTime - System.currentTimeMillis())

        stopBannerRefreshScheduleIfNeed()
        //Check size FrameLayout
        log("Ads are scheduled to show in $delay mils")
        refreshHandler.postDelayed({ loadAd() }, delay)
    }

    private fun stopBannerRefreshScheduleIfNeed() {
        refreshHandler.removeCallbacksAndMessages(null)
    }

    internal object Factory {
        fun getAdView(
            activity: Activity,
            adUnitId: String,
            bannerType: BannerPlugin.BannerType,
            refreshRateSec: Int?,
            cbFetchIntervalSec: Int,bannerRemoteConfig: BannerRemoteConfig
        ): BaseAdView {
            return when (bannerType) {
                BannerPlugin.BannerType.Adaptive,
                BannerPlugin.BannerType.Standard,
                BannerPlugin.BannerType.CollapsibleBottom,
                BannerPlugin.BannerType.CollapsibleTop -> BannerAdView(activity, adUnitId, bannerType, refreshRateSec, cbFetchIntervalSec ,bannerRemoteConfig)
            }
        }
    }
}