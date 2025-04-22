package com.finanzapp.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Clase para gestionar el feedback de los usuarios
 */
class FeedbackManager(private val context: Context) {

    private val appVersionName: String by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "1.0"
        }
    }

    private val appVersionCode: Int by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Muestra diálogo para enviar un correo de feedback con
     * información sobre la aplicación y el dispositivo
     */
    fun showFeedbackDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle("Enviar comentarios")
            .setMessage("¿Quieres enviarnos tus comentarios o reportar un problema? Tu opinión es muy valiosa para mejorar FinanzApp.")
            .setPositiveButton("Enviar comentarios") { _, _ ->
                sendFeedbackEmail()
            }
            .setNegativeButton("Calificar app") { _, _ ->
                openAppInStore()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    /**
     * Abre el cliente de correo con información precompletada
     */
    private fun sendFeedbackEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:feedback@finanzapp.com")
            putExtra(Intent.EXTRA_SUBJECT, "Feedback de FinanzApp $appVersionName")
            putExtra(Intent.EXTRA_TEXT, getDeviceInfo())
        }

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Enviar comentarios"))
        } catch (e: Exception) {
            // Manejar caso donde no hay aplicación de correo
            showFallbackFeedbackDialog()
        }
    }

    /**
     * Muestra un diálogo alternativo cuando no hay app de correo
     */
    private fun showFallbackFeedbackDialog() {
        MaterialAlertDialogBuilder(context)
            .setTitle("No se puede enviar correo")
            .setMessage("Por favor envía tus comentarios a feedback@finanzapp.com o visítanos en nuestro sitio web.")
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * Abre la Play Store en la página de la app
     */
    private fun openAppInStore() {
        val packageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            // Si no está instalada la Play Store, abrir en el navegador
            context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    /**
     * Genera información del dispositivo para el reporte
     */
    private fun getDeviceInfo(): String {
        val analyticsManager = AnalyticsManager.getInstance(context)
        val userId = analyticsManager.getUserId().takeLast(8) // Solo mostramos parte del ID para privacidad

        return """
            |--- Información de la Aplicación ---
            |Versión: $appVersionName ($appVersionCode)
            |ID usuario: $userId
            |
            |--- Información del Dispositivo ---
            |Modelo: ${Build.MANUFACTURER} ${Build.MODEL}
            |Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            |
            |--- Describe aquí tus comentarios o el problema ---
            |
            |
        """.trimMargin()
    }
}