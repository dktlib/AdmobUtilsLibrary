package com.vapp.admoblibrary.ads.remote

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.annotations.SerializedName
import com.vapp.admoblibrary.R
import com.vapp.admoblibrary.ads.AdmobUtils
import com.vapp.admoblibrary.ads.remote.BannerPlugin.BannerConfig.Companion.TYPE_ADAPTIVE
import com.vapp.admoblibrary.ads.remote.BannerPlugin.BannerConfig.Companion.TYPE_COLLAPSIBLE_BOTTOM
import com.vapp.admoblibrary.ads.remote.BannerPlugin.BannerConfig.Companion.TYPE_COLLAPSIBLE_TOP
import com.vapp.admoblibrary.ads.remote.BannerPlugin.BannerConfig.Companion.TYPE_STANDARD

@SuppressLint("ViewConstructor")
class BannerPlugin(
    private val activity: Activity,
    private val adContainer: ViewGroup,
    private val id: String,
    private val bannerConfig: BannerConfig?,
    var bannerRemoteConfig: BannerRemoteConfig
) {
    companion object {

        var shimmerFrameLayout: ShimmerFrameLayout? = null
        private var LOG_ENABLED = true

        fun setLogEnabled(enabled: Boolean) {
            LOG_ENABLED = enabled
        }

        internal fun log(message: String) {
            if (LOG_ENABLED) {
                Log.d("BannerPlugin", message)
            }
        }
    }

    class Config {
        lateinit var defaultAdUnitId: String
        lateinit var defaultBannerType: BannerType
        /**
         * Remote config key to retrieve banner config data remotely
         * */
        var configKey: String? = null

        /**
         * Banner refresh rate, in seconds. Pub are recommended to disable auto refresh from dashboard
         * Most of the case this is used to refresh a collapsible banner manually
         * */
        var defaultRefreshRateSec: Int? = null

        /**
         * In seconds, indicate minimum time b/w 2 collapsible banner requests.
         * Only works with BannerType.CollapsibleTop or BannerType.CollapsibleBottom
         * If it is the time to send ad request but the duration to last request collapsible banner < cbFetchInterval,
         * Adaptive banner will be shown instead.
         * */
        var defaultCBFetchIntervalSec: Int = 180
        var loadAdAfterInit = true
    }

    enum class BannerType {
        Standard,
        Adaptive,
        CollapsibleTop,
        CollapsibleBottom
    }

    private var adView: BaseAdView? = null
    var config: Config = Config().apply {
        this.defaultAdUnitId = id
        this.defaultBannerType = BannerType.Adaptive
        this.defaultRefreshRateSec = 10
        this.defaultCBFetchIntervalSec = 20
        this.loadAdAfterInit = AdmobUtils.isShowAds
    }

    init {
        initViewAndConfig()
        if (config.loadAdAfterInit) {
            loadAd()
        }
    }

    private fun initViewAndConfig() {
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        adContainer.addView(tagView, 0)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        var adUnitId = config.defaultAdUnitId
        var bannerType = config.defaultBannerType
        var cbFetchIntervalSec = config.defaultCBFetchIntervalSec
        var refreshRateSec: Int? = config.defaultRefreshRateSec

        if (AdmobUtils.isTesting) {
            adUnitId = activity.getString(R.string.test_ads_admob_banner_collapsible_id)
        }
        bannerType = when (bannerConfig?.type) {
            TYPE_STANDARD -> BannerType.Standard
            TYPE_ADAPTIVE -> BannerType.Adaptive
            TYPE_COLLAPSIBLE_TOP -> BannerType.CollapsibleTop
            TYPE_COLLAPSIBLE_BOTTOM -> BannerType.CollapsibleBottom
            else -> bannerType
        }
        refreshRateSec = bannerConfig?.refreshRateSec ?: refreshRateSec
        cbFetchIntervalSec = bannerConfig?.cbFetchIntervalSec ?: cbFetchIntervalSec

        log("\n adUnitId = $adUnitId \n bannerType = $bannerType \n refreshRateSec = $refreshRateSec \n cbFetchIntervalSec = $cbFetchIntervalSec")

        adView = BaseAdView.Factory.getAdView(
                                           activity,
            adUnitId,
            bannerType,
            refreshRateSec,
            cbFetchIntervalSec,
            bannerRemoteConfig
        )
        adContainer.addView(adView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun loadAd() {
        adView?.loadAd()
    }

    data class BannerConfig(
        @SerializedName("ad_unit_id")
        val adUnitId: String?,
        @SerializedName("type")
        val type: String?,
        @SerializedName("refresh_rate_sec")
        val refreshRateSec: Int?,
        @SerializedName("cb_fetch_interval_sec")
        val cbFetchIntervalSec: Int?
    ) {
        companion object {
            const val TYPE_STANDARD = "standard"
            const val TYPE_ADAPTIVE = "adaptive"
            const val TYPE_COLLAPSIBLE_TOP = "collapsible_top"
            const val TYPE_COLLAPSIBLE_BOTTOM = "collapsible_bottom"
        }
    }
}