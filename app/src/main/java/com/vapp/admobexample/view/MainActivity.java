package com.vapp.admobexample.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codemybrainsout.ratingdialog.MaybeLaterCallback;
import com.codemybrainsout.ratingdialog.RatingDialog;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.vapp.admobexample.utilsdemp.AdsManager;
import com.vapp.admoblibrary.AdsInterCallBack;
import com.vapp.admoblibrary.ads.AdLoadCallback;
import com.vapp.admoblibrary.ads.AppOpenManager;
import com.vapp.admoblibrary.ads.NativeAdCallback;
import com.vapp.admoblibrary.ads.admobnative.enumclass.CollapsibleBanner;
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleENative;
import com.vapp.admobexample.R;
import com.vapp.admobexample.iap.IAPActivity;
import com.vapp.admoblibrary.ads.model.AdUnitListModel;
import com.vapp.admoblibrary.ads.model.BannerAdCallback;
import com.vapp.admoblibrary.utils.Utils;
import com.vapp.admoblibrary.ads.AdmobUtils;
import com.vapp.admoblibrary.ads.RewardAdCallback;

public class MainActivity extends AppCompatActivity {
    Button btn_LoadInter, btn_ShowInter, btn_ShowInter1, btn_ShowInter2, btn_ShowInter3;
    Button btn_LoadAndShowInter, btn_LoadAndShowReward;
    Button btn_LoadInterReward, btn_ShowInterReward;
    Button btn_LoadNativeinRec, btn_LoadNativeGrid;
    Button btn_LoadAndShowNative, btn_LoadAndGetNative, btn_ShowNative;
    Button btn_IAP, btn_Rate, btn_Utils;
    FrameLayout viewNativeAds;
    FrameLayout banner;
    public NativeAd nativeAd;
    public NativeAd nativeAd2;
    public NativeAd nativeAd3;

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 5) {
                    AdsManager.INSTANCE.loadAndShowNative(this,viewNativeAds,AdsManager.INSTANCE.getNativeHolder());

                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        findbyid();
        AdsManager.INSTANCE.loadNativeFullScreen(this,AdsManager.INSTANCE.getNativeHolder());
        //API data sample
        AdUnitListModel adUnitList = Utils.getInstance().getAdUnitByName("Name AdUnit", "Defaul Id Admob");
        //check Countries (BOOL)
        btn_Utils.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AdsManager.INSTANCE.loadNative(MainActivity.this,AdsManager.INSTANCE.getNativeHolder());
//                aoaManager.loadAndShowAoA();
                mStartForResult.launch(new Intent(MainActivity.this, OtherActivity.class));
            }
        });
        btn_LoadInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdsManager.INSTANCE.loadInter(MainActivity.this,AdsManager.INSTANCE.getInterholder());
            }
        });
        btn_ShowInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AdsManager.INSTANCE.showAdInter(MainActivity.this, AdsManager.INSTANCE.getInterholder(), new AdsManager.AdListener() {
//                    @Override
//                    public void onAdClosedOrFailed() {
//                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
//                    }
//                });

                AdmobUtils.loadAndShowAdInterstitial(MainActivity.this, AdsManager.INSTANCE.getInterholder(), new AdsInterCallBack() {
                    @Override
                    public void onStartAction() {

                    }

                    @Override
                    public void onEventClickAdClosed() {
                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
                    }

                    @Override
                    public void onAdShowed() {

                    }

                    @Override
                    public void onAdLoaded() {

                    }

                    @Override
                    public void onAdFail(String error) {
                        Utils.getInstance().addActivity(MainActivity.this, OtherActivity.class);
                    }

                    @Override
                    public void onPaid(AdValue adValue, String adUnitAds) {

                    }
                },true);
            }
        });
        btn_LoadAndShowInter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdsManager.INSTANCE.loadAndShowIntersial(MainActivity.this, new AdsManager.AdListener() {
                    @Override
                    public void onAdClosedOrFailed() {
                        startActivity(new Intent(MainActivity.this, OtherActivity.class));
                    }
                });
            }
        });
        btn_LoadAndShowReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdmobUtils.loadAndShowAdRewardWithCallback(MainActivity.this, getString(R.string.test_ads_admob_reward_id), new RewardAdCallback() {
                    @Override
                    public void onAdClosed() {
                        if (AdmobUtils.mRewardedAd != null) {
                            AdmobUtils.mRewardedAd = null;
                        }
                        AdmobUtils.dismissAdDialog();
                        //Utils.getInstance().showMessenger(MainActivity.this, "close ad");
                        startActivity(new Intent(MainActivity.this, OtherActivity.class));
                    }

                    @Override
                    public void onAdShowed() {
                        Utils.getInstance().showMessenger(MainActivity.this, "onAdShowed");
                        new Handler().postDelayed(() -> AdmobUtils.dismissAdDialog(),800);
                    }

                    @Override
                    public void onAdFail(String message) {
                        Utils.getInstance().showMessenger(MainActivity.this, "Reward fail");

                    }

                    @Override
                    public void onEarned() {
                        if (AdmobUtils.mRewardedAd != null) {
                            AdmobUtils.mRewardedAd = null;
                        }
                        AdmobUtils.dismissAdDialog();
                        Utils.getInstance().showMessenger(MainActivity.this, "Reward");

                    }

                    @Override
                    public void onPaid(AdValue adValue, String adUnitAds) {
                        Toast.makeText(MainActivity.this,adValue.getValueMicros() + adValue.getCurrencyCode(),Toast.LENGTH_SHORT).show();
                    }
                }, true);

            }
        });
        btn_LoadNativeinRec.setOnClickListener(new View.OnClickListener() {
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
        btn_LoadInterReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdmobUtils.loadAdInterstitialReward(MainActivity.this, AdsManager.INSTANCE.getInterRewardHolder(), new AdLoadCallback() {
                    @Override
                    public void onAdFail(String message) {

                    }

                    @Override
                    public void onAdLoaded() {
                        //show dialog
                    }
                });
            }

        });

        btn_ShowInterReward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdmobUtils.showAdInterstitialRewardWithCallback( MainActivity.this,AdsManager.INSTANCE.getInterRewardHolder(), new RewardAdCallback() {
                    @Override
                    public void onAdClosed() {
                        startActivity(new Intent(MainActivity.this, OtherActivity.class));
                    }

                    @Override
                    public void onAdShowed() {
                        Utils.getInstance().showMessenger(MainActivity.this, "onAdShowed");
                    }

                    @Override
                    public void onAdFail(String message) {
                        Utils.getInstance().showMessenger(MainActivity.this, "onAdFail");
                        startActivity(new Intent(MainActivity.this, OtherActivity.class));
                    }

                    @Override
                    public void onEarned() {
                        Utils.getInstance().showMessenger(MainActivity.this, "onEarned");
                        //bool true
                    }

                    @Override
                    public void onPaid(AdValue adValue, String adUnitAds) {
                        Toast.makeText(MainActivity.this, adValue.getValueMicros() + adValue.getCurrencyCode(),Toast.LENGTH_SHORT).show();
                    }
                });
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

        btn_LoadAndShowNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdmobUtils.loadAndShowNativeAdsWithLayoutAds(MainActivity.this, AdsManager.INSTANCE.getNativeHolder(), viewNativeAds, R.layout.ad_template_medium, GoogleENative.UNIFIED_MEDIUM, new AdmobUtils.NativeAdCallbackNew() {
                    @Override
                    public void onClickAds() {

                    }

                    @Override
                    public void onNativeAdLoaded() {
                    }

                    @Override
                    public void onAdFail(String s) {

                    }

                    @Override
                    public void onAdPaid(AdValue adValue, String s) {

                    }

                    @Override
                    public void onLoadedAndGetNativeAd(NativeAd ad) {

                    }
                });

            }
        });

        btn_LoadAndGetNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdsManager.INSTANCE.loadNative(MainActivity.this,AdsManager.INSTANCE.getNativeHolder());
            }
        });

        btn_ShowNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdsManager.INSTANCE.showAdNativeMedium(MainActivity.this,viewNativeAds,AdsManager.INSTANCE.getNativeHolder());
            }
        });
        AdsManager.showAdBanner(this,"", findViewById(R.id.banner),findViewById(R.id.line));

