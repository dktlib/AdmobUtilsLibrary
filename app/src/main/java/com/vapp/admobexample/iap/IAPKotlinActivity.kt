package com.vapp.admobexample.iap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.vapp.admobexample.R
import com.vapp.admobexample.databinding.ActivityIapkotlinBinding
import com.vapp.admoblibrary.iap.IapCallback
import com.vapp.admoblibrary.iap.PurchaseCallback
import com.vapp.admoblibrary.iap.PurchaseUtils.initBilling
import com.vapp.admoblibrary.iap.PurchaseUtils.launchSubscribeFlow
import com.vapp.admoblibrary.iap.PurchaseUtils.queryPurchases
import com.vapp.admoblibrary.iap.PurchaseUtils.querySubscriptionSkuDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class IAPKotlinActivity : AppCompatActivity(),IapCallback {
    var skuDetails:SkuDetails? = null
    lateinit var binding:ActivityIapkotlinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iapkotlin)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_iapkotlin)
        initBilling(this,this)
        binding.btnSubscribe.setOnClickListener{
            lifecycleScope.launch {
                skuDetails?.let{

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenResumed {
//            skuDetails = querySubscriptionSkuDetails(getString(R.string.premium))?.get(0)
            binding.tvStatus.text = skuDetails?.price + skuDetails?.priceCurrencyCode
        }
        queryPurchases("subs", object : PurchaseCallback {
            override fun onQuerySuccessful() {
                binding.tvStatus.setText("Bought")
            }

            override fun onQueryFail() {
                binding.tvStatus.setText("Not yet bought")
            }
        })
    }

    override fun onSubscribeSuccessful() {
        Toast.makeText(this,"subscribe successfully",Toast.LENGTH_LONG).show()
    }

    override fun onSubscribeError() {
        Toast.makeText(this,"subscribe error",Toast.LENGTH_LONG).show()
    }
}