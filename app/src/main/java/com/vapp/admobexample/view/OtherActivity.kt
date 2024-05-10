package com.vapp.admobexample.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codemybrainsout.ratingdialog.RatingDialog
import com.vapp.admobexample.R
import com.vapp.admobexample.databinding.ActivityOtherBinding
import com.vapp.admobexample.utilsdemp.AdsManager
import com.vapp.admobexample.utilsdemp.AdsManager.nativeHolder
import com.vapp.admobexample.utilsdemp.AdsManager.showAdBannerCollapsible
import com.vapp.admoblibrary.utils.Utils

class OtherActivity : AppCompatActivity() {
    var binding: ActivityOtherBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setResult(5)
//                AdsManager.showAdBanner(this,"",, findViewById(R.id.banner),findViewById(R.id.line));
//        showAdBannerCollapsible(this, AdsManager.bannerHolder_other, findViewById(R.id.banner), findViewById(R.id.line))
//        AdsManager.loadAndShowNativeFullScreen(this,binding!!.nativeAds,nativeHolder)
        //        if (AdmodUtils.getInstance().dialog != null) {
//            if (AdmodUtils.getInstance().dialog.isShowing()) {
//                AdmodUtils.getInstance().dialog.dismiss();
//            }
//        }
        val ratingDialog = RatingDialog.Builder(this)
            .session(1)
            .date(1)
            .setNameApp(getString(R.string.app_name))
            .setIcon(R.mipmap.ic_launcher)
            .setEmail("vapp.helpcenter@gmail.com")
            .isShowButtonLater(true)
            .isClickLaterDismiss(true)
            .setTextButtonLater("Maybe Later")
            .setOnlickMaybeLate {
                Utils.getInstance().showMessenger(this@OtherActivity, "clicked Maybe Later")
            }
            .ignoreRated(true)
            .ratingButtonColor(R.color.purple_200)
            .build()

        //Cancel On Touch Outside
        ratingDialog.setCanceledOnTouchOutside(false)
        //show
//        ratingDialog.show();
    }

}