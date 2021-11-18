package com.vapp.admoblibrary.iap;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.vapp.admoblibrary.R;
import com.vapp.admoblibrary.utils.Utils;

import java.util.List;

public class PurchaseUtils {
    public static BillingProcessor bp;
    public static PurchaseInfo purchaseTransactionDetails = null;
    public static SkuDetailsModel detailsModel;
    private static volatile PurchaseUtils INSTANCE;


    public static synchronized PurchaseUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PurchaseUtils();
        }
        return INSTANCE;
    }

    public void initBilling(Context context, String play_console_license) {
            bp =BillingProcessor.newBillingProcessor(context, play_console_license, new BillingProcessor.IBillingHandler() {
                @Override
                public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {

                }

                @Override
            public void onPurchaseHistoryRestored() {
               // Utils.getInstance().showMessenger(context,"onPurchaseHistoryRestored");

            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
                Utils.getInstance().showMessenger(context,"onBillingError");

            }

            @Override
            public void onBillingInitialized() {
//                getDetailSubscribe(context, "1111");
            }
        });
        bp.initialize();

        BillingClient billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    int a = 0;

                }

            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                int a = 0;

            }
        });
    }


    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    int a = 0;
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                int a = 0;

            } else {
                // Handle any other error codes.
                int a = 0;

            }
        }
    };

    private  boolean hasSubscription() {
        if (purchaseTransactionDetails != null) {
            return purchaseTransactionDetails.purchaseData != null;
        }
        return false;
    }


    public  boolean isSubscriptiond(String idSubscribe) {
        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            @Override
            public void onPurchasesSuccess() {

            }

            @Override
            public void onPurchasesError() {

            }
        });
        purchaseTransactionDetails = bp.getSubscriptionPurchaseInfo(idSubscribe);
        if (hasSubscription()) {
            return  true;
        } else {
            return false;
        }
    }

    public  boolean isPurchased(String idSubscribe) {
        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            @Override
            public void onPurchasesSuccess() {

            }

            @Override
            public void onPurchasesError() {

            }
        });
        purchaseTransactionDetails = bp.getPurchaseInfo(idSubscribe);
        if (hasSubscription()) {
            return  true;
        } else {
            return false;
        }
    }

    public  void subscribeById(Activity context, String idSubscribe) {
        if (bp.isSubscriptionUpdateSupported()) {
            bp.subscribe(context, idSubscribe);
        } else {
            Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported");
        }
    }


    public  void purchaseById(Activity context, String idSubscribe) {
        if (bp.isSubscriptionUpdateSupported()) {
            bp.purchase(context, idSubscribe);
        } else {
            Log.d("MainActivity", "onBillingInitialized: Subscription updated is not supported");
        }
    }

    public void getDetailSubscribe(Context activity, String idSubscribe, PurchaseCallback callback) {
        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            @Override
            public void onPurchasesSuccess() {
                //Load success
                bp.getSubscriptionListingDetailsAsync(idSubscribe, new BillingProcessor.ISkuDetailsResponseListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSkuDetailsResponse(@Nullable final List<SkuDetails> products) {
                        if (products != null) {
                            if(products.size()>0)
                            {
                                Log.d("onSkuDetailsCallBack","Succsess");
                                callback.onSkuDetailsResponse(new SkuDetailsModel(products.get(0).productId, products.get(0).title, products.get(0).description, products.get(0).isSubscription, products.get(0).currency, products.get(0).priceValue, products.get(0).subscriptionPeriod, products.get(0).subscriptionFreeTrialPeriod, products.get(0).haveTrialPeriod, products.get(0).introductoryPriceValue, products.get(0).introductoryPricePeriod, products.get(0).haveIntroductoryPeriod, products.get(0).introductoryPriceCycles));
                            }
                        }
                        else{
                            callback.onSkuDetailsError("Product list cannot be null");
                        }
                    }

                    @Override
                    public void onSkuDetailsError(String string) {
                        callback.onSkuDetailsError(string);
                        Log.d("onSkuDetailsCallBack","Fails");
                    }
                });

            }

            @Override
            public void onPurchasesError() {
                //Load fails
                detailsModel = null;
            }
        });
    }


    public void getDetailPurchase(Activity activity, String idSubscribe,PurchaseCallback callback) {
        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            @Override
            public void onPurchasesSuccess() {
                bp.getPurchaseListingDetailsAsync(idSubscribe, new BillingProcessor.ISkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@Nullable List<SkuDetails> products) {

                        SkuDetailsModel detailsModel = null;
                        if (products != null) {
                            detailsModel = new SkuDetailsModel(products.get(0).productId, products.get(0).title, products.get(0).description, products.get(0).isSubscription, products.get(0).currency, products.get(0).priceValue, products.get(0).subscriptionPeriod, products.get(0).subscriptionFreeTrialPeriod, products.get(0).haveTrialPeriod, products.get(0).introductoryPriceValue, products.get(0).introductoryPricePeriod, products.get(0).haveIntroductoryPeriod, products.get(0).introductoryPriceCycles);
                            callback.onSkuDetailsResponse(detailsModel);
                        }
                        else{
                            callback.onSkuDetailsError("products cannot be null");
                        }
                    }

                    @Override
                    public void onSkuDetailsError(String error) {
                        callback.onSkuDetailsError(error);
                    }
                });
            }

            @Override
            public void onPurchasesError() {
                callback.onSkuDetailsError(activity.getString(R.string.error_getting_sku_model));
            }
        });
    }


    public void restoreSubscription(String idSubscribeOrPurchases,PurchaseCheckCallback callback){
        bp.loadOwnedPurchasesFromGoogleAsync(new BillingProcessor.IPurchasesResponseListener() {
            void checkSubscription(){
                purchaseTransactionDetails = bp.getSubscriptionPurchaseInfo(idSubscribeOrPurchases);
                if (hasSubscription()) {
                    callback.onQueryComplete(true);
                } else {
                    callback.onQueryComplete(false);
                }
            }
            @Override
            public void onPurchasesSuccess() {
                checkSubscription();
            }

            @Override
            public void onPurchasesError() {
                checkSubscription();
            }
        });
    }

    @Deprecated
    public boolean restorePurchase(String idSubscribeOrPurchases){
//        bp.loadOwnedPurchasesFromGoogle();
//        purchaseTransactionDetails = bp.getPurchaseTransactionDetails(idSubscribeOrPurchases);
//        if (isPurchased(idSubscribeOrPurchases)) {
//            return  true;
//        } else {
//            return false;
//        }
        return false;
    }
}
