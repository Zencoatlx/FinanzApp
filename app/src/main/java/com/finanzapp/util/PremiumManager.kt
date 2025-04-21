package com.finanzapp.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PremiumManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val _isPremium = MutableLiveData<Boolean>()
    val isPremium: LiveData<Boolean> = _isPremium

    private val _monthlyPrice = MutableLiveData<String>()
    val monthlyPrice: LiveData<String> = _monthlyPrice

    private val _yearlyPrice = MutableLiveData<String>()
    val yearlyPrice: LiveData<String> = _yearlyPrice

    private val _lifetimePrice = MutableLiveData<String>()
    val lifetimePrice: LiveData<String> = _lifetimePrice

    // Valores de prueba
    val hasTrialPeriod = true
    val trialDaysRemaining = 7

    companion object {
        const val PRODUCT_ID_MONTHLY = "finanzapp.premium.monthly"
        const val PRODUCT_ID_YEARLY = "finanzapp.premium.yearly"
        const val PRODUCT_ID_LIFETIME = "finanzapp.premium.lifetime"

        @Volatile
        private var instance: PremiumManager? = null

        fun getInstance(context: Context): PremiumManager {
            return instance ?: synchronized(this) {
                instance ?: PremiumManager(context).also { instance = it }
            }
        }
    }

    init {
        // En una implementación real, aquí inicializaríamos el SDK de facturación
        // y obtendríamos el estado de suscripción del usuario
        _isPremium.value = false

        // Precios de prueba
        _monthlyPrice.value = "$3.99"
        _yearlyPrice.value = "$29.99"
        _lifetimePrice.value = "$49.99"
    }

    // Función que simula la compra
    fun launchBillingFlow(activity: Activity, productId: String) {
        // En una implementación real, aquí lanzaríamos el flujo de facturación
        Toast.makeText(appContext, "Iniciando compra de: $productId", Toast.LENGTH_SHORT).show()

        // Simulamos una compra exitosa después de un tiempo
        activity.window.decorView.postDelayed({
            // Simulamos que la compra fue exitosa
            _isPremium.value = true
            Toast.makeText(appContext, "¡Compra exitosa! Ahora eres Premium", Toast.LENGTH_LONG).show()
        }, 2000)
    }

    // Simulamos la verificación de características disponibles
    fun isFeatureSupported(featureId: String): Boolean {
        return true
    }
}