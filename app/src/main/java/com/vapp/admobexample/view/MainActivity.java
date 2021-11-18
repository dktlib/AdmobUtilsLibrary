package com.vapp.admobexample.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.vapp.admobexample.R;
import com.vapp.admobexample.iap.IAPActivity;
import com.vapp.admobexample.utilsdemp.UtilsDemoActivity;
import com.vapp.admoblibrary.ads.AdCallback;
import com.vapp.admoblibrary.ads.AdmodUtils;
import com.vapp.admoblibrary.ads.NativeAdCallback;
import com.vapp.admoblibrary.ads.RewardAdCallback;
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleEBanner;
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleENative;
import com.vapp.admoblibrary.ads.model.AdUnitListModel;
import com.vapp.admoblibrary.rate.MaybeLaterCallback;
import com.vapp.admoblibrary.rate.RatingDialog;
import com.vapp.admoblibrary.utils.Utils;

public class MainActivity extends AppCompatActivity {
    Button btn_LoadInter;
    Button btn_ShowInter;
    Button btn_LoadAndShowInter;
    Button btn_LoadAndShowReward;
    Button btn_LoadNative, btn_LoadNativeGrid;
    Button btn_IAP, btn_Rate, btn_Utils;
    LinearLayout nativeAds;
    LinearLayout banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        finbyid();
        showDialogRate();

        // AdsConfigModel = Model call by API
//         Utils.getInstance().adUnitLists = adsConfigModel.getAdUnitList();

        //API data sample
        AdUnitListModel adUnitList = Utils.getInstance().getAdUnitByName("Name AdUnit", "Defaul Id Admob");
        //check Countries (BOOL)

//        if (Utils.getInstance().showAdForCountry(this,adUnitList)){
//            //show ads
//        }else{
//            //dont show ads
//        }

        btn_Utils.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UtilsDemoActivity.class));
            }
        });

        btn_LoadInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdmodUtils.getInstance().loadAdInterstitial(MainActivity.this, getString(R.string.test_ads_admob_inter_id), false);

            }
        });
        btn_ShowInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdmodUtils.getInstance().showAdInterstitialWithCallback(MainActivity.this, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        //code here
//                        Utils.getInstance().replaceActivity(MainActivity.this,OtherActivity.class);
                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
                    }

                    @Override
                    public void onAdFail() {
                        //code here
//                        Utils.getInstance().replaceActivity(MainActivity.this,OtherActivity.class);
                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
                    }
                }, 0);
            }
        });
        btn_LoadAndShowInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdmodUtils.getInstance().loadAndShowAdInterstitialWithCallback(MainActivity.this, getString(R.string.test_ads_admob_inter_id), 0, new AdCallback() {
                    @Override
                    public void onAdClosed() {
                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
                    }

                    @Override
                    public void onAdFail() {onAdClosed();}
                }, true);
            }
        });
        btn_LoadAndShowReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdmodUtils.getInstance().loadAndShowAdRewardWithCallback(MainActivity.this, getString(R.string.test_ads_admob_reward_id), new RewardAdCallback() {
                    @Override
                    public void onAdClosed() {
                        if(AdmodUtils.getInstance().mRewardedAd != null){
                            AdmodUtils.getInstance().mRewardedAd = null;}
                        AdmodUtils.getInstance().dismissAdDialog();
                        //Utils.getInstance().showMessenger(MainActivity.this, "close ad");
                    }

                    @Override
                    public void onEarned() {
                        if(AdmodUtils.getInstance().mRewardedAd != null){
                            AdmodUtils.getInstance().mRewardedAd = null;}
                        AdmodUtils.getInstance().dismissAdDialog();
                        Utils.getInstance().showMessenger(MainActivity.this, "Reward");

                    }

                    @Override
                    public void onAdFail() {
                        //code here
                        Utils.getInstance().showMessenger(MainActivity.this, "Reward fail");
                    }
                }, true);

            }
        });
        btn_LoadNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.getInstance().addActivity(MainActivity.this, NativeRecyclerActivity.class);
            }
        });

        btn_LoadNativeGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.getInstance().addActivity(MainActivity.this, NativeGridActivity.class);
            }
        });


        btn_IAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.getInstance().addActivity(MainActivity.this, IAPActivity.class);
            }
        });
        btn_Rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRate();
            }
        });
//        AdmodUtils.getInstance().loadNativeAdsWithLayout(MainActivity.this, getString(R.string.test_ads_admob_native_id), nativeAds, R.layout.ad_unified_medium);
        AdmodUtils.getInstance().loadNativeAds(MainActivity.this, getString(R.string.test_ads_admob_native_id), nativeAds, GoogleENative.UNIFIED_SMALL, new NativeAdCallback() {
            @Override
            public void onNativeAdLoaded() {

            }

            @Override
            public void onAdFail() {

            }
        });
        AdmodUtils.getInstance().loadAdBanner(MainActivity.this, getString(R.string.test_ads_admob_native_id), banner, GoogleEBanner.SIZE_SMALL);
    }

    private void showDialogRate() {
        RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(1)
                .date(1)
                .setNameApp(getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setEmail("vapp.helpcenter@gmail.com")
                .isShowButtonLater(true)
                .isClickLaterDismiss(true)
                .setTextButtonLater("Maybe Later")
                .setOnlickMaybeLate(new MaybeLaterCallback() {
                    @Override
                    public void onClick() {
                        Utils.getInstance().showMessenger(MainActivity.this, "clicked Maybe Later");
                    }
                })
                .ratingButtonColor(R.color.purple_200)
                .build();

        //Cancel On Touch Outside
        ratingDialog.setCanceledOnTouchOutside(false);
        //show
        ratingDialog.show();


        //thêm vào activity trong manifest
//        <intent-filter>
//            <action android:name="android.intent.action.SENDTO" />
//            <data android:scheme="mailto" />
//            <category android:name="android.intent.category.DEFAULT" />
//        </intent-filter>

        // thêm vào activity
//        android:windowSoftInputMode="adjustPan|adjustResize"

    }


    void finbyid() {
        btn_Utils = findViewById(R.id.btn_Utils);
        btn_LoadInter = findViewById(R.id.btn_LoadInter);
        btn_ShowInter = findViewById(R.id.btn_ShowInter);
        btn_LoadAndShowInter = findViewById(R.id.btn_LoadAndShowInter);
        btn_LoadAndShowReward = findViewById(R.id.btn_LoadAndShowReward);
        btn_LoadNative = findViewById(R.id.btn_LoadNative);
        nativeAds = findViewById(R.id.nativeAds);
        banner = findViewById(R.id.banner);
        btn_IAP = findViewById(R.id.btn_IAP);
        btn_Rate = findViewById(R.id.btn_Rate);
        btn_LoadNativeGrid = findViewById(R.id.btn_LoadNativeGrid);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}