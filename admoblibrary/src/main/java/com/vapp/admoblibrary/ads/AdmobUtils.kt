package com.vapp.admoblibrary.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.vapp.admoblibrary.AdsInterCallBack
import com.vapp.admoblibrary.R
import com.vapp.admoblibrary.ads.admobnative.NativeFunc.Companion.populateNativeAdView
import com.vapp.admoblibrary.ads.admobnative.enumclass.CollapsibleBanner
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleENative
import com.vapp.admoblibrary.ads.model.BannerAdCallback
import com.vapp.admoblibrary.ads.model.BannerHolder
import com.vapp.admoblibrary.ads.model.InterHolder
import com.vapp.admoblibrary.ads.model.NativeHolder
import com.vapp.admoblibrary.ads.model.RewardedInterstitialHolder
import com.vapp.admoblibrary.ads.nativefullscreen.NativeFullScreenCallBack
import com.vapp.admoblibrary.ads.remote.BannerAdView
import com.vapp.admoblibrary.ads.remote.BannerPlugin
import com.vapp.admoblibrary.ads.remote.BannerRemoteConfig
import com.vapp.admoblibrary.utils.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date
import java.util.Locale

object AdmobUtils {
    //Dialog loading
    @JvmField
    var dialog: SweetAlertDialog? = null
    var dialogFullScreen: Dialog? = null
    // Biến check lần cuối hiển thị quảng cáo
    var lastTimeShowInterstitial: Long = 0
    // Timeout init admob
    var timeOut = 0
    //Check quảng cáo đang show hay không
    @JvmField
    var isAdShowing = false
    var isClick = false
    //Ẩn hiện quảng cáo
    @JvmField
    var isShowAds = true
    //Dùng ID Test để hiển thị quảng cáo
    @JvmField
    var isTesting = false
    //List device test
    var testDevices: MutableList<String> = ArrayList()
    var deviceId = ""
    //Reward Ads
    @JvmField
    var mRewardedAd: RewardedAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var shimmerFrameLayout: ShimmerFrameLayout?=null
    //id thật
    var idIntersitialReal: String? = null
    var interIsShowingWithNative = false
    var interIsShowingWithBanner = false
    //Hàm Khởi tạo admob
    @JvmStatic
    fun initAdmob(context: Context?, timeout: Int, isDebug: Boolean, isEnableAds: Boolean) {
        timeOut = timeout
        if (timeOut < 5000 && timeout != 0) {
            Toast.makeText(context, "Nên để limit time ~10000", Toast.LENGTH_LONG).show()
        }
        timeOut = if (timeout > 0) {
            timeout
        } else {
            10000
        }
        isTesting = isDebug
        isShowAds = isEnableAds
        MobileAds.initialize(context!!) { initializationStatus: InitializationStatus? -> }
        initListIdTest()
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDevices)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        initAdRequest(timeout)
    }

    var adRequest: AdRequest? = null
    // get AdRequest
    @JvmStatic
    fun initAdRequest(timeOut: Int) {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }
    fun initListIdTest() {
        testDevices.add("D4A597237D12FDEC52BE6B2F15508BB")
    }

    //check open network
    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    interface BannerCallBack {
        fun onClickAds()
        fun onLoad()
        fun onFailed(message : String)
        fun onPaid(adValue: AdValue?, mAdView: AdView?)
    }

    @JvmStatic
    fun loadAdBanner(
        activity: Activity,
        bannerId: String?,
        viewGroup: ViewGroup,
        bannerAdCallback: BannerCallBack
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            bannerAdCallback.onFailed("None Show")
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)

        try {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView, 0)
            viewGroup.addView(mAdView, 1)
        }catch (_: Exception){

        }
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        mAdView.onPaidEventListener =
            OnPaidEventListener { adValue -> bannerAdCallback.onPaid(adValue, mAdView) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onLoad()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onFailed(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                bannerAdCallback.onClickAds()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        if (adRequest != null) {
            mAdView.loadAd(adRequest!!)
        }
        Log.e(" Admod", "loadAdBanner")
    }
    interface BannerCollapsibleAdCallback {
        fun onClickAds()
        fun onBannerAdLoaded(adSize: AdSize)
        fun onAdFail(message : String)
        fun onAdPaid(adValue: AdValue, mAdView: AdView)
    }
    var mAdView :AdView? = null
    @JvmStatic
    fun loadAdBannerCollapsibleReload(
        activity: Activity,
        banner: BannerHolder,
        collapsibleBannerSize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerCollapsibleAdCallback
    ) {
        var bannerId = banner.ads
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        banner.mAdView?.destroy()
        banner.mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_collapsible_id)
        }
        banner.mAdView?.adUnitId = bannerId
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView, 0)
            viewGroup.addView(banner.mAdView, 1)
        }catch (_: Exception){

        }
        val adSize = getAdSize(activity)
        banner.mAdView?.setAdSize(adSize)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        banner.mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                banner.mAdView?.onPaidEventListener =
                    OnPaidEventListener { adValue -> callback.onAdPaid(adValue, banner.mAdView!!) }
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onBannerAdLoaded(adSize)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onAdFail(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                callback.onClickAds()
            }

            override fun onAdClosed() {

            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannerSize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        banner.mAdView?.loadAd(adRequest2)
        Log.e(" Admod", "loadAdBanner")
    }

    @JvmStatic
    fun loadAdBannerCollapsible(
        activity: Activity,
        bannerId: String?,
        collapsibleBannerSize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerCollapsibleAdCallback
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_collapsible_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        val tagView = activity.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView, 0)
            viewGroup.addView(mAdView, 1)
        }catch (_: Exception){

        }
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        mAdView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView.onPaidEventListener =
                    OnPaidEventListener { adValue -> callback.onAdPaid(adValue, mAdView) }
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onBannerAdLoaded(adSize)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onAdFail(adError.message)
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                callback.onClickAds()
            }

            override fun onAdClosed() {

            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannerSize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        mAdView.loadAd(adRequest2)
        Log.e(" Admod", "loadAdBanner")
    }

    @JvmStatic
    fun loadAndShowBannerCollapsibleWithConfig(
        activity: Activity,
        id: String,refreshRateSec : Int,view: ViewGroup,
        bannerAdCallback: BannerCollapsibleAdCallback
    ) {
        var bannerPlugin: BannerPlugin? = null
        val bannerConfig = BannerPlugin.BannerConfig(id,"collapsible_bottom",refreshRateSec,0)
        bannerPlugin = bannerConfig.adUnitId?.let {
            BannerPlugin(
                activity, view, it, bannerConfig, object : BannerRemoteConfig {
                    override fun onBannerAdLoaded(adSize: AdSize?) {
                        adSize?.let { it1 -> bannerAdCallback.onBannerAdLoaded(it1) }
                    }

                    override fun onAdFail() {
                        Log.d("===Banner", "Banner2")
                        bannerAdCallback.onAdFail("Banner Failed")
                    }

                    override fun onAdPaid(adValue: AdValue, mAdView: AdView) {
                        bannerAdCallback.onAdPaid(adValue,mAdView)
                    }
                })
        }
    }

    private fun getAdSize(context: Activity): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = context.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    //Load native 1 in here
    @JvmStatic
    fun loadAndGetNativeAds(
        context: Context,
        nativeHolder: NativeHolder,
        adCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(context)) {
            adCallback.onAdFail("No internet")
            return
        }
        //If native is loaded return
        if (nativeHolder.nativeAd != null) {
            Log.d("===AdsLoadsNative", "Native not null")
            return
        }
        if (isTesting) {
            nativeHolder.ads = context.getString(R.string.test_ads_admob_native_id)
        }
        nativeHolder.isLoad = true
        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).build()
        val adLoader: AdLoader = AdLoader.Builder(context, nativeHolder.ads)
            .forNativeAd { nativeAd ->
                nativeHolder.nativeAd = nativeAd
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? -> adValue?.let {
                    adCallback.onAdPaid(
                        it,nativeHolder.ads)
                } }
                adCallback.onLoadedAndGetNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    nativeHolder.nativeAd = null
                    nativeHolder.isLoad = false
                    nativeHolder.native_mutable.value = null
                    adCallback.onAdFail(adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
    }

    //Load native 2 in here
    interface AdsNativeCallBackAdmod {
        fun NativeLoaded()
        fun NativeFailed(massage : String)
        fun onPaidNative(adValue : AdValue, adUnitAds : String)
    }

    @JvmStatic
    fun showNativeAdsWithLayout(
        activity: Activity,
        nativeHolder: NativeHolder,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        callback: AdsNativeCallBackAdmod
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }
        viewGroup.removeAllViews()
        if (!nativeHolder.isLoad) {
            if (nativeHolder.nativeAd != null) {
                val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeHolder.nativeAd!!, adView, size)
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                callback.NativeLoaded()
            } else {
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                callback.NativeFailed("None Show")
            }
        } else {
            val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
                activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
            } else {
                activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
            }
            viewGroup.addView(tagView, 0)
            if (shimmerFrameLayout == null) shimmerFrameLayout =
                tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout?.startShimmer()
            nativeHolder.native_mutable.observe((activity as LifecycleOwner)) { nativeAd: NativeAd? ->
                if (nativeAd != null) {
                    nativeAd.setOnPaidEventListener {
                        callback.onPaidNative(it,nativeHolder.ads)
                    }
                    val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                    populateNativeAdView(nativeAd, adView, size)
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    viewGroup.removeAllViews()
                    viewGroup.addView(adView)
                    callback.NativeLoaded()
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                } else {
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    callback.NativeFailed("None Show")
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                }
            }
        }
    }

    // ads native
    @JvmStatic
    fun loadAndShowNativeAdsWithLayout(
        activity: Activity,
        s: String?,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        adCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        var s = s
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val videoOptions =
            VideoOptions.Builder().setStartMuted(false).build()
        val adLoader: AdLoader = AdLoader.Builder(activity, s!!)
            .forNativeAd { nativeAd ->
                nativeAd.setOnPaidEventListener { adValue: AdValue -> adCallback.onAdPaid(adValue,s) }
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, size)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    adCallback.onAdFail(adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    interface NativeAdCallbackNew{
        fun onLoadedAndGetNativeAd(ad: NativeAd?)
        fun onNativeAdLoaded()
        fun onAdFail(error: String)
        fun onAdPaid(adValue: AdValue?, adUnitAds: String?)
        fun onClickAds()

    }
    @JvmStatic
    fun loadAndShowNativeAdsWithLayoutAds(
        activity: Activity,
        nativeHolder: NativeHolder,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        adCallback: NativeAdCallbackNew
    ) {
        Log.d("===Native", "Native1")
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
//        val videoOptions =
//            VideoOptions.Builder().setStartMuted(false).build()
        viewGroup.removeAllViews()
        var s = nativeHolder.ads
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader = AdLoader.Builder(activity, s)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, size)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                nativeAd.setOnPaidEventListener { adValue: AdValue ->
                    adCallback.onAdPaid(adValue,s)
                }
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    nativeHolder.isLoad = false
                    adCallback.onAdFail(adError.message)
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    adCallback.onClickAds()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    //Load Inter in here
    @JvmStatic
    fun loadAndGetAdInterstitial(
        activity: Context,
        interHolder: InterHolder,
        adLoadCallback: AdCallBackInterLoad
    ) {
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail("None Show")
            return
        }
        if (interHolder.inter != null) {
            Log.d("===AdsInter", "inter not null")
            return
        }
        interHolder.check = true
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isTesting) {
            interHolder.ads = activity.getString(R.string.test_ads_admob_inter_id)
        }
        idIntersitialReal = interHolder.ads
        InterstitialAd.load(
            activity,
            idIntersitialReal!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (isClick) {
                        interHolder.mutable.value = interstitialAd
                    }
                    interHolder.inter = interstitialAd
                    interHolder.check = false
                    adLoadCallback.onAdLoaded(interstitialAd, false)
                    Log.i("adLog", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdShowing = false
                    if (mInterstitialAd != null) {
                        mInterstitialAd = null
                    }
                    interHolder.check = false
                    if (isClick) {
                        interHolder.mutable.value = null
                    }
                    adLoadCallback.onAdFail(loadAdError.message)
                }
            })
    }

    //Load Inter 2 in here if inter 1 false

    //Show Inter in here
    @JvmStatic
    fun showAdInterstitialWithCallbackNotLoadNew(
        activity: Activity,
        interHolder: InterHolder,
        timeout: Long,
        adCallback: AdsInterCallBack?,
        enableLoadingDialog: Boolean
    ) {
        isClick = true
        //Check internet
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            adCallback?.onAdFail("No internet")
            return
        }
        adCallback?.onAdLoaded()
        val handler = Handler(Looper.getMainLooper())
        //Check timeout show inter
        val runnable = Runnable {
            if (interHolder.check) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                isClick = false
                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                isAdShowing = false
                dismissAdDialog()
                adCallback?.onAdFail("timeout")
            }
        }
        handler.postDelayed(runnable, timeout)
        //Inter is Loading...
        if (interHolder.check) {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            interHolder.mutable.observe((activity as LifecycleOwner)) { aBoolean: InterstitialAd? ->
                if (aBoolean != null) {
                    interHolder.mutable.removeObservers((activity as LifecycleOwner))
                    isClick = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d("===DelayLoad", "delay")
                        aBoolean.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                //Set inter = null
                                interHolder.inter = null
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                adCallback?.onEventClickAdClosed()
                                dismissAdDialog()
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                isAdShowing = false
                                //Set inter = null
                                interHolder.inter = null
                                dismissAdDialog()
                                Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                handler.removeCallbacksAndMessages(null)
                                adCallback?.onAdFail(adError.message)
                            }

                            override fun onAdShowedFullScreenContent() {
                                handler.removeCallbacksAndMessages(null)
                                isAdShowing = true
                                adCallback?.onAdShowed()
                                try {
                                    aBoolean.setOnPaidEventListener {
                                            adValue -> adCallback?.onPaid(adValue,interHolder.inter?.adUnitId)
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                        showInterstitialAdNew(activity, aBoolean, adCallback)
                    }, 400)
                }else{
                    interHolder.check = true
                }
            }
            return
        }
        //Load inter done
        if (interHolder.inter == null) {
            if (adCallback != null) {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                adCallback.onAdFail("inter null")
                handler.removeCallbacksAndMessages(null)
            }
        } else {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                interHolder.inter?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            isClick = false
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            interHolder.inter = null
                            adCallback?.onEventClickAdClosed()
                            dismissAdDialog()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            handler.removeCallbacksAndMessages(null)
                            isClick = false
                            interHolder.inter = null
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            isAdShowing = false
                            dismissAdDialog()
                            adCallback?.onAdFail(adError.message)
                            Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                            Log.e("Admodfail", "errorCodeAds" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handler.removeCallbacksAndMessages(null)
                            isAdShowing = true
                            adCallback?.onAdShowed()
                        }
                    }
                showInterstitialAdNew(activity, interHolder.inter, adCallback)
            }, 400)
        }
    }
    @JvmStatic
    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdsInterCallBack?
    ) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            isAdShowing = true
            Handler(Looper.getMainLooper()).postDelayed({
                callback?.onStartAction()
                mInterstitialAd.setOnPaidEventListener { adValue -> callback?.onPaid(adValue,mInterstitialAd.adUnitId) }
                mInterstitialAd.show(activity)
            },400)
        } else {
            isAdShowing = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            dismissAdDialog()
            callback?.onAdFail("onResume")
        }
    }

    @JvmStatic
    fun dismissAdDialog() {
        try {
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        }catch (_: Exception){

        }
    }
    @JvmStatic
    fun loadAndShowAdRewardWithCallback(
        activity: Activity,
        admobId: String?,
        adCallback2: RewardAdCallback,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback2.onAdClosed()
            return
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_reward_id)
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }
        isAdShowing = false
        if (AppOpenManager.getInstance().isInitialized) {
            AppOpenManager.getInstance().isAppResumeEnabled = false
        }
        RewardedAd.load(activity, admobId!!,
            adRequest!!, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    mRewardedAd = null
                    adCallback2.onAdFail(loadAdError.message)
                    dismissAdDialog()
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.message)
                    Log.e("Admodfail", "errorCodeAds" + loadAdError.cause)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    if (mRewardedAd != null) {
                        mRewardedAd?.setOnPaidEventListener { adCallback2.onPaid(it, mRewardedAd?.adUnitId) }
                        mRewardedAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdShowedFullScreenContent() {
                                    isAdShowing = true
                                    adCallback2.onAdShowed()
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = false
                                    }
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when ad fails to show.
                                    if (adError.code != 1) {
                                        isAdShowing = false
                                        adCallback2.onAdFail(adError.message)
                                        mRewardedAd = null
                                        dismissAdDialog()
                                    }
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    mRewardedAd = null
                                    isAdShowing = false
                                    adCallback2.onAdClosed()
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                }
                            }
                        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = false
                            }
                            mRewardedAd?.show(activity) { adCallback2.onEarned() }
                            isAdShowing = true
                        } else {
                            mRewardedAd = null
                            dismissAdDialog()
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    } else {
                        isAdShowing = false
                        adCallback2.onAdFail("None Show")
                        dismissAdDialog()
                        if (AppOpenManager.getInstance().isInitialized) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true
                        }
                    }
                }
            })
    }

    //Interstitial Reward ads
    @JvmField
    var mInterstitialRewardAd: RewardedInterstitialAd? = null
    @JvmStatic
    fun loadAdInterstitialReward(
        activity: Context,
        mInterstitialRewardAd: RewardedInterstitialHolder,
        adLoadCallback: AdLoadCallback
    ) {
        var admobId = mInterstitialRewardAd.ads
        if (!isShowAds || !isNetworkConnected(activity)) {
            return
        }
        if (mInterstitialRewardAd.inter != null) {
            Log.d("===AdsInter", "mInterstitialRewardAd not null")
            return
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        mInterstitialRewardAd.isLoading = true
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_reward_id)
        }
        RewardedInterstitialAd.load(
            activity,
            admobId,
            adRequest!!,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialRewardAd: RewardedInterstitialAd) {
                    mInterstitialRewardAd.inter = interstitialRewardAd
                    mInterstitialRewardAd.mutable.value = interstitialRewardAd
                    mInterstitialRewardAd.isLoading = false
                    adLoadCallback.onAdLoaded()
                    Log.i("adLog", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialRewardAd.inter = null
                    mInterstitialRewardAd.isLoading = false
                    mInterstitialRewardAd.mutable.value = null
                    adLoadCallback.onAdFail(loadAdError.message)
                }
            })
    }
    @JvmStatic
    fun showAdInterstitialRewardWithCallback(
        activity: Activity,mInterstitialRewardAd : RewardedInterstitialHolder,
        adCallback: RewardAdCallback
    ) {
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            adCallback.onAdFail("No internet or isShowAds = false")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.Main){
                if (mInterstitialRewardAd.isLoading){
                    dialogLoading(activity)
                    delay(800)

                    mInterstitialRewardAd.mutable.observe(activity as LifecycleOwner){reward: RewardedInterstitialAd? ->
                        reward?.let {
                            mInterstitialRewardAd.mutable.removeObservers((activity as LifecycleOwner))
                            it.setOnPaidEventListener { value ->
                                adCallback.onPaid(value,mInterstitialRewardAd.inter?.adUnitId)
                            }
                            mInterstitialRewardAd.inter?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    mInterstitialRewardAd.inter = null
                                    mInterstitialRewardAd.mutable.removeObservers((activity as LifecycleOwner))
                                    mInterstitialRewardAd.mutable.value = null
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                    isAdShowing = false
                                    dismissAdDialog()
                                    adCallback.onAdClosed()
                                    Log.d("TAG", "The ad was dismissed.")
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    mInterstitialRewardAd.inter = null
                                    mInterstitialRewardAd.mutable.removeObservers((activity as LifecycleOwner))
                                    mInterstitialRewardAd.mutable.value = null
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                    isAdShowing = false
                                    dismissAdDialog()
                                    adCallback.onAdFail(adError.message)
                                    Log.d("TAG", "The ad failed to show.")
                                }

                                override fun onAdShowedFullScreenContent() {
                                    isAdShowing = true
                                    adCallback.onAdShowed()
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        dismissAdDialog()
                                    },800)
                                    Log.d("TAG", "The ad was shown.")
                                }
                            }
                            it.show(activity) { adCallback.onEarned() }
                        }
                    }
                }else {
                    if (mInterstitialRewardAd.inter != null) {
                        dialogLoading(activity)
                        delay(800)

                        mInterstitialRewardAd.inter?.setOnPaidEventListener {
                            adCallback.onPaid(it,mInterstitialRewardAd.inter?.adUnitId)
                        }
                        mInterstitialRewardAd.inter?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                mInterstitialRewardAd.inter = null
                                mInterstitialRewardAd.mutable.removeObservers((activity as LifecycleOwner))
                                mInterstitialRewardAd.mutable.value = null
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isAdShowing = false
                                dismissAdDialog()
                                adCallback.onAdClosed()
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                mInterstitialRewardAd.inter = null
                                mInterstitialRewardAd.mutable.removeObservers((activity as LifecycleOwner))
                                mInterstitialRewardAd.mutable.value = null
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isAdShowing = false
                                dismissAdDialog()
                                adCallback.onAdFail(adError.message)
                                Log.d("TAG", "The ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                isAdShowing = true
                                adCallback.onAdShowed()
                                Log.d("TAG", "The ad was shown.")
                            }
                        }
                        mInterstitialRewardAd.inter?.show(activity) { adCallback.onEarned() }

                    } else {
                        isAdShowing = false
                        adCallback.onAdFail("None Show")
                        dismissAdDialog()
                        if (AppOpenManager.getInstance().isInitialized) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true
                        }
                        Log.d("TAG", "Ad did not load.")
                    }
                }
            }
        }
    }


    @JvmStatic
    fun loadAndShowAdInterstitial(
        activity: AppCompatActivity,
        admobId: InterHolder,
        adCallback: AdsInterCallBack,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId.ads
        mInterstitialAd = null
        isAdShowing = false
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            return
        }
        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }
        val dialogFullScreen = Dialog(activity)
        dialogFullScreen.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen.setCancelable(false)
        dialogFullScreen.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        if (enableLoadingDialog) {
            try {
                if (!activity.isFinishing && !dialogFullScreen.isShowing) {
                    dialogFullScreen.show()
                }
            } catch (ignored: Exception) {
            }
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id)
        } else {
            checkIdTest(activity, admobId)
        }
        InterstitialAd.load(
            activity,
            admobId,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    adCallback.onAdLoaded()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mInterstitialAd = interstitialAd
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.onPaidEventListener =
                                OnPaidEventListener { adValue: AdValue? -> adCallback.onPaid(adValue,mInterstitialAd?.adUnitId) }
                            mInterstitialAd!!.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        adCallback.onAdFail(adError.message)
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        isAdShowing = false
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        dismissAdDialog()
                                        dialogFullScreen.dismiss()
                                        Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                        Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        lastTimeShowInterstitial = Date().time
                                        adCallback.onEventClickAdClosed()
                                        dialogFullScreen.dismiss()
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        super.onAdShowedFullScreenContent()
                                        Log.e("===onAdShowed", "onAdShowedFullScreenContent")
                                        adCallback.onAdShowed()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            dismissAdDialog()
                                            dialogFullScreen.dismiss()
                                        },800)
                                    }
                                }
                            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
                                adCallback.onStartAction()
                                mInterstitialAd!!.show(activity)
                                isAdShowing = true
                            } else {
                                mInterstitialAd = null
                                dismissAdDialog()
                                dialogFullScreen.dismiss()
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                adCallback.onAdFail("Interstitial can't show in background")
                            }
                        } else {
                            dismissAdDialog()
                            dialogFullScreen.dismiss()
                            adCallback.onAdFail("mInterstitialAd null")
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    }, 800)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    mInterstitialAd = null
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    adCallback.onAdFail(loadAdError.message)
                    dismissAdDialog()
                }
            })
    }

    //Update New Lib
    private fun checkIdTest(activity: Activity, admobId: String?) {
//        if (admobId.equals(activity.getString(R.string.test_ads_admob_inter_id)) && !BuildConfig.DEBUG) {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
//            Utils.getInstance().showDialogTitle(activity, "Warning", "Build bản release nhưng đang để id test ads", "Đã biết", DialogType.WARNING_TYPE, false, "", new DialogCallback() {
//                @Override
//                public void onClosed() {
//                }
//
//                @Override
//                public void cancel() {
//                }
//            });
//        }
    }

    private val currentTime: Long
        private get() = System.currentTimeMillis()

    fun getDeviceID(context: Context): String {
        val android_id = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return md5(android_id).uppercase(Locale.getDefault())
    }



    fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }


    fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        try {
            if (!context.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (ignored: Exception) {
        }

    }
    fun loadAndShowNativeFullScreen(activity: Activity,id : String, viewGroup: ViewGroup,layout: Int,mediaAspectRatio : Int, listener: NativeFullScreenCallBack){
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        var adMobId : String = id
        if (isTesting) {
            adMobId = activity.getString(R.string.test_ads_admob_native_full_screen_id)
        }
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.layoutnative_loading_fullscreen, null, false)
        viewGroup.addView(tagView,0)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
        val builder = AdLoader.Builder(activity,adMobId)
        val videoOptions = VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(false).build()
        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(mediaAspectRatio)
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        builder.forNativeAd { nativeAd ->
            nativeAd.setOnPaidEventListener { adValue: AdValue? -> adValue?.let { listener.onPaidNative(adValue,id) } }
            populateNativeAdView(nativeAd,adView.findViewById(R.id.native_ad_view))
            viewGroup.removeAllViews()
            shimmerFrameLayout?.stopShimmer()
            viewGroup.addView(adView)
        }
        builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("===AdmobFailed", loadAdError.toString())
                shimmerFrameLayout?.stopShimmer()
                listener.onLoadFailed()
            }
        })
        if (adRequest != null) {
            builder.build().loadAd(adRequest!!)
        }
    }

    @JvmStatic
    fun loadAndGetNativeFullScreenAds(
        context: Context,
        nativeHolder: NativeHolder,mediaAspectRatio : Int,
        adCallback: NativeAdCallbackNew
    ) {
        if (!isShowAds || !isNetworkConnected(context)) {
            adCallback.onAdFail("No internet")
            return
        }
        //If native is loaded return
        if (nativeHolder.nativeAd != null) {
            Log.d("===AdsLoadsNative", "Native not null")
            return
        }
        if (isTesting) {
            nativeHolder.ads = context.getString(R.string.test_ads_admob_native_full_screen_id)
        }
        nativeHolder.isLoad = true
        val videoOptions = VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(mediaAspectRatio)
            .setVideoOptions(videoOptions)
            .build()
        val adLoader = AdLoader.Builder(context, nativeHolder.ads)
        adLoader.withNativeAdOptions(adOptions)
        adLoader.forNativeAd { nativeAd ->
            nativeHolder.nativeAd = nativeAd
            nativeHolder.isLoad = false
            nativeHolder.native_mutable.value = nativeAd
            nativeAd.setOnPaidEventListener { adValue: AdValue? -> adValue?.let {
                adCallback.onAdPaid(
                    it,nativeHolder.ads)
            } }
            adCallback.onLoadedAndGetNativeAd(nativeAd)
        }
        adLoader.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                Log.e("Admodfail", "errorCodeAds" + adError.cause)
                nativeHolder.nativeAd = null
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = null
                adCallback.onAdFail("errorId2_"+adError.message)
            }

            override fun onAdClicked() {
                super.onAdClicked()
                adCallback.onClickAds()
            }
        })
        if (adRequest != null) {
            adLoader.build().loadAd(adRequest!!)
        }
    }

    @JvmStatic
    fun showNativeFullScreenAdsWithLayout(
        activity: Activity,
        nativeHolder: NativeHolder,
        viewGroup: ViewGroup,
        layout: Int,
        callback: AdsNativeCallBackAdmod
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }
        viewGroup.removeAllViews()
        if (!nativeHolder.isLoad) {
            if (nativeHolder.nativeAd != null) {
                val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeHolder.nativeAd!!,adView.findViewById(R.id.native_ad_view))
                shimmerFrameLayout?.stopShimmer()
                viewGroup.addView(adView)
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                callback.NativeLoaded()
            } else {
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                callback.NativeFailed("None Show")
            }
        } else {
            val tagView = activity.layoutInflater.inflate(R.layout.layoutnative_loading_fullscreen, null, false)
            viewGroup.addView(tagView, 0)
            if (shimmerFrameLayout == null) shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout?.startShimmer()
            nativeHolder.native_mutable.observe((activity as LifecycleOwner)) { nativeAd: NativeAd? ->
                if (nativeAd != null) {
                    nativeAd.setOnPaidEventListener {
                        callback.onPaidNative(it,nativeHolder.ads)
                    }
                    val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                    populateNativeAdView(nativeHolder.nativeAd!!,adView.findViewById(R.id.native_ad_view))
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    viewGroup.removeAllViews()
                    viewGroup.addView(adView)
                    callback.NativeLoaded()
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                } else {
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    callback.NativeFailed("None Show")
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                }
            }
        }
    }
}