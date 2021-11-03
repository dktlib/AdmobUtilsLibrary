package com.vapp.admoblibrary.ads

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import com.vapp.admoblibrary.utils.SweetAlert.SweetAlertDialog


object IronSourceUtil {
    fun initIronSource(activity: Activity,appKey:String){
        IronSource.init(activity,appKey)
    }
    val TAG:String = "IronSourceUtil"
    fun showInterstitialAdsWithCallback(activity: AppCompatActivity, adPlacementId:String, showLoadingDialog:Boolean, callback:AdCallback){
        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        dialog.getProgressHelper().barColor = Color.parseColor("#A5DC86")
        dialog.setTitleText("Loading ads. Please wait...")
        dialog.setCancelable(false)
        val mInterstitialListener = object : InterstitialListener {
            override fun onInterstitialAdReady() {
                if(dialog.isShowing && activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){
                    dialog.dismiss()
                }
                IronSource.showInterstitial(adPlacementId)
                IronSource.removeInterstitialListener()
            }

            override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                callback.onAdFail()
            }

            override fun onInterstitialAdOpened() {
                Log.d(TAG,"onInterstitialAdOpened")
            }

            override fun onInterstitialAdClosed() {
                callback.onAdClosed()
            }

            override fun onInterstitialAdShowSucceeded() {
                Log.d(TAG,"onInterstitialAdShowSucceeded")
            }

            override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                callback.onAdFail()
            }

            override fun onInterstitialAdClicked() {
                Log.d(TAG,"onInterstitialAdClicked")
            }
        }
        if(IronSource.isInterstitialReady()){
            IronSource.showInterstitial(adPlacementId)
        }
        else{
            IronSource.loadInterstitial()
            if(dialog.isShowing && activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){
                dialog.show()
            }
        }
        IronSource.setInterstitialListener(mInterstitialListener);
    }
    fun showBanner(activity:Activity, bannerContainer: ViewGroup, adPlacementId:String, bannerSize:ISBannerSize){
        val banner = IronSource.createBanner(activity, bannerSize)
        bannerContainer.addView(banner)
        IronSource.loadBanner(banner,adPlacementId)
    }
}