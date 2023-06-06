package com.vapp.admobexample;

import android.content.Context;

import androidx.multidex.MultiDex;

import com.vapp.admobexample.utilsdemp.AdsManager;
import com.vapp.admoblibrary.AdsMultiDexApplication;
import com.vapp.admoblibrary.ads.AdmodUtils;
import com.vapp.admoblibrary.ads.AppOpenManager;
import com.vapp.admoblibrary.iap.PurchaseUtils;

public class MyApplication extends AdsMultiDexApplication {
    boolean isShowAds = false;
    boolean isShowAdsResume = true;

    @Override
    public void onCreate() {
        super.onCreate();

        PurchaseUtils.getInstance().initBilling(this, getString(R.string.play_console_license));
//        if (PurchaseUtils.getInstance().isSubscriptiond(getString(R.string.premium))) {
//            isShowAds = false;
//        }else {
//            isShowAds = true;
//        }
        PurchaseUtils.getInstance().isPurchased(getString(R.string.product_id));

        AdmodUtils.initAdmob(this, 10000, true, isShowAds);
        if (isShowAdsResume) {
            AppOpenManager.getInstance().init(this, getString(R.string.test_ads_admob_app_open));
//            AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        }
//        ;

//        AdsManager.INSTANCE.loadInter2(getApplicationContext());
//        AdsManager.INSTANCE.loadInter3(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
