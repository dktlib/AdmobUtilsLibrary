package com.vapp.admoblibrary.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleEBanner;
import com.vapp.admoblibrary.ads.admobnative.enumclass.GoogleENative;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.vapp.admoblibrary.R;
import com.vapp.admoblibrary.ads.admobnative.NativeFunc;
import com.vapp.admoblibrary.ads.model.AdUnitListModel;
import com.vapp.admoblibrary.utils.DialogCallback;
import com.vapp.admoblibrary.utils.DialogType;
import com.vapp.admoblibrary.utils.SweetAlert.SweetAlertDialog;
import com.vapp.admoblibrary.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AdmodUtils {
    SweetAlertDialog dialog;
    public long lastTimeShowInterstitial = 0;
    public long timeOut = 0;
    public boolean isAdShowing = false;
    public boolean isShowAds = true;
    public boolean isTesting = false;
    public List<String> testDevices = new ArrayList<>();
    private static volatile AdmodUtils INSTANCE;

    public static synchronized AdmodUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmodUtils();
        }
        return INSTANCE;
    }


    public void initAdmob(Context context, int timeout, boolean isDebug, boolean isEnableAds) {
        timeOut = timeout;
        if (timeOut < 5000 && timeout!= 0) {
            Toast.makeText(context,"Nên để limit time ~10000",Toast.LENGTH_LONG).show();
        }

        if (timeout > 0) {
            timeOut = timeout;
        } else {
            timeOut = 10000;
        }

        if (isDebug) {
            isTesting = true;
        } else {
            isTesting = false;
        }

        if (!isEnableAds) {
            isShowAds = false;
        }

        MobileAds.initialize(context, initializationStatus -> {
        });

        initListIdTest();
        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        initAdRequest(timeout);


    }

    public AdRequest adRequest;

    // get AdRequest
    public void initAdRequest(int timeOut) {
        adRequest = new AdRequest.Builder()
                .setHttpTimeoutMillis(timeOut)
                .build();
    }

    public void initListIdTest() {
        testDevices.add("727D4F658B63BDFA0EFB164261AAE54");
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0");
        testDevices.add("482996BF6946FBE1B9FFD3975144D084");
        testDevices.add("8619926A823916A224795141B93B7E0B");
        testDevices.add("6399D5AEE5C75205B6C0F6755365CF21");
        testDevices.add("2E379568A9F147A64B0E0C9571DE812D");
        testDevices.add("A0518C6FA4396B91F82B9656DE83AFC7");
        testDevices.add("C8EEFFC32272E3F1018FC72ECBD46F0C");
        testDevices.add("284A7F7624F1131E7341ECDCBBCDF9A8");
        testDevices.add("FEECD9793CCCE1E0FF8D392B0DB65559");
        testDevices.add("D34AE6EC4CBA619D6243B03D4E31EED6");
        testDevices.add("25F9EEACB11D46869D2854923615D839");
        testDevices.add("A5CB09DBBE486E3421502DFF53070339");
        testDevices.add("5798E06F645D797640A9C4B90B6CBEA7");
        testDevices.add("E91FD94E971864C3880FB434D1C39A03");
        testDevices.add("50ACF2DAA0884FF8B08F7C823E046DEA");
        testDevices.add("97F07D4A6D0145F9DB7114B63D3D8E9B");
        testDevices.add("4C96668EC6F204034D0CDCE1B94A4E65");
        testDevices.add("00A52C89E14694316247D3CA3DF19F6B");
        testDevices.add("C38A7BF0A80E31BD6B76AF6D0C1EE4A1");
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E");
        testDevices.add("39D7026016640CEA1502836C6EF3776D");
        testDevices.add("A99C99C378EE9BDE5D3DE404D3A4A812");
        testDevices.add("EB28F4CCC32F14DC98068A063B97E6CE");
        //Oneplus GM1910
        testDevices.add("D94D5042C9CC42DA75DCC0C4C233A500");
        //Redmi note 4
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0");
        //Galaxy M11
        testDevices.add("AF6ABEDE9EE7719295BF5E6F19A40452");
        //Samsung SM-G610F
        testDevices.add("2B018C52668CBA0B033F411955A5B561");
        //Realme RMX1851
        testDevices.add("39D7026016640CEA1502836C6EF3776D");
        //Redmi 5 Plus
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E");
        //Redmi 9A
        testDevices.add("13D67F452A299DB825A348917D52D640");
        //POCO X3
        testDevices.add("7D94825002E2407B75A9D5378194CFA9");
        //Galaxy A21S
        testDevices.add("98EFC23E56FA228C791F8C3AFBEE44D4");
        //Oppo CPH1825
        testDevices.add("805702C1D9D4FD957AFE14F3D69E79F7");
        //Xiaomi Redmi Note 7
        testDevices.add("9C62AAC36B9F23413AF4D66FE48F9E9B");

    }

    //check open network
    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    public void loadAdBanner(Context context, String bannerId, ViewGroup viewGroup, GoogleEBanner size) {

        if (!isShowAds) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdView mAdView = new AdView(context);
        if (isTesting) {
            bannerId = context.getString(R.string.test_ads_admob_banner_id);
        }
        mAdView.setAdUnitId(bannerId);
        if (size == GoogleEBanner.SIZE_SMALL) {
            mAdView.setAdSize(AdSize.BANNER);
        } else if (size == GoogleEBanner.SIZE_MEDIUM) {
            mAdView.setAdSize(AdSize.LARGE_BANNER);
        } else if (size == GoogleEBanner.SIZE_FULL) {
            mAdView.setAdSize(AdSize.FULL_BANNER);
        } else {
            mAdView.setAdSize(AdSize.SMART_BANNER);
        }

        viewGroup.removeAllViews();
        viewGroup.addView(mAdView);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.e(" Admod", "onAdLoaded");

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(" Admod", "failloadbanner" + adError.getMessage());

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        mAdView.loadAd(adRequest);

        Log.e(" Admod", "loadAdBanner");
    }

    // ads native
    public void loadNativeAdsWithLayout(Activity activity, String s, ViewGroup viewGroup, int layout, NativeAdCallback adCallback) {

        if (!isShowAds) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdLoader adLoader;
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id);
        }

        adLoader = new AdLoader.Builder(activity, s)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        adCallback.onNativeAdLoaded();

                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                                .inflate(layout, null);

                        NativeFunc.Companion.populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM);

                        viewGroup.removeAllViews();
                        viewGroup.addView(adView);
                        viewGroup.setVisibility(View.VISIBLE);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        adLoader.loadAd(adRequest);
        Log.e("Admod", "loadAdNativeAds");
    }

    // ads native
    @SuppressLint("StaticFieldLeak")
    public void loadNativeAds(Activity activity, String s, ViewGroup viewGroup, GoogleENative size, NativeAdCallback adCallback) {

        if (!isShowAds) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdLoader adLoader;
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id);
        }

        adLoader = new AdLoader.Builder(activity, s)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        adCallback.onNativeAdLoaded();
                        int id = 0;
                        if (size == GoogleENative.UNIFIED_MEDIUM) {
                            id = R.layout.ad_unified_medium;
                        } else {
                            id = R.layout.ad_unified_small;
                        }

                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                                .inflate(id, null);

                        NativeFunc.Companion.populateNativeAdView(nativeAd, adView, size);

                        viewGroup.removeAllViews();
                        viewGroup.addView(adView);
                        viewGroup.setVisibility(View.VISIBLE);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        adLoader.loadAd(adRequest);
        Log.e("Admod", "loadAdNativeAds");
    }


    //reward
    RewardedAd mRewardedAd = null;

    public void loadAndShowAdRewardWithCallback(Activity activity, String admobId, RewardAdCallback adCallback2, boolean enableLoadingDialog) {

        Handler handlerTimeOut = new Handler();
        handlerTimeOut.postDelayed(new Runnable() {
            public void run() {
                adCallback2.onAdClosed();
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
            }
        }, timeOut);

        if (!isShowAds) {
            adCallback2.onAdClosed();
            if (dialog != null) {
                dialog.dismiss();
            }
            return;
        }


        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_reward_id);
        }
        if (enableLoadingDialog) {

            dialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            dialog.setTitleText("Loading ads. Please wait...");
            dialog.setCancelable(false);
            dialog.show();

        }

        isAdShowing = false;
        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().isAppResumeEnabled = false;
        }

        RewardedAd.load(activity, admobId,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        mRewardedAd = null;
                        adCallback2.onAdFail();
                        handlerTimeOut.removeCallbacksAndMessages(null);
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                        }
                        Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + loadAdError.getCause());

                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;

                        isAdShowing = false;
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = false;
                        }

                        if (dialog != null) {
                            dialog.dismiss();
                        }


                        if (mRewardedAd != null) {
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdShowedFullScreenContent() {
                                    isAdShowing = false;
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    if (AppOpenManager.getInstance().isInitialized()) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = false;
                                    }
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    // Called when ad fails to show.
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    adCallback2.onAdFail();
                                    handlerTimeOut.removeCallbacksAndMessages(null);

                                    mRewardedAd = null;
                                    if (AppOpenManager.getInstance().isInitialized()) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true;
                                    }
                                    Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                                    Log.e("Admodfail", "errorCodeAds" + adError.getCause());

                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    mRewardedAd = null;
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    adCallback2.onAdClosed();
                                    handlerTimeOut.removeCallbacksAndMessages(null);

                                    if (AppOpenManager.getInstance().isInitialized()) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true;
                                    }
                                }
                            });


                            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                                try {
                                    if (dialog != null && dialog.isShowing())
                                        dialog.dismiss();
                                } catch (Exception e) {
                                    dialog = null;
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                    e.printStackTrace();
                                }
                                new Handler().postDelayed(() -> {
                                    if (AppOpenManager.getInstance().isInitialized()) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = false;

                                    }
                                    mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                            // Handle the reward.
                                            adCallback2.onEarned();


                                        }


                                    });
                                }, 800);

                            }

                        } else {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            adCallback2.onAdFail();
                            handlerTimeOut.removeCallbacksAndMessages(null);

                            if (AppOpenManager.getInstance().isInitialized()) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true;
                            }
                        }
                    }
                });


    }

    //inter ads
    public InterstitialAd mInterstitialAd;

    public void loadAdInterstitial(Activity activity, String admobId, boolean enableLoadingDialog) {

        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id);
        } else {
            if (admobId.equals(activity.getString(R.string.test_ads_admob_inter_id)) && !BuildConfig.DEBUG) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Utils.getInstance().showDialogTitle(activity, "Warning", "Build bản release nhưng đang để id test ads", "Đã biết", DialogType.WARNING_TYPE, false, "", new DialogCallback() {
                    @Override
                    public void onClosed() {

                    }

                    @Override
                    public void cancel() {

                    }
                });

            }
        }

        if (enableLoadingDialog) {
            dialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            dialog.setTitleText("Loading ads. Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        InterstitialAd.load(activity, admobId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull @org.jetbrains.annotations.NotNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull @org.jetbrains.annotations.NotNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd = null;
                Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.getMessage());
                Log.e("Admodfail", "errorCodeAds" + loadAdError.getCause());

            }
        });


    }

    public void showAdInterstitialWithCallback(Activity activity, AdCallback adCallback, int limitTime) {
        if (!isShowAds) {
            adCallback.onAdClosed();
            return;
        }

        Handler handlerTimeOut = new Handler();
        handlerTimeOut.postDelayed(new Runnable() {
            public void run() {

                adCallback.onAdClosed();
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
            }
        }, timeOut);

        long currentTime = getCurrentTime();
        if (mInterstitialAd != null) {
            if (currentTime - lastTimeShowInterstitial >= limitTime) {

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull @NotNull AdError adError) {
                        Log.e("Admodfail", "errorCodeAds:" + adError.getCause());
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());

                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        adCallback.onAdClosed();
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                        }
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        adCallback.onAdClosed();
                        lastTimeShowInterstitial = new Date().getTime();

                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;

                        }
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                    }
                });
            } else {
                if (dialog != null) {
                    dialog.dismiss();
                }
                adCallback.onAdClosed();
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;

                }
            }
        }
        if (mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                } catch (Exception e) {
                    dialog = null;
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().isAppResumeEnabled = false;

                    }
                    mInterstitialAd.show(activity);

                }, 800);

            }
        }
    }



    public void loadAndShowAdInterstitialWithCallback(Activity activity
            , String admobId, int limitTime, AdCallback adCallback, boolean enableLoadingDialog) {

        Handler handlerTimeOut = new Handler();
        handlerTimeOut.postDelayed(new Runnable() {
            public void run() {
                adCallback.onAdClosed();
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
            }
        }, timeOut);

        if (!isShowAds) {
            adCallback.onAdClosed();
            handlerTimeOut.removeCallbacksAndMessages(null);
            return;
        }

        if (AppOpenManager.getInstance().isInitialized()) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return;
            } else {
                isAdShowing = false;
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false;
                }
            }
        }

        long currentTime = getCurrentTime();
        if (currentTime - lastTimeShowInterstitial >= limitTime) {
            if (enableLoadingDialog) {
                dialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
                dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                dialog.setTitleText("Loading ads. Please wait...");
                dialog.setCancelable(false);
                dialog.show();
            }

            if (isTesting) {
                admobId = activity.getString(R.string.test_ads_admob_inter_id);
            } else {
                checkIdTest(activity, admobId);
            }
            InterstitialAd.load(activity, admobId, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull @org.jetbrains.annotations.NotNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    mInterstitialAd = interstitialAd;
                    handlerTimeOut.removeCallbacksAndMessages(null);

                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull AdError adError) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                adCallback.onAdFail();
                                handlerTimeOut.removeCallbacksAndMessages(null);
                                isAdShowing = false;
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                                }
                                Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                                Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                            }


                            @Override
                            public void onAdDismissedFullScreenContent() {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                lastTimeShowInterstitial = new Date().getTime();
                                handlerTimeOut.removeCallbacksAndMessages(null);
                                adCallback.onAdClosed();

                                isAdShowing = false;
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                                }
                            }
                        });

                        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                            try {
//                                if (dialog != null && dialog.isShowing())
//                                    dialog.dismiss();
                            } catch (Exception e) {
                                dialog = null;
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                e.printStackTrace();
                            }
                            new Handler().postDelayed(() -> {
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = false;
                                }

                                mInterstitialAd.show(activity);
                            }, 800);
                        }

                    } else {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        adCallback.onAdFail();
                        handlerTimeOut.removeCallbacksAndMessages(null);
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                        }
                    }
                }

                @Override
                public void onAdFailedToLoad(@NonNull @org.jetbrains.annotations.NotNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    mInterstitialAd = null;

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    adCallback.onAdFail();
                    handlerTimeOut.removeCallbacksAndMessages(null);
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true;
                    }

                    Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.getMessage());
                    Log.e("Admodfail", "errorCodeAds" + loadAdError.getCause());
                }
            });

        } else {
            if (dialog != null) {
                dialog.dismiss();
            }
            adCallback.onAdClosed();
            handlerTimeOut.removeCallbacksAndMessages(null);
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().isAppResumeEnabled = true;
            }

        }
    }

    private void checkIdTest(Activity activity, String admobId) {
        if (admobId.equals(activity.getString(R.string.test_ads_admob_inter_id)) && !BuildConfig.DEBUG) {
            if (dialog != null) {
                dialog.dismiss();
            }
            Utils.getInstance().showDialogTitle(activity, "Warning", "Build bản release nhưng đang để id test ads", "Đã biết", DialogType.WARNING_TYPE, false, "", new DialogCallback() {
                @Override
                public void onClosed() {
                }

                @Override
                public void cancel() {
                }
            });
        }
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public String getDeviceID(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
        return deviceId;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