//        AdmobUtils.loadAndShowBannerCollapsibleWithConfig(this, "", 5, findViewById(R.id.banner), new AdmobUtils.BannerCollapsibleAdCallback() {
//            @Override
//            public void onClickAds() {
//
//            }
//
//            @Override
//            public void onBannerAdLoaded(@NonNull AdSize adSize) {
//                Log.d("===Banner","onBannerAdLoaded");
//                Toast.makeText(MainActivity.this,"onBannerAdLoaded",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAdFail(@NonNull String message) {
//
//            }
//
//            @Override
//            public void onAdPaid(@NonNull AdValue adValue, @NonNull AdView mAdView) {
//
//            }
//        });
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
                .ignoreRated(true)
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

    void findbyid() {
        btn_Utils = findViewById(R.id.btn_Utils);
        btn_LoadInter = findViewById(R.id.btn_LoadInter);
        btn_ShowInter = findViewById(R.id.btn_ShowInter);
        btn_LoadAndShowInter = findViewById(R.id.btn_LoadAndShowInter);
        btn_LoadAndShowReward = findViewById(R.id.btn_LoadAndShowReward);
        btn_LoadNativeinRec = findViewById(R.id.btn_LoadNative);
        viewNativeAds = findViewById(R.id.nativeAds);
        banner = findViewById(R.id.banner);
        btn_IAP = findViewById(R.id.btn_IAP);
        btn_Rate = findViewById(R.id.btn_Rate);
        btn_LoadNativeGrid = findViewById(R.id.btn_LoadNativeGrid);

        btn_LoadInterReward = findViewById(R.id.btn_LoadInterReward);
        btn_ShowInterReward = findViewById(R.id.btn_ShowInterReward);

        btn_LoadAndGetNative = findViewById(R.id.btn_LoadAndGetNative);
        btn_ShowNative = findViewById(R.id.btn_ShowNative);
        btn_LoadAndShowNative = findViewById(R.id.btn_LoadAndShowNative);


    }

    @Override
    protected void onResume() {
        super.onResume();
        AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity.class);
//        AdsManager.showAdBannerCollapsible(this,AdsManager.INSTANCE.getBannerHolder(), findViewById(R.id.banner),findViewById(R.id.line));
    }
}