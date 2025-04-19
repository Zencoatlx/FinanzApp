package com.finanzapp.util

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Clase para gestionar las compras in-app y suscripciones.
 */
class BillingManager(private val context: Context) : PurchasesUpdatedListener, BillingClientStateListener {

    // Cliente de facturación
    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Estado para saber si el usuario es premium
    private val _isPremium = MutableLiveData<Boolean>(false)
    val isPremium: LiveData<Boolean> = _isPremium

    // Inicializar la conexión con Google Play Billing
    init {
        billingClient.startConnection(this)
    }

    // Productos disponibles en la app
    companion object {
        const val PREMIUM_MONTHLY = "premium_monthly"
        const val PREMIUM_YEARLY = "premium_yearly"
        const val PREMIUM_LIFETIME = "premium_lifetime"
        const val REMOVE_ADS = "remove_ads"
    }

    // Conexión establecida con Google Play Billing
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // La conexión se estableció correctamente
            queryPurchases()
        }
    }

    // Conexión perdida con Google Play Billing
    override fun onBillingServiceDisconnected() {
        // Intentar reconectar
        billingClient.startConnection(this)
    }

    // Consultar las compras existentes
    private fun queryPurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            // Consultar compras de productos
            val purchasesResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            // Consultar suscripciones
            val subscriptionsResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            // Procesar todas las compras
            val allPurchases = purchasesResult.purchasesList + subscriptionsResult.purchasesList
            processPurchases(allPurchases)
        }
    }

    // Procesar las compras
    private fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Verificar si el usuario ha comprado algún producto premium
                if (purchase.products.contains(PREMIUM_MONTHLY) ||
                    purchase.products.contains(PREMIUM_YEARLY) ||
                    purchase.products.contains(PREMIUM_LIFETIME)
                ) {
                    _isPremium.postValue(true)
                }

                // Confirmar la compra si aún no está reconocida
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase.purchaseToken)
                }
            }
        }
    }

    // Confirmar una compra
    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            billingClient.acknowledgePurchase(params)
        }
    }

    // Actualización de compras
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    // Iniciar flujo de compra
    fun launchPurchaseFlow(activity: Activity, productId: String, isSubscription: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            // Consultar detalles del producto
            val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

            val productDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    )
                )
                .build()

            val productDetailsResult = billingClient.queryProductDetails(productDetailsParams)

            if (productDetailsResult.productDetailsList?.isNotEmpty() == true) {
                val productDetails = productDetailsResult.productDetailsList!![0]

                // Preparar flujo de compra
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()

                // Mostrar pantalla de compra
                withContext(Dispatchers.Main) {
                    billingClient.launchBillingFlow(activity, billingFlowParams)
                }
            }
        }
    }

    // Comprobar si las suscripciones están soportadas
    suspend fun areSubscriptionsSupported(): Boolean {
        val billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        return billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }
}