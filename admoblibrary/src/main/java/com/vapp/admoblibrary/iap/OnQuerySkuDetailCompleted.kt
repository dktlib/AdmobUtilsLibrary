package com.vapp.admoblibrary.iap

import com.android.billingclient.api.SkuDetails

interface OnQuerySkuDetailCompleted {
    fun onQuerySuccessful(skuDetailsList: List<SkuDetails>)
    fun onQueryFailed(error:String)
}