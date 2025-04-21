package com.finanzapp

import android.app.Application
import com.finanzapp.util.PremiumManager

class FinanzApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar PremiumManager al iniciar la aplicación
        PremiumManager.getInstance(this)

        // Otras inicializaciones que pudieras necesitar
    }
}