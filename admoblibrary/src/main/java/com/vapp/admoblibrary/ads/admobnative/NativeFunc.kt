package com.vapp.admoblibrary.ads.admobnative

import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.vapp.admoblibrary.R
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleENative

class NativeFunc {

   companion object {
        fun populateNativeAdView(
            nativeAd: NativeAd,
            adView: NativeAdView,
            size: GoogleENative
        ) {
            if (nativeAd == null || adView == null || size == null) {
                return
            }

            adView.findViewById<MediaView>(R.id.ad_media)?.let {
                adView.mediaView = it
            }
            adView.findViewById<TextView>(R.id.ad_headline)?.let {
                adView.headlineView = it
            }
            adView.findViewById<TextView>(R.id.ad_body)?.let {
                adView.bodyView = it
            }
            adView.findViewById<Button>(R.id.ad_call_to_action)?.let {
                adView.callToActionView = it
            }
            adView.findViewById<ImageView>(R.id.ad_app_icon)?.let {
                adView.iconView = it
            }
            adView.findViewById<RatingBar>(R.id.ad_stars)?.let {
                adView.starRatingView = it
            }
            if (nativeAd.mediaContent != null) {
                if (size == GoogleENative.UNIFIED_MEDIUM) {
                    adView.mediaView?.let {
                        it.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE)
                        val mediaContent = nativeAd.mediaContent
                        if (mediaContent != null && mediaContent.hasVideoContent()) {
                            // Create a MediaView and set its media content.
                            val mediaView = MediaView(it.context)
                            mediaView.mediaContent = mediaContent
                            it.addView(mediaView)
                        }
                    }
                }
            }

            if (nativeAd.headline != null) {
                (adView.headlineView as TextView).text = nativeAd.headline
            }
            if (nativeAd.body == null) {
                adView.bodyView!!.visibility = View.INVISIBLE
            } else {
                adView.bodyView!!.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
            if (nativeAd.callToAction == null) {
                adView.callToActionView!!.visibility = View.INVISIBLE

            }else{
                adView.callToActionView!!.visibility = View.VISIBLE
                (adView.callToActionView as Button).text = nativeAd.callToAction
            }


            if (adView.iconView != null) {
                if (nativeAd.icon == null) {
                    adView.iconView!!.visibility = View.GONE
                } else {
                    (adView.iconView as ImageView).setImageDrawable(
                        nativeAd.icon!!.drawable
                    )
                    adView.iconView!!.visibility = View.VISIBLE
                }
            }

            if (nativeAd.starRating != null) {
                (adView.starRatingView as RatingBar).rating = 5f
            }

            adView.setNativeAd(nativeAd)

            val vc = nativeAd.mediaContent!!.videoController
            if (vc.hasVideoContent()) {
                vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                }
            }
        }


       fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
           val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
           // Set the media view.
           adView.mediaView = mediaView

           // Set other ad assets.
           adView.headlineView = adView.findViewById(R.id.ad_headline)
           adView.bodyView = adView.findViewById(R.id.ad_body)
           adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
           val imageView = adView.findViewById<ImageView>(R.id.ad_app_icon)
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               imageView.clipToOutline = true
           }
           adView.iconView = imageView

           // The headline and mediaContent are guaranteed to be in every NativeAd.
           (adView.headlineView as TextView?)!!.text = nativeAd.headline
           mediaView.mediaContent = nativeAd.mediaContent

           // These assets aren't guaranteed to be in every NativeAd, so it's important to
           // check before trying to display them.
           if (nativeAd.body == null) {
               adView.bodyView!!.visibility = View.INVISIBLE
           } else {
               adView.bodyView!!.visibility = View.VISIBLE
               (adView.bodyView as TextView?)!!.text = nativeAd.body
           }
           if (nativeAd.callToAction == null) {
               adView.callToActionView!!.visibility = View.INVISIBLE
           } else {
               adView.callToActionView!!.visibility = View.VISIBLE
               (adView.callToActionView as Button?)!!.text = nativeAd.callToAction
           }
           if (nativeAd.icon == null) {
               adView.iconView!!.visibility = View.GONE
           } else {
               (adView.iconView as ImageView?)!!.setImageDrawable(
                   nativeAd.icon!!.drawable
               )
               adView.iconView!!.visibility = View.VISIBLE
           }

           // This method tells the Google Mobile Ads SDK that you have finished populating your
           // native ad view with this native ad.
           adView.setNativeAd(nativeAd)

           // Get the video controller for the ad. One will always be provided,
           // even if the ad doesn't have a video asset.
           val videoController = nativeAd.mediaContent!!.videoController

           // Updates the UI to say whether or not this ad has a video asset.
           if (videoController.hasVideoContent()) {

               // Create a new VideoLifecycleCallbacks object and pass it to the VideoController.
               // The VideoController will call methods on this object when events occur in the
               // video lifecycle.
               videoController.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
               }
           }
       }
    }
}