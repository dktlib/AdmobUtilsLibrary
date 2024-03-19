package com.vapp.admoblibrary.ads;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.vapp.admoblibrary.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static final String TAG = "AppOpenManager";
    private static volatile AppOpenManager INSTANCE;
    private AppOpenAd appResumeAd = null;
    private AppOpenAd splashAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private FullScreenContentCallback fullScreenContentCallback;
    private String appResumeAdId;
    private Activity currentActivity;
    private Application myApplication;
    private static boolean isShowingAd = false;
    public boolean isShowingAdsOnResume = false;
    public boolean isShowingAdsOnResumeBanner = false;
    private long appResumeLoadTime = 0;
    private long splashLoadTime = 0;
    private int splashTimeout = 0;
    private boolean isInitialized = false;
    public boolean isAppResumeEnabled = true;
    private final List<Class> disabledAppOpenList;
    private Class splashActivity;
    private boolean isTimeout = false;
    private static final int TIMEOUT_MSG = 11;
    private Dialog dialogFullScreen;
    private Handler timeoutHandler = new Handler(msg -> {
        if (msg.what == TIMEOUT_MSG) {
            isTimeout = true;
        }
        return false;
    });

    /**
     * Constructor
     */
    public AppOpenManager() {
        disabledAppOpenList = new ArrayList<>();
    }

    public static synchronized AppOpenManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppOpenManager();
        }
        return INSTANCE;
    }

    /**
     * Init AppOpenManager
     *
     * @param application
     */
    public void init(Application application, String appOpenAdId) {
        isInitialized = true;
        this.myApplication = application;
        initAdRequest();
        if (AdmobUtils.isTesting) {
            this.appResumeAdId = application.getString(R.string.test_ads_admob_app_open_new);

        } else {
            this.appResumeAdId = appOpenAdId;
        }
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        if (!isAdAvailable(false) && appOpenAdId != null) {
            fetchAd(false);
        }
    }

    AdRequest adRequest;

    // get AdRequest
    public void initAdRequest() {
        adRequest = new AdRequest.Builder()
                .setHttpTimeoutMillis(5000)
                .build();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Check app open ads is showing
     *
     * @return
     */
    public boolean isShowingAd() {
        return isShowingAd;
    }
    public boolean isShowingAdsOnResume() {
        return isShowingAdsOnResume;
    }

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    public void disableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.add(activityClass);
    }

    public void enableAppResumeWithActivity(Class activityClass) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.getName());
        disabledAppOpenList.remove(activityClass);
    }


    public void setAppResumeAdId(String appResumeAdId) {
        this.appResumeAdId = appResumeAdId;
    }

    public void setFullScreenContentCallback(FullScreenContentCallback callback) {
        this.fullScreenContentCallback = callback;
    }

    public void removeFullScreenContentCallback() {
        this.fullScreenContentCallback = null;
    }

    boolean isLoading = false;
    public void fetchAd(final boolean isSplash) {
        Log.d(TAG, "fetchAd: isSplash = " + isSplash);
        if (isAdAvailable(isSplash) || appResumeAdId == null || AppOpenManager.this.appResumeAd!= null) {
            Log.d(TAG, "AppOpenManager: Ad is ready or id = null");
            return;
        }
        if (!isLoading){
            Log.d(TAG, "===fetchAd: Loading");
            isLoading = true;
            loadCallback =
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        /**
                         * Called when an app open ad has loaded.
                         *
                         * @param ad the loaded app open ad.
                         */
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            Log.d(TAG, "AppOpenManager: Loaded");
                            AppOpenManager.this.appResumeAd = ad;
                            AppOpenManager.this.appResumeLoadTime = (new Date()).getTime();
                        }

                        /**
                         * Called when an app open ad has failed to load.
                         *
                         * @param loadAdError the error.
                         */
                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            // Handle the error.
                            isLoading = false;
                            Log.d(TAG, "AppOpenManager: onAdFailedToLoad");
                            String a = "fail";
                        }

                    };
            AppOpenAd.load(
                    myApplication, appResumeAdId, adRequest, loadCallback);
        }

    }


    private boolean wasLoadTimeLessThanNHoursAgo(long loadTime, long numHours) {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


    public boolean isAdAvailable(boolean isSplash) {
        long loadTime = isSplash ? splashLoadTime : appResumeLoadTime;
        boolean wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4);
        Log.d(TAG, "isAdAvailable: " + wasLoadTimeLessThanNHoursAgo);
        return (isSplash ? splashAd != null : appResumeAd != null)
                && wasLoadTimeLessThanNHoursAgo;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("===ADS", activity.getClass() + "|"+AdActivity.class);
        if (activity.getClass() == AdActivity.class){
            Log.d("===ADS", "Back");
            return;
        }
        currentActivity = activity;
        Log.d("===ADS", "Running");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity.getClass() == AdActivity.class){
            return;
        }
        currentActivity = activity;
        if (splashActivity == null) {
            if (!activity.getClass().getName().equals(AdActivity.class.getName())) {
                fetchAd(false);
            }
        } else {
            if (!activity.getClass().getName().equals(splashActivity.getName()) && !activity.getClass().getName().equals(AdActivity.class.getName())) {
                fetchAd(false);
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity.getClass() == AdActivity.class){
            return;
        }
        currentActivity = null;
        if (dialogFullScreen != null && dialogFullScreen.isShowing()){
            dialogFullScreen.dismiss();
        }
    }

    public void showAdIfAvailable(final boolean isSplash) {
        if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Log.d("===Onresume", "STARTED");
            if (fullScreenContentCallback != null) {
                try {
                    dialogFullScreen.dismiss();
                    dialogFullScreen = null;
                }catch (Exception ignored){

                }
                fullScreenContentCallback.onAdDismissedFullScreenContent();
            }
            return;
        }
        Log.d("===Onresume", "FullScreenContentCallback");
        if (!isShowingAd && isAdAvailable(isSplash)) {
            FullScreenContentCallback callback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            isLoading = false;
                            Log.d(TAG, "onAdShowedFullScreenContent: Dismiss");
                            try {
                                dialogFullScreen.dismiss();
                                dialogFullScreen = null;
                            }catch (Exception ignored){

                            }
                            // Set the reference to null so isAdAvailable() returns false.
                            appResumeAd = null;
                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdDismissedFullScreenContent();
                            }
                            isShowingAd = false;
                            fetchAd(isSplash);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            isLoading = false;
                            Log.d(TAG, "onAdShowedFullScreenContent: Show false");
                            try {
                                dialogFullScreen.dismiss();
                                dialogFullScreen = null;
                            }catch (Exception ignored){

                            }

                            if (fullScreenContentCallback != null) {
                                fullScreenContentCallback.onAdFailedToShowFullScreenContent(adError);
                            }
                            fetchAd(isSplash);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            Log.d(TAG, "onAdShowedFullScreenContent: Show");
                            isShowingAd = true;
                            appResumeAd = null;
                        }
                    };
            showAdsResume(isSplash, callback);

        } else {
            Log.d(TAG, "Ad is not ready");
            if (!isSplash) {
                fetchAd(false);
            }
        }
    }

    private void showAdsResume(final boolean isSplash, final FullScreenContentCallback callback) {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            new Handler().postDelayed(() -> {
                if (appResumeAd != null){
                    appResumeAd.setFullScreenContentCallback(callback);
                    if (currentActivity != null){
                        showDialog(currentActivity);
                        appResumeAd.show(currentActivity);
                    }
                }
            }, 100);
        }
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Log.d("===Onresume", "onresume");
        if (currentActivity == null) {
            return;
        }
        if(AdmobUtils.isAdShowing){
            return;
        }
        if (!AdmobUtils.isShowAds) {
            return;
        }

        if (!isAppResumeEnabled) {
            Log.d("===Onresume", "isAppResumeEnabled");
            return;
        } else {
            if(AdmobUtils.dialog != null && AdmobUtils.dialog.isShowing())
                AdmobUtils.dialog.dismiss();
        }

        for (Class activity : disabledAppOpenList) {
            if (activity.getName().equals(currentActivity.getClass().getName())) {
                Log.d(TAG, "onStart: activity is disabled");
                return;
            }
        }
        showAdIfAvailable(false);
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    public void onResume() {
//
//    }

    public void showDialog(Context context){
        isShowingAdsOnResume = true;
        isShowingAdsOnResumeBanner = true;
        dialogFullScreen = new Dialog(context);
        dialogFullScreen.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFullScreen.setContentView(R.layout.dialog_full_screen_onresume);
        dialogFullScreen.setCancelable(false);
        dialogFullScreen.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialogFullScreen.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        try {
            if (!currentActivity.isFinishing() && dialogFullScreen!=null && !dialogFullScreen.isShowing()){
                dialogFullScreen.show();
            }
        }catch (Exception ignored){

        }
    }
}

