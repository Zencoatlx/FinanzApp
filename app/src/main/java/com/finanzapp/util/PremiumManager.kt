package com.finanzapp.util

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PremiumManager(private val context: Context) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _isPremium = MutableLiveData<Boolean>(false)
    val isPremium: LiveData<Boolean> = _isPremium

    private val _productDetails = MutableLiveData<ProductDetails?>()
    val productDetails: LiveData<ProductDetails?> = _productDetails

    private val productId = "premium_subscription"

    init {
        connectToBillingService()
    }

    private fun connectToBillingService() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // El servicio de facturación está listo
                    queryPurchases()
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Intentar reconectar al servicio
                connectToBillingService()
            }
        })
    }

    // Cambiado de private a public para que PremiumFragment pueda acceder a él
    fun queryPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            val purchasesResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val purchases = purchasesResult.purchasesList
            _isPremium.postValue(purchases.any { purchase ->
                purchase.products.contains(productId) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            })
        }
    }

    private fun queryProductDetails() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        // Corrigiendo la llamada a queryProductDetailsAsync con la API correcta
        CoroutineScope(Dispatchers.IO).launch {
            val productDetailsResult = billingClient.queryProductDetailsAsync(queryProductDetailsParams)

            if (productDetailsResult.productDetailsList?.isNotEmpty() == true) {
                _productDetails.postValue(productDetailsResult.productDetailsList?.first())
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val productDetails = _productDetails.value ?: return

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.products.contains(productId)) {
                    _isPremium.postValue(true)
                    // Verificar y reconocer la compra
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { /* Manejar resultado */ }
            }
        }
    }
}