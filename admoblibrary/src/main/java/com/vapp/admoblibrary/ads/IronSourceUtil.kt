package com.vapp.admoblibrary.ads

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.integration.IntegrationHelper
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.vapp.admoblibrary.utils.SweetAlert.SweetAlertDialog


object IronSourceUtil : LifecycleObserver {
    fun initIronSource(activity: Activity, appKey: String) {
        IronSource.init(activity, appKey)
    }
    fun validateIntegration(activity:Activity){
        IntegrationHelper.validateIntegration(activity);
    }
    val TAG: String = "IronSourceUtil"
    fun showInterstitialAdsWithCallback(
        activity: AppCompatActivity,
        adPlacementId: String,
        showLoadingDialog: Boolean,
        callback: AdCallback
    ) {
        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
        dialog.setTitleText("Loading ads. Please wait...")
        dialog.setCancelable(false)
        val lifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                IronSource.onResume(activity)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                IronSource.onPause(activity)
            }
        }
        activity.lifecycle.addObserver(lifecycleObserver)
//        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        val mInterstitialListener = object : InterstitialListener {
            override fun onInterstitialAdReady() {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                if(activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){
                    Log.d(TAG, "onInterstitialAdReady")
                    IronSource.showInterstitial(adPlacementId)
                }
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError) {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                callback.onAdFail()
                if (p0 != null) {
                    Log.d(TAG, "onInterstitialAdLoadFailed "+p0.errorMessage)
                }
            }

            override fun onInterstitialAdOpened() {
                Log.d(TAG, "onInterstitialAdOpened")
            }

            override fun onInterstitialAdClosed() {
                callback.onAdClosed()
                Log.d(TAG, "onInterstitialAdClosed")
            }

            override fun onInterstitialAdShowSucceeded() {
                Log.d(TAG, "onInterstitialAdShowSucceeded")
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError) {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                Log.d(TAG, "onInterstitialAdShowFailed " + p0.errorMessage)
//                callback.onAdFail()
            }

            override fun onInterstitialAdClicked() {
                Log.d(TAG, "onInterstitialAdClicked")
            }
        }
            Log.d(TAG, "isInterstitialNotReady")
            IronSource.loadInterstitial()
        if(showLoadingDialog){
            dialog.show()
        }
        IronSource.setInterstitialListener(mInterstitialListener);
    }

    fun showBanner(activity: Activity, bannerContainer: ViewGroup, adPlacementId: String) {
        val banner = IronSource.createBanner(activity, ISBannerSize.SMART)
        bannerContainer.addView(banner)
        IronSource.loadBanner(banner, adPlacementId)
    }
}