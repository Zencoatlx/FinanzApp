package com.finanzapp

import android.app.Application
import com.finanzapp.util.BillingManager
import com.finanzapp.util.PremiumManager

class FinanzApp : Application() {

    // Managers
    lateinit var billingManager: BillingManager
    lateinit var premiumManager: PremiumManager

    override fun onCreate() {
        super.onCreate()

        // Inicializar managers
        billingManager = BillingManager(this)
        premiumManager = PremiumManager(this)
    }

    companion object {
        const val TAG = "FinanzApp"
    }
}