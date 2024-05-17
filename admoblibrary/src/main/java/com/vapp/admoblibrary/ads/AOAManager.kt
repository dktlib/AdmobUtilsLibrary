package com.vapp.admoblibrary.ads

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.vapp.admoblibrary.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AOAManager(private val activity: Activity,val appOpen: String,val timeOut: Long, val isShow : Boolean, val appOpenAdsListener: AppOpenAdsListener) {
    private var appOpenAd: AppOpenAd? = null
    var isShowingAd = true
    var isLoading = true
    var dialogFullScreen: Dialog? = null
    var isStart = true
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    fun loadAoA() {
        Log.d("===Load","id1")
        var idAoa = appOpen
        if (AdmobUtils.isTesting){
            idAoa = activity.getString(R.string.test_ads_admob_app_open_new)
        }
        if (!AdmobUtils.isShowAds){
            appOpenAdsListener.onAdsFailed("isShowAds false")
            return
        }
        //Check timeout show inter
        val job = CoroutineScope(Dispatchers.Main).launch{
            delay(timeOut)
            if (isLoading && isStart) {
                isStart = false
                isLoading = false
                onAoaDestroyed()
                appOpenAdsListener.onAdsFailed("Time out")
                Log.d("====Timeout", "TimeOut")
            }
        }
        if (isAdAvailable) {
            job.cancel()
            appOpenAdsListener.onAdsFailed("isAdAvailable true")
            return
        } else {
            Log.d("====Timeout", "fetching... ")
            isShowingAd = false
            val request = adRequest
            AppOpenAd.load(activity, idAoa, request, object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    isLoading = false
                    super.onAdFailedToLoad(p0)
                    if (isStart){
                        isStart = false
                        appOpenAdsListener.onAdsFailed(p0.message)
                    }
                    job.cancel()
                    Log.d("====Timeout", "onAppOpenAdFailedToLoad: $p0")
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    appOpenAd = ad
                    appOpenAdsListener.onAdsLoaded()
                    job.cancel()
                    Log.d("====Timeout", "isAdAvailable = true")
                    if (!AppOpenManager.getInstance().isShowingAd && !isShowingAd && isShow){
                        showAdIfAvailable()
                    }
                }
            })
        }
    }

    fun showAdIfAvailable() {
        Log.d("====Timeout", "$isShowingAd - $isAdAvailable")
        if (!isShowingAd && isAdAvailable && isLoading) {
            isLoading = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = false
            }
            Log.d("====Timeout", "will show ad ")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        appOpenAd = null
                        isShowingAd = true
                        Log.d("====Timeout", "Dismiss... ")
                        if (isStart){
                            isStart = false
                            appOpenAdsListener.onAdsClose()
                        }
                        if (AppOpenManager.getInstance().isInitialized) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        isShowingAd = true
                        if (isStart){
                            isStart = false
                            appOpenAdsListener.onAdsFailed(p0.message)
                            Log.d("====Timeout", "Failed... $p0")
                        }
                        if (AppOpenManager.getInstance().isInitialized) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                this.fullScreenContentCallback = fullScreenContentCallback
                dialogFullScreen = Dialog(activity)
                dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                dialogFullScreen?.setCancelable(false)
                dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                dialogFullScreen?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
                img?.setAnimation(R.raw.gifloading)
                try {
                    if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                        dialogFullScreen?.show()
                    }
                } catch (ignored: Exception) {
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!AppOpenManager.getInstance().isShowingAd && !isShowingAd){
                        Log.d("===AOA","Show")
                        try {
                            val txt = dialogFullScreen?.findViewById<TextView>(R.id.txtLoading)
                            img?.visibility = View.INVISIBLE
                            txt?.visibility = View.INVISIBLE
                        } catch (ignored: Exception) {
                        }
                        setOnPaidEventListener { appOpenAdsListener.onAdPaid(it,adUnitId) }
                        show(activity)
                    }else{
                        appOpenAdsListener.onAdsFailed("AOA can't show")
                    }
                }, 800)
            }
        }else{
            appOpenAdsListener.onAdsFailed("AOA can't show in background!")
        }
    }

    fun onAoaDestroyed(){
        isShowingAd = true
        isLoading = false
        try {
            if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
            appOpenAd?.fullScreenContentCallback?.onAdDismissedFullScreenContent()
        } catch (ignored: Exception) {
        }
    }
    interface AppOpenAdsListener {
        fun onAdsClose()
        fun onAdsLoaded()
        fun onAdsFailed(message : String)
        fun onAdPaid(adValue: AdValue, adUnitAds : String)
    }

}