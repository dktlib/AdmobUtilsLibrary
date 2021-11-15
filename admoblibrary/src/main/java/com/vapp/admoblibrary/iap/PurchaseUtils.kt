package com.vapp.admoblibrary.iap

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object PurchaseUtils {
    lateinit var billingClient: BillingClient
    fun initBilling(context: Context,iapCallback: IapCallback) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, mutableList ->

                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        iapCallback.onSubscribeSuccessful()
                    }
                    else -> {
                        iapCallback.onSubscribeError()
                    }
                }
            }
            .enablePendingPurchases()
            .build()
    }
    fun querySubscriptionSkuDetails(vararg  querySku:String,onQuerySkuDetailCompleted: OnQuerySkuDetailCompleted) {
        val skuList = ArrayList<String>()
        for(sku in querySku){
            skuList.add(sku)
        }
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {

                    // leverage querySkuDetails Kotlin extension function
                    val skuDetailsResult = billingClient.querySkuDetailsAsync(params.build(),object : SkuDetailsResponseListener {
                        override fun onSkuDetailsResponse(
                            p0: BillingResult,
                            p1: MutableList<SkuDetails>?
                        ) {
                            if(p0.responseCode == BillingClient.BillingResponseCode.OK){
                                p1?.let{
                                    onQuerySkuDetailCompleted.onQuerySuccessful(it)
                                }?:let{
                                    onQuerySkuDetailCompleted.onQueryFailed("SkuListIsNull")
                                }
                            }
                            else{
                                onQuerySkuDetailCompleted.onQueryFailed("Response code is not OK")
                            }
                        }
                    })

                }
            }
            override fun onBillingServiceDisconnected() {

            }
        })

        // Process the result.
    }
    fun launchSubscribeFlow(activity: Activity, queryDetails: SkuDetails){
//        val querySkuList:List<SkuDetails>? =  querySubscriptionSkuDetails(querySku)
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(queryDetails)
                .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode

    }
    fun queryPurchases(skuType:String,purchaseCallback: PurchaseCallback){
        billingClient.queryPurchasesAsync(skuType,object : PurchasesResponseListener {
            override fun onQueryPurchasesResponse(p0: BillingResult, p1: MutableList<Purchase>) {
                if(p1.size>0){
                    purchaseCallback.onQuerySuccessful()
                }
                else{
                    purchaseCallback.onQueryFail()
                }
            }
        })
    }

}