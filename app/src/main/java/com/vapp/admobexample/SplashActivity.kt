package com.vapp.admobexample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdValue
import com.vapp.admobexample.databinding.SplashBinding
import com.vapp.admobexample.utilsdemp.AdsManager.interholder
import com.vapp.admobexample.view.MainActivity
import com.vapp.admoblibrary.AdsInterCallBack
import com.vapp.admoblibrary.ads.AOAManager
import com.vapp.admoblibrary.ads.AdmobUtils.initAdmob
import com.vapp.admoblibrary.ads.AdmobUtils.loadAndShowAdInterstitial
import com.vapp.admoblibrary.ads.AppOpenManager
import com.vapp.admoblibrary.cmp.GoogleMobileAdsConsentManager
import com.vapp.admoblibrary.utils.Utils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.atomic.AtomicBoolean


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    var aoaManager: AOAManager? = null
    var isAOAFalse = false
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    val binding by lazy { SplashBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action === Intent.ACTION_MAIN) {
            finish()
            return
        }
        setupCMP()
        val android_id: String = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val deviceId: String = md5(android_id).toUpperCase()
        Log.i("device id=", deviceId)
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
    override fun onBackPressed() {
        super.onBackPressed()
        System.exit(0)
    }

    override fun onResume() {
        super.onResume()
        if (isAOAFalse) {
            Utils.getInstance().replaceActivity(this@SplashActivity, MainActivity::class.java)
        }
    }

    fun setupCMP(){
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this)
        googleMobileAdsConsentManager.gatherConsent { error ->
            error?.let {
                // Consent not obtained in current session.
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }

    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.get()) {
            //start action
            return
        }
        isMobileAdsInitializeCalled.set(true)
        initAdmob(this, 10000, isDebug = true, isEnableAds = true)
        AppOpenManager.getInstance().init(application, getString(R.string.test_ads_admob_app_open_new))
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
//        showInter()
        showAOA()
    }

    fun showInter(){
        loadAndShowAdInterstitial(this@SplashActivity, interholder, object : AdsInterCallBack {
            override fun onStartAction() {}
            override fun onEventClickAdClosed() {
                Utils.getInstance().addActivity(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            }

            override fun onAdShowed() {}
            override fun onAdLoaded() {}
            override fun onAdFail(error: String) {
                Utils.getInstance().addActivity(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            }

            override fun onPaid(adValue: AdValue, adUnitAds: String) {
                }
        }, false)
    }

    fun showAOA(){
        aoaManager = AOAManager(
            this,
            "",
            20000,
            object : AOAManager.AppOpenAdsListener {
                override fun onAdPaid(adValue: AdValue, s: String) {
                    Toast.makeText(
                        this@SplashActivity,
                        "\${adValue?.currencyCode}|\${adValue?.valueMicros}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAdsClose() {
                    Utils.getInstance()
                        .replaceActivity(this@SplashActivity, MainActivity::class.java)
                }

                override fun onAdsFailed(massage: String) {
                    isAOAFalse = true
                    Utils.getInstance()
                        .replaceActivity(this@SplashActivity, MainActivity::class.java)
                }
            })
        aoaManager!!.loadAndShowAoA()
    }
}
