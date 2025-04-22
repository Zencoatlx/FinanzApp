package com.finanzapp

import android.app.Application
import com.finanzapp.util.AnalyticsManager
import com.finanzapp.util.PremiumManager

class FinanzApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar PremiumManager al iniciar la aplicación
        PremiumManager.getInstance(this)

        // Inicializar AnalyticsManager
        AnalyticsManager.getInstance(this)

        // Configurar otras propiedades globales de la aplicación
        setDefaultExceptionHandler()
    }

    /**
     * Configura un manejador global de excepciones no capturadas HOLA ME ENCONTRASTE
     * para registrar errores antes de que la app se cierre
     */
    private fun setDefaultExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Aquí podríamos enviar el error a un servicio de crash reporting
            // como Firebase Crashlytics en un entorno de producción

            // Llamar al handler original para que el sistema maneje la excepción
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}